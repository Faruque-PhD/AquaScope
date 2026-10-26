package com.example.root.ffttest2;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Layer 1 of the MoE gating network, optical half: turns a camera frame into a
 * normalised turbidity score.
 *
 * Two metrics are computed over a centre crop, per the design reference:
 *
 *   Variance of Laplacian — measures sharpness. Suspended particles scatter the
 *   light before it forms a coherent image, so silty water produces extreme blur
 *   and a low variance. This is the primary signal:
 *       turbidity = 1 - clamp(variance / baseline, 0, 1)
 *
 *   Mean luminance of the same crop — a secondary check. Near-total darkness
 *   (night dive or heavy silt) also indicates an unusable optical path, because
 *   the receiver has no ambient reference to detect pulses against.
 *
 * Both are computed on a small downscale, which keeps the Laplacian pass
 * sub-millisecond on a mobile CPU as the doc assumes.
 */
public final class TurbidityAnalyzer {

    private TurbidityAnalyzer() {}

    /** Working resolution for the analysis; small on purpose, this is not display. */
    private static final int WORK_SIZE = 64;

    /**
     * Analyse a frame and return the ContextVector with the turbidity fields set.
     * The noise score is carried over from the previous vector because this class
     * only owns the optical half of the sensor pipeline.
     */
    public static ContextVector analyse(Bitmap frame, ContextVector previous) {
        float prevNoise = (previous == null) ? 0f : previous.noiseScore;
        if (frame == null) {
            return new ContextVector(0f, prevNoise, 0f, 0f);
        }

        Bitmap crop = centreCropToSquare(frame);
        Bitmap small = Bitmap.createScaledBitmap(crop, WORK_SIZE, WORK_SIZE, true);

        int w = small.getWidth();
        int h = small.getHeight();
        int[] pixels = new int[w * h];
        small.getPixels(pixels, 0, w, 0, 0, w, h);

        // Greyscale plane, computed once and reused by both metrics.
        float[] grey = new float[w * h];
        double sum = 0.0;
        for (int i = 0; i < pixels.length; i++) {
            int c = pixels[i];
            float v = (0.299f * Color.red(c) + 0.587f * Color.green(c) + 0.114f * Color.blue(c)) / 255f;
            grey[i] = v;
            sum += v;
        }
        float meanLuminance = (float) (sum / pixels.length);

        // Variance of the Laplacian over the interior (border pixels have no
        // full 3x3 neighbourhood).
        double lapSum = 0.0;
        double lapSumSq = 0.0;
        int n = 0;
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                float v = laplacian(grey, w, h, x, y);
                lapSum += v;
                lapSumSq += v * v;
                n++;
            }
        }
        double mean = (n == 0) ? 0.0 : lapSum / n;
        double variance = (n == 0) ? 0.0 : (lapSumSq / n) - (mean * mean);
        if (variance < 0) variance = 0;
        float laplacianVariance = (float) variance;

        // Primary signal: blur -> turbidity.
        float sharpness = laplacianVariance / RouterConfig.TURBIDITY_BASELINE_VARIANCE;
        if (sharpness > 1f) sharpness = 1f;
        if (sharpness < 0f) sharpness = 0f;
        float turbidity = 1f - sharpness;

        // A frame this flat is not "turbid water", it is a dead or mispointed
        // camera. Treat it as unusable either way, but say which so the log
        // distinguishes a calibration problem from a real optical veto.
        if (laplacianVariance < RouterConfig.TURBIDITY_FLOOR_VARIANCE) {
            Utils.logd("Turbidity: variance below floor (" + laplacianVariance
                    + " < " + RouterConfig.TURBIDITY_FLOOR_VARIANCE
                    + "), frame has no usable detail - check camera or recalibrate baseline");
        }

        // Secondary signal: darkness also means an unusable optical path.
        float darkness = 0f;
        if (meanLuminance < RouterConfig.DARKNESS_LUMINANCE_THRESHOLD) {
            darkness = RouterConfig.DARKNESS_TURBIDITY_WEIGHT
                    * (1f - meanLuminance / RouterConfig.DARKNESS_LUMINANCE_THRESHOLD);
        }
        float combined = turbidity + darkness;
        if (combined > 1f) combined = 1f;

        Utils.logd("Turbidity: variance=" + laplacianVariance
                + " meanLum=" + meanLuminance
                + " turbidity=" + combined);

        recycle(small);
        if (crop != frame) recycle(crop);

        return new ContextVector(combined, prevNoise, meanLuminance, laplacianVariance);
    }

    private static float laplacian(float[] g, int w, int h, int x, int y) {
        int i = y * w + x;
        // 4-neighbour Laplacian kernel.
        return 4f * g[i] - g[i - 1] - g[i + 1] - g[i - w] - g[i + w];
    }

    private static Bitmap centreCropToSquare(Bitmap b) {
        int w = b.getWidth();
        int h = b.getHeight();
        int side = Math.min(w, h);
        int x = (w - side) / 2;
        int y = (h - side) / 2;
        if (side == w && side == h) return b;
        return Bitmap.createBitmap(b, x, y, side, side);
    }

    private static void recycle(Bitmap b) {
        if (b != null && !b.isRecycled()) b.recycle();
    }
}
