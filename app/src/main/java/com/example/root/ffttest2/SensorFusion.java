package com.example.root.ffttest2;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

/**
 * Layer 1 of the MoE gating network: the sensor fusion pipeline.
 *
 * Runs on its own background thread on a 500 ms tick. Each tick grabs the most
 * recent camera frame, computes the turbidity score, folds in the latest noise
 * score from {@link NoiseAnalyzer}, and publishes the result to
 * {@link ContextVector}. By the time a message reaches the router every input to
 * the decision is already computed and cached, which is what keeps the per-message
 * routing path to a few microseconds of arithmetic.
 *
 * The camera frame is obtained from {@link CameraHelper} without triggering a new
 * capture: the live preview is already running, so this reuses the last analysed
 * frame rather than adding capture latency to the routing path.
 */
public class SensorFusion {

    private static volatile SensorFusion instance;

    private final Context context;
    private final Handler main = new Handler(Looper.getMainLooper());
    private NoiseAnalyzer noiseAnalyzer;

    private Thread thread;
    private volatile boolean running = false;

    private volatile Bitmap latestFrame;

    private SensorFusion(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized SensorFusion get(Context context) {
        if (instance == null) {
            instance = new SensorFusion(context);
        }
        return instance;
    }

    /** Start the fusion thread (idempotent). */
    public synchronized void start() {
        if (running) {
            // onResume fires again after each permission dialog, so reaching here
            // is normal. Re-announcing would look like a second startup and would
            // hide a genuine leak.
            return;
        }
        running = true;
        if (noiseAnalyzer == null) {
            noiseAnalyzer = new NoiseAnalyzer(context);
        }
        noiseAnalyzer.start();
        thread = new Thread(this::loop, "MoESensorFusion");
        thread.start();
        Utils.log("SensorFusion: started (tick=" + RouterConfig.SENSOR_TICK_MS + "ms)");
    }

    /** Stop the fusion thread and release the microphone. */
    public synchronized void stop() {
        running = false;
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
        if (noiseAnalyzer != null) {
            noiseAnalyzer.stop();
        }
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * Publish a frame for turbidity analysis. Called by the camera layer whenever
     * a preview frame or capture is available, so the fusion thread always has
     * something current to analyse.
     */
    public void submitFrame(Bitmap frame) {
        if (frame == null) return;
        if (frame.isRecycled()) return;
        this.latestFrame = frame;
    }

    private void loop() {
        while (running) {
            try {
                updateTurbidity();
            } catch (Exception e) {
                Utils.logd("SensorFusion tick failed: " + e);
            }
            try {
                Thread.sleep(RouterConfig.SENSOR_TICK_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void updateTurbidity() {
        Bitmap frame = latestFrame;
        if (frame == null) {
            // No frame yet. Carry the previous noise score with a neutral
            // turbidity so the router degrades toward acoustic rather than
            // making a decision on absent data.
            ContextVector prev = ContextVector.current();
            if (prev.turbidityScore != 0f) {
                ContextVector.publish(new ContextVector(0f, prev.noiseScore, 0f, 0f));
            }
            return;
        }
        ContextVector prev = ContextVector.current();
        ContextVector withTurbidity = TurbidityAnalyzer.analyse(frame, prev);
        float noise = (noiseAnalyzer != null) ? noiseAnalyzer.lastScore() : prev.noiseScore;
        ContextVector merged = new ContextVector(
                withTurbidity.turbidityScore,
                noise,
                withTurbidity.meanLuminance,
                withTurbidity.laplacianVariance);
        ContextVector.publish(merged);
    }

    /**
     * Analyse a bitmap immediately and fold in the latest noise score. Used by the
     * send path so a transmission always uses a turbidity reading of the image
     * that is actually being sent, not one from an earlier tick.
     */
    public ContextVector analyseNow(Bitmap frame) {
        ContextVector prev = ContextVector.current();
        ContextVector t = TurbidityAnalyzer.analyse(frame, prev);
        float noise = (noiseAnalyzer != null) ? noiseAnalyzer.lastScore() : prev.noiseScore;
        ContextVector merged = new ContextVector(t.turbidityScore, noise, t.meanLuminance, t.laplacianVariance);
        ContextVector.publish(merged);
        return merged;
    }
}
