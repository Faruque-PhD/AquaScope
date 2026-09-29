package com.example.root.ffttest2;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Layer 1 of the MoE gating network: the shared Context Vector.
 *
 * Holds the two continuously-computed normalised sensor scores consumed by the
 * router. Written by the sensor thread (every 500 ms for turbidity, continuously
 * for the rolling noise buffer) and read by the router thread on every outgoing
 * message, so it is published through an {@link AtomicReference} to give a
 * consistent snapshot without locking the routing path.
 */
public final class ContextVector {
    /** turbidity score in [0,1]; 1.0 means the water is opaque to light. */
    public final float turbidityScore;
    /** noise score in [0,1]; 1.0 means the water column is saturated with noise. */
    public final float noiseScore;
    /** mean luminance of the analysed centre crop in [0,1]; low values mean darkness. */
    public final float meanLuminance;
    /** raw variance-of-Laplacian sharpness, kept for diagnostics and calibration. */
    public final float laplacianVariance;

    public ContextVector(float turbidityScore, float noiseScore, float meanLuminance, float laplacianVariance) {
        this.turbidityScore = clamp01(turbidityScore);
        this.noiseScore = clamp01(noiseScore);
        this.meanLuminance = clamp01(meanLuminance);
        this.laplacianVariance = laplacianVariance;
    }

    private static volatile ContextVector current = new ContextVector(0f, 0f, 0f, 0f);

    public static void publish(ContextVector v) {
        current = v;
    }

    public static ContextVector current() {
        return current;
    }

    static float clamp01(float v) {
        if (Float.isNaN(v)) return 0f;
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }
}
