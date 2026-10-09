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
    private long lastRateLogMs = 0;
    private int imgWidth = 0;
    private int imgHeight = 0;
    private long analyzeSumNanos = 0;
    private int analyzeFrames = 0;

    private volatile boolean armed = false;
    private volatile boolean burstInProgress = false;
    /** When the current burst was first seen; drives the decode deadline. */
    private long burstStartMs = 0;
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
        burstStartMs = 0;
        burstOutcomeRecorded = false;
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
     *
     * The analysis resolution is deliberately low. The demodulator only needs the
     * mean luminance of a centre ROI, so full resolution buys nothing.
     *
     * Note this is not what sets the link rate. Measured on an SM-A346E, the
     * analysis rate is ~9 fps whether analysis is bound alone, or alongside
     * preview and capture, and at full or reduced resolution. The rate is a
     * property of the camera, which is why {@link FlashTransmitter#SYMBOL_DURATION_MS}
     * is sized to it rather than the other way round.
     */
    public static ImageAnalysis buildAnalyzer(OpticalReceiver receiver) {
        ImageAnalysis analysis = new ImageAnalysis.Builder()
                .setTargetResolution(new android.util.Size(640, 480))
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

            long t0 = System.nanoTime();
            float level = meanCentreLuminance(image);
            long t1 = System.nanoTime();
            analyzeSumNanos += (t1 - t0);
            analyzeFrames++;
            if (imgWidth == 0) {
                imgWidth = image.getWidth();
                imgHeight = image.getHeight();
            }
            long now = t1 / 1_000_000L;

            // Track frame rate so a device that cannot oversample the symbol rate
            // is reported rather than silently producing garbage.
            if (lastFrameMs != 0 && now > lastFrameMs) {
                frameCount++;
                if (frameCount >= 10) {
                    frameRate = 1000f * (frameCount - 1) / (now - lastFrameMs);
                    frameCount = 0;
                    lastFrameMs = now;
                    // Logged every few seconds rather than once. The first
                    // reading after a bind includes camera warm-up and can come
                    // out well under the steady-state rate, so a single sample is
                    // not something to tune a symbol duration against, and
                    // tuning is exactly what this line is for.
                    if (now - lastRateLogMs >= 5000) {
                        lastRateLogMs = now;
                        double sps = frameRate * Constants.opticalSymbolDurationMs / 1000.0;
                        // The average time this thread spends inside the analyzer
                        // separates the two possible causes of a low rate: if it
                        // is a small fraction of the frame interval the camera
                        // simply is not delivering more, and if it is close to
                        // the interval the callback itself is the ceiling.
                        double analyzeMs = analyzeFrames > 0
                                ? analyzeSumNanos / 1e6 / analyzeFrames : 0;
                        double intervalMs = frameRate > 0 ? 1000.0 / frameRate : 0;
                        analyzeSumNanos = 0;
                        analyzeFrames = 0;
                        Utils.log("OpticalReceiver: camera delivers " + String.format("%.1f", frameRate)
                                + " fps, " + String.format("%.1f", sps)
                                + " samples per " + Constants.opticalSymbolDurationMs
                                + "ms symbol" + (sps < 2.0
                                ? " - TOO FEW to resolve the eye pattern, raise the symbol duration"
                                : " - sufficient to demodulate")
                                + " | analysis " + imgWidth + "x" + imgHeight
                                + " " + String.format("%.2f", analyzeMs) + "ms of "
                                + String.format("%.1f", intervalMs) + "ms interval");
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
                    burstOutcomeRecorded = false;
                    burstStartMs = System.currentTimeMillis();
                    Utils.log("OpticalReceiver: burst detected, ambient=" + ambient
                            + " signal=" + signal + " threshold=" + threshold
                            + " frameRate=" + String.format("%.1f", frameRate) + "fps");
                    // The pulse just measured how much light survived the path,
                    // which is what turbidity actually does to a link. Feed it back
                    // into the MoE context before demodulating.
                    publishLinkQuality(ambient, signal);
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
        if (frameRate > 0f && frameRate * Constants.opticalSymbolDurationMs / 1000.0 < 2.0) {
            Utils.log("OpticalReceiver: " + String.format("%.1f", frameRate)
                    + "fps is too low for " + Constants.opticalSymbolDurationMs + "ms symbols; "
                    + String.format("%.1f", frameRate * Constants.opticalSymbolDurationMs / 1000.0)
                    + " samples per symbol");
        }
        int[] recovered = Demodulator.demodulate(
                samples, sampleTimeMs, sampleCount, threshold,
                Constants.opticalSymbolDurationMs,
                FlashTransmitter.PREAMBLE_SYMBOLS, expectedLineBits());
        if (recovered == null) {
            // Returning null is the normal state for most of a burst: the
            // demodulator is asked on every frame and only succeeds once the
            // whole payload has landed. So this is not a failure yet.
            //
            // It becomes one when the burst has run past its own length without
            // decoding. Recording the failure here rather than at detection is
            // what makes the outcome exactly one sample per burst - a naive
            // record-on-null would fire on the first frame of every burst, while
            // the preamble was still incomplete, and would then suppress the
            // real result.
            //
            // Closing the burst also matters on its own account: without it a
            // burst that never decodes stays in progress until the receiver is
            // re-armed, and every following frame is spent demodulating a buffer
            // that will never change.
            //
            // The deadline is the framing plus slack. The slack has to swallow
            // the transmitter's rate error: the real cadence runs long of
            // nominal (204.7 ms per symbol against a configured 200 ms measured
            // on this hardware), so the payload's last symbol lands ~4.7 ms x
            // 976 symbols later than nominal predicts. A 2 s slack expired one
            // second before the payload finished, closing the burst before the
            // sweep ever saw a complete buffer; the slack is now 8 s.
            long deadline = (long) (FlashTransmitter.PREAMBLE_SYMBOLS
                    + expectedLineBits()
                    + FlashTransmitter.POSTAMBLE_SYMBOLS)
                    * (long) Constants.opticalSymbolDurationMs + 8000L;
            if (burstStartMs > 0 && System.currentTimeMillis() - burstStartMs > deadline) {
                recordBurstOutcome(false);
                Utils.log("OpticalReceiver: burst ran " + (System.currentTimeMillis() - burstStartMs)
                        + "ms without decoding (deadline " + deadline + "ms"
                        + "; last fit P=" + String.format("%.2f", Demodulator.lastFittedPeriodMs)
                        + "ms phase=" + (long) Demodulator.lastFittedPhaseMs
                        + " score=" + Demodulator.lastFittedScore + "/" + (expectedLineBits() / 5)
                        + "); closing it");
                burstInProgress = false;
                sampleCount = 0;
            }
            return;
        }
        deliver(recovered);
    }

    /**
     * Whether this burst has already been reported to {@link MoE}, so each burst
     * contributes exactly one sample to the error history.
     */
    private boolean burstOutcomeRecorded = false;

    /**
     * Record the burst's result against optical, once per burst.
     *
     * <p>A success lands after the payload decodes; a failure lands the first
     * time the demodulator comes back empty. Either way, this is how the MoE
     * finally gets an optical quality history: {@link #lastMeasuredOpticalSnr}
     * carries the pulse contrast measured at detection, so the record is
     * outcome plus the link quality that produced it rather than an unrelated
     * number.
     */
    private void recordBurstOutcome(boolean ok) {
        if (burstOutcomeRecorded) {
            return;
        }
        burstOutcomeRecorded = true;
        MoE.recordResult(ok, lastMeasuredOpticalSnr, Constants.CommMedium.OPTICAL);
        Utils.log("MoE: optical burst recorded (" + (ok ? "ok" : "failed")
                + ", quality=" + lastMeasuredOpticalSnr + ")");
    }

    /**
     * Last optical link quality published by {@link #publishLinkQuality}, 0-100,
     * so the decode outcome can be recorded with a matching SNR rather than with
     * the acoustic expert's number.
     */
    private static volatile int lastMeasuredOpticalSnr = 0;

    /**
     * Turn a detected pulse into a turbidity reading and publish it.
     *
     * <p>This measures the thing the camera-from-the-scene metric cannot: light
     * actually reaching the receiver through the path between the two phones.
     * Suspended particles scatter and absorb it, so the gap between the ambient
     * level and the received pulse is a direct, propagation-based turbidity
     * proxy — closer in spirit to an optical time-domain measurement than to
     * judging sharpness of a photograph.
     *
     * <p>Calibration: detection itself requires signal > ambient *
     * {@link #MIN_SIGNAL_RATIO}, so any burst that reaches here already has a
     * contrast of at least 1 - 1/1.6 = 0.375. That observed floor is mapped onto
     * 1.0 turbidity and a full-scale pulse onto 0. Without this rescaling the
     * value would top out around 0.63 and could never reach the router's 0.70
     * veto threshold, making the measurement decorative.
     */
    private static void publishLinkQuality(float ambient, float signal) {
        if (signal <= 0f || Float.isNaN(signal) || Float.isNaN(ambient)) {
            return;
        }
        float contrast = (signal - Math.max(ambient, 0f)) / signal;
        float floor = 1f - 1f / MIN_SIGNAL_RATIO;   // 0.375 for a 1.6x trigger
        float norm = (contrast - floor) / (1f - floor);
        if (norm < 0f) norm = 0f;
        if (norm > 1f) norm = 1f;
        float turbidity = 1f - norm;
        lastMeasuredOpticalSnr = Math.round(100f * (1f - turbidity));
        ContextVector prev = ContextVector.current();
        ContextVector.publish(new ContextVector(
                turbidity, prev.noiseScore, prev.meanLuminance, prev.laplacianVariance));
        Utils.log("MoE: optical pulse -> contrast=" + String.format("%.2f", contrast)
                + " turbidity=" + String.format("%.2f", turbidity)
                + " quality=" + lastMeasuredOpticalSnr);
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
            // The symbols recovered but the payload did not survive the 4B5B and
            // parity stages, which is the clearest possible optical NACK.
            recordBurstOutcome(false);
            return;
        }
        boolean[] valid = new boolean[tokens.length];
        Arrays.fill(valid, true);
        Utils.log("OpticalReceiver: recovered " + tokens.length + " tokens: "
                + Arrays.toString(tokens));
        recordBurstOutcome(true);
        if (listener != null) {
            listener.onOpticalTokensReceived(tokens, valid, 0);
        }
        burstInProgress = false;
        sampleCount = 0;
    }
}
