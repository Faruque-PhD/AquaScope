package com.example.root.ffttest2;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.core.content.ContextCompat;

import android.content.Context;

/**
 * Layer 1 of the MoE gating network, acoustic half: turns the ambient sound field
 * into a normalised noise score.
 *
 * A dedicated {@link AudioRecord} keeps a rolling window of microphone samples
 * (default 200 ms) and runs an FFT over it. The score is derived from the sound
 * pressure level across the 1-30 kHz acoustic communication band:
 *
 *   noise_score = clamp((SPL_dB - quiet_baseline_dB) / dynamic_range_dB, 0, 1)
 *
 * Broadband energy (propellers, structural surge) is penalised more than tonal
 * noise, because tonal noise is more likely to be filtered out by the receiver.
 *
 * Why this exists alongside the expert-reported SNR: SNR is backward-looking, it
 * describes the previous transmission. The noise score is forward-looking. Boat
 * traffic and surge can change the acoustic environment within a second or two,
 * which is faster than the ACK/NACK cycle, so the router needs to see the current
 * environment before it transmits.
 */
public class NoiseAnalyzer implements Runnable {

    private static final String TAG = "NoiseAnalyzer";
    private static final int SAMPLE_RATE = 48000;

    private final Context context;
    private volatile boolean running = false;
    private Thread thread;
    private AudioRecord record;

    // Rolling window of samples, kept as raw short PCM.
    private short[] window;
    private int windowFill = 0;
    private float lastScore = 0f;

    public NoiseAnalyzer(Context context) {
        this.context = context.getApplicationContext();
    }

    /** Start the background analysis loop. Safe to call more than once. */
    public synchronized void start() {
        if (running) {
            Utils.logd("NoiseAnalyzer: already running, ignoring start()");
            return;
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Utils.logd("NoiseAnalyzer: RECORD_AUDIO not granted, noise score stays 0");
            return;
        }
        int windowSamples = (int) (SAMPLE_RATE * (RouterConfig.NOISE_WINDOW_MS / 1000.0));
        // The FFT is radix-2 and requires a power-of-two length. Round down to the
        // largest power of two that fits the requested window, otherwise the
        // transform silently does nothing and the score is computed from raw
        // samples instead of a spectrum.
        windowSamples = Integer.highestOneBit(windowSamples);
        if (windowSamples < 1024) windowSamples = 1024;
        Log.i(TAG, "Noise window: " + windowSamples + " samples ("
                + (windowSamples * 1000 / SAMPLE_RATE) + " ms @ " + SAMPLE_RATE + " Hz)");
        window = new short[windowSamples];
        windowFill = 0;
        running = true;
        thread = new Thread(this, "MoENoiseAnalyzer");
        thread.start();
    }

    /** Stop the loop and release the microphone. */
    public void stop() {
        running = false;
        Thread t = thread;
        if (t != null) {
            t.interrupt();
            thread = null;
        }
        releaseRecord();
    }

    public float lastScore() {
        return lastScore;
    }

    @Override
    public void run() {
        int minBuffer = AudioRecord.getMinBufferSize(
                SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (minBuffer <= 0) {
            Utils.logd("NoiseAnalyzer: unsupported sample rate");
            return;
        }
        AudioRecord rec;
        try {
            rec = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, minBuffer * 2);
        } catch (SecurityException e) {
            Utils.logd("NoiseAnalyzer: no permission for AudioRecord: " + e);
            return;
        }
        if (rec.getState() != AudioRecord.STATE_INITIALIZED) {
            Utils.logd("NoiseAnalyzer: AudioRecord failed to initialise");
            rec.release();
            return;
        }

        record = rec;
        record.startRecording();
        if (rec.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            Utils.logd("NoiseAnalyzer: startRecording() failed on an uninitialised record");
            releaseRecord();
            return;
        }

        short[] chunk = new short[1024];
        while (running) {
            AudioRecord cur = record;
            if (cur == null) break;
            int read;
            try {
                read = cur.read(chunk, 0, chunk.length);
            } catch (IllegalStateException e) {
                // stop() released the recorder out from under us. Expected on teardown.
                break;
            }
            if (read > 0) {
                appendToWindow(chunk, read);
                if (windowFill == window.length) {
                    lastScore = computeScore(window);
                    windowFill = 0;
                }
            } else if (read < 0) {
                break;
            }
        }
        releaseRecord();
    }

    private void appendToWindow(short[] chunk, int len) {
        int space = window.length - windowFill;
        int copy = Math.min(space, len);
        System.arraycopy(chunk, 0, window, windowFill, copy);
        windowFill += copy;
        if (copy < len) {
            // Window wrapped mid-chunk; analyse what we have and keep the tail.
            lastScore = computeScore(window);
            System.arraycopy(chunk, copy, window, 0, len - copy);
            windowFill = len - copy;
        }
    }

    /**
     * FFT magnitude in the 1-30 kHz band, normalised to a 0-1 score.
     */
    private float computeScore(short[] samples) {
        int n = samples.length;
        if (n < 2) return 0f;

        double[] re = new double[n];
        double[] im = new double[n];
        for (int i = 0; i < n; i++) {
            re[i] = samples[i] / 32768.0;
        }
        // Hann window before the transform: the buffer edges are discontinuous
        // and would otherwise smear energy across the whole spectrum.
        FFT.hann(re);
        FFT.fft(re, im);

        double binHz = SAMPLE_RATE / (double) n;
        int loBin = (int) Math.max(1, Math.floor(RouterConfig.NOISE_BAND_LOW_HZ / binHz));
        int hiBin = (int) Math.min(n / 2 - 1, Math.ceil(RouterConfig.NOISE_BAND_HIGH_HZ / binHz));

        double bandEnergy = 0.0;
        for (int k = loBin; k <= hiBin && k < n / 2; k++) {
            bandEnergy += re[k] * re[k] + im[k] * im[k];
        }
        int bins = Math.max(1, hiBin - loBin + 1);
        double rms = Math.sqrt(bandEnergy / bins);

        // Convert to dBFS relative to full scale.
        double db = 20.0 * Math.log10(rms + 1e-12);
        float score = (float) ((db - RouterConfig.QUIET_BASELINE_DB) / RouterConfig.NOISE_DYNAMIC_RANGE_DB);
        if (score < 0f) score = 0f;
        if (score > 1f) score = 1f;

        Utils.logd("Noise: bandRms=" + rms + " dB=" + db + " score=" + score);
        return score;
    }

    /**
     * Tear down the recorder exactly once.
     *
     * Synchronized because stop() runs on the caller's thread while the read loop
     * runs on the analysis thread, and both reach this method. An unsynchronized
     * version would NPE when stop() nulls the field between the null check and
     * the release call.
     */
    private synchronized void releaseRecord() {
        AudioRecord rec = record;
        record = null;
        if (rec == null) return;
        try {
            if (rec.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                rec.stop();
            }
        } catch (IllegalStateException ignored) {
            // Recorder was already torn down; nothing to release cleanly.
        }
        rec.release();
    }
}
