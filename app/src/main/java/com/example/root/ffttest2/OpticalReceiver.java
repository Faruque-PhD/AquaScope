package com.example.root.ffttest2;

import android.graphics.ImageFormat;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import java.util.Arrays;

/**
 * Optical receiver: demodulates an OOK torch burst captured with the phone camera
 * and reconstructs the transmitted image.
 *
 * The link is a camera, not a photodiode, which shapes every design choice here:
 *
 *   - Frames arrive at roughly 30/s, so a symbol must be long enough to be
 *     oversampled. The transmitted symbol duration is
 *     {@link FlashTransmitter#SYMBOL_DURATION_MS}.
 *   - Each frame's luminance is averaged over a small centre ROI. Averaging is
 *     what makes a point-source torch visible: the LED may be far smaller than a
 *     pixel, and the frame's exposure varies between devices.
 *   - There is no reference level available, so the decision threshold is
 *     adaptive: the ambient is tracked with a slow moving average and the signal
 *     level is taken from the burst peak. This is the same logic an AGC would do.
 *
 * Symbol timing is recovered from the preamble rather than assumed. The detector
 * waits for a brightness jump, then slides a symbol-rate decision window across
 * the samples looking for the alternating preamble pattern. Once the preamble is
 * matched, the payload follows at a known symbol rate.
 *
 * Demodulated 5-bit groups that do not exist in the 4B5B table are channel
 * errors, not random data. They become the lost-token marker (4096) so the
 * error-correcting transformer can repair them, which is the same treatment the
 * acoustic receiver gives its CRC failures.
 */
public class OpticalReceiver {

    private static final String TAG = "OpticalReceiver";

    // ---- Adaptive threshold parameters --------------------------------------
    /** Samples of burst needed before the signal level is considered known. */
    private static final int MIN_BURST_SAMPLES = 6;
    /** Fraction of the noise span the threshold sits above the ambient. */
    private static final float THRESHOLD_FRACTION = 0.45f;
    /** Ratio of signal to ambient required to call a burst. */
    private static final float MIN_SIGNAL_RATIO = 1.6f;
    /** Centre ROI as a fraction of the frame, in both dimensions. */
    private static final float ROI_FRACTION = 0.30f;

    // ---- Demodulator state --------------------------------------------------
    private final float[] samples = new float[4096];
    private long[] sampleTimeMs = new long[4096];
    private int sampleCount = 0;

    private float ambient = Float.NaN;
    private long lastFrameMs = 0;
    private int frameCount = 0;
    private float frameRate = 0f;
    private boolean rateLogged = false;

    private volatile boolean armed = false;
    private volatile boolean burstInProgress = false;
    private float signal = Float.NaN;

    /** Set by the host so the recovered image can be displayed. */
    public interface Listener {
        void onOpticalTokensReceived(long[] tokens, boolean[] tokenValid, int errorCount);
    }

    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    // =======================================================================
    // Arming
    // =======================================================================

    public void arm() {
        armed = true;
        reset();
        Utils.log("OpticalReceiver: armed, waiting for a flash burst");
    }

    public void disarm() {
        armed = false;
        reset();
    }

    public boolean isArmed() {
        return armed;
    }

    private void reset() {
        sampleCount = 0;
        burstInProgress = false;
        signal = Float.NaN;
        frameCount = 0;
    }

    public float measuredFrameRate() {
        return frameRate;
    }

    // =======================================================================
    // Frame ingestion
    // =======================================================================

    /**
     * Build an analyzer that feeds every frame into the demodulator. Attach it as
     * an additional CameraX use case; the same camera cannot run it and drive the
     * torch at once, which is why the sender releases the session first.
     */
    public static ImageAnalysis buildAnalyzer(OpticalReceiver receiver) {
        ImageAnalysis analysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build();
        analysis.setAnalyzer(receiver.executor, image -> receiver.onFrame(image));
        return analysis;
    }

    private final java.util.concurrent.ExecutorService executor =
            java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "OpticalReceiver");
                t.setDaemon(true);
                return t;
            });

    void onFrame(@NonNull ImageProxy image) {
        try {
            if (!armed) return;
            if (image.getFormat() != ImageFormat.YUV_420_888) return;

            float level = meanCentreLuminance(image);
            long now = System.nanoTime() / 1_000_000L;

            // Track frame rate so a device that cannot oversample the symbol rate
            // is reported rather than silently producing garbage.
            if (lastFrameMs != 0 && now > lastFrameMs) {
                frameCount++;
                if (frameCount >= 10) {
                    frameRate = 1000f * (frameCount - 1) / (now - lastFrameMs);
                    frameCount = 0;
                    lastFrameMs = now;
                    if (!rateLogged) {
                        // Report the achieved rate once. A camera slower than
                        // 2 frames per symbol cannot resolve the eye pattern, and
                        // the operator needs to know that before a field test.
                        rateLogged = true;
                        double sps = frameRate * FlashTransmitter.SYMBOL_DURATION_MS / 1000.0;
                        Utils.log("OpticalReceiver: camera delivers " + String.format("%.1f", frameRate)
                                + " fps, " + String.format("%.1f", sps)
                                + " samples per " + FlashTransmitter.SYMBOL_DURATION_MS
                                + "ms symbol" + (sps < 2.0
                                ? " - TOO FEW to resolve the eye pattern, raise the symbol duration"
                                : " - sufficient to demodulate"));
                    }
                }
            } else {
                lastFrameMs = now;
            }

            if (sampleCount < samples.length) {
                samples[sampleCount] = level;
                sampleTimeMs[sampleCount] = now;
                sampleCount++;
            }
            detectBurst();
        } catch (Throwable t) {
            Utils.logd("OpticalReceiver frame error: " + t);
        } finally {
            image.close();
        }
    }

    /**
     * Mean luma over the centre ROI. The Y plane of YUV_420_888 is full resolution
     * and is the luminance channel, so no colour conversion is needed.
     */
    private float meanCentreLuminance(ImageProxy image) {
        ImageProxy.PlaneProxy plane = image.getPlanes()[0];
        java.nio.ByteBuffer buffer = plane.getBuffer();
        int rowStride = plane.getRowStride();
        int width = image.getWidth();
        int height = image.getHeight();

        int roiW = Math.max(1, (int) (width * ROI_FRACTION));
        int roiH = Math.max(1, (int) (height * ROI_FRACTION));
        int x0 = (width - roiW) / 2;
        int y0 = (height - roiH) / 2;

        double sum = 0;
        int n = 0;
        byte[] row = new byte[rowStride];
        for (int y = y0; y < y0 + roiH; y++) {
            int offset = y * rowStride + x0;
            if (offset + roiW > buffer.limit()) break;
            buffer.position(offset);
            buffer.get(row, 0, roiW);
            for (int x = 0; x < roiW; x++) {
                sum += (row[x] & 0xFF);
                n++;
            }
        }
        if (n == 0) return -1f;
        return (float) (sum / n);
    }

    // =======================================================================
    // Burst detection and demodulation
    // =======================================================================

    private void detectBurst() {
        if (sampleCount < MIN_BURST_SAMPLES) {
            updateAmbient();
            return;
        }

        if (!burstInProgress) {
            // Idle: track the ambient as a slow average of the whole sample buffer,
            // which is just the received energy while nothing is transmitting.
            float current = samples[sampleCount - 1];
            if (Float.isNaN(ambient)) {
                ambient = current;
            }
            // Look for a level clearly above the ambient established so far.
            if (current > ambient * MIN_SIGNAL_RATIO && current > 8f) {
                // Estimate the signal level from the run of high samples.
                signal = estimateSignal();
                float threshold = ambient + THRESHOLD_FRACTION * (signal - ambient);
                if (signal > threshold) {
                    burstInProgress = true;
                    Utils.log("OpticalReceiver: burst detected, ambient=" + ambient
                            + " signal=" + signal + " threshold=" + threshold
                            + " frameRate=" + String.format("%.1f", frameRate) + "fps");
                    demodulate(threshold);
                }
            } else {
                // Exponential moving average keeps up with slow ambient drift
                // without being dragged up by the burst itself.
                ambient = 0.99f * ambient + 0.01f * current;
            }
            return;
        }

        demodulate(ambient + THRESHOLD_FRACTION * (signal - ambient));
    }

    private void updateAmbient() {
        if (sampleCount > 0) {
            float v = samples[sampleCount - 1];
            ambient = Float.isNaN(ambient) ? v : 0.99f * ambient + 0.01f * v;
        }
    }

    private float estimateSignal() {
        float max = 0;
        for (int i = 0; i < sampleCount; i++) {
            if (samples[i] > max) max = samples[i];
        }
        return max;
    }

    /**
     * Walk the sample buffer at the symbol rate, find the preamble, then collect
     * the payload.
     */
    private void demodulate(float threshold) {
        if (frameRate > 0f && frameRate * FlashTransmitter.SYMBOL_DURATION_MS / 1000.0 < 2.0) {
            Utils.log("OpticalReceiver: " + String.format("%.1f", frameRate)
                    + "fps is too low for " + FlashTransmitter.SYMBOL_DURATION_MS + "ms symbols; "
                    + String.format("%.1f", frameRate * FlashTransmitter.SYMBOL_DURATION_MS / 1000.0)
                    + " samples per symbol");
        }
        int[] recovered = Demodulator.demodulate(
                samples, sampleTimeMs, sampleCount, threshold,
                FlashTransmitter.SYMBOL_DURATION_MS,
                FlashTransmitter.PREAMBLE_SYMBOLS, expectedLineBits());
        if (recovered == null) return;
        deliver(recovered);
    }

    /** Payload size implied by the active codebook, so the receiver knows when a
     * burst is complete without any out-of-band signalling. */
    int expectedLineBits() {
        int bitsPerToken = Constants.codebookSize.equals("4096") ? 12
                : Constants.codebookSize.equals("256") ? 8 : 10;
        int tokens = 64;
        int payloadBits = tokens * bitsPerToken;
        int bytes = (payloadBits + 7) / 8;
        return bytes * 10; // 4B5B
    }

    // =======================================================================
    // Delivery
    // =======================================================================

    private int[] lastBytes;

    /** Payload bytes from the most recent successful burst, or null. */
    public int[] lastPayloadBytes() {
        return lastBytes == null ? null : Arrays.copyOf(lastBytes, lastBytes.length);
    }

    private void deliver(int[] bytes) {
        lastBytes = bytes;
        byte[] payload = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            payload[i] = (byte) bytes[i];
        }
        long[] tokens = Utils.Bytes2Embedding(payload);
        if (tokens == null || tokens.length == 0) {
            Utils.log("OpticalReceiver: payload decoded to no tokens");
            return;
        }
        boolean[] valid = new boolean[tokens.length];
        Arrays.fill(valid, true);
        Utils.log("OpticalReceiver: recovered " + tokens.length + " tokens: "
                + Arrays.toString(tokens));
        if (listener != null) {
            listener.onOpticalTokensReceived(tokens, valid, 0);
        }
        burstInProgress = false;
        sampleCount = 0;
    }
}
