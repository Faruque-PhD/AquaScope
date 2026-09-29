package com.example.root.ffttest2;

/**
 * Tunable configuration for the MoE gating network (section 12 of the design
 * reference). Every threshold and weight the router uses lives here so it can be
 * adjusted without touching the routing logic or recompiling the analysis code.
 */
public final class RouterConfig {
    private RouterConfig() {}

    // ---- Layer 3: veto thresholds -----------------------------------------

    /** Turbidity above which the optical expert is vetoed. Range 0.50 - 0.90. */
    public static final float TURBIDITY_VETO_THRESHOLD = 0.70f;

    /**
     * Noise level that must accompany a high acoustic NACK rate before acoustic
     * is locked out. Both conditions must hold simultaneously.
     */
    public static final float ACOUSTIC_NOISE_VETO_THRESHOLD = 0.80f;

    /** NACK rate that triggers the penalty window. Range 0.40 - 0.80. */
    public static final float NACK_PENALTY_THRESHOLD = 0.65f;

    /** Penalty window duration in seconds. Range 10 - 120. */
    public static final int PENALTY_WINDOW_SECONDS = 30;

    /** Consecutive NACKs that activate the penalty window. Range 2 - 5. */
    public static final int CONSECUTIVE_NACK_LIMIT = 3;

    // ---- Layer 4: scoring weights -----------------------------------------

    /** Weight on (1 - noise) in the acoustic score. Range 0.30 - 0.70. */
    public static final float ACOUSTIC_NOISE_WEIGHT = 0.50f;
    /** Weight on last SNR / 100 in the acoustic score. Range 0.20 - 0.50. */
    public static final float ACOUSTIC_SNR_WEIGHT = 0.30f;
    /** Weight on (1 - nack_rate) in the acoustic score. Range 0.10 - 0.30. */
    public static final float ACOUSTIC_NACK_WEIGHT = 0.20f;

    /** Weight on (1 - turbidity) in the optical score. Range 0.40 - 0.75. */
    public static final float OPTICAL_TURBIDITY_WEIGHT = 0.60f;
    /** Weight on last SNR / 100 in the optical score. Range 0.20 - 0.50. */
    public static final float OPTICAL_SNR_WEIGHT = 0.30f;
    /** Weight on (1 - nack_rate) in the optical score. Range 0.05 - 0.20. */
    public static final float OPTICAL_NACK_WEIGHT = 0.10f;

    /** Bonus added to optical for pre-coded gesture messages. Range 0.00 - 0.25. */
    public static final float GESTURE_OPTICAL_BONUS = 0.15f;

    /** Minimum optical score for the gesture bonus to apply. */
    public static final float GESTURE_BONUS_MIN_SCORE = 0.40f;

    /** Scores closer than this count as tied and break in favour of acoustic. */
    public static final float TIE_EPSILON = 0.01f;

    // ---- Layer 1: sensor fusion calibration --------------------------------

    /**
     * Laplacian variance treated as perfectly clear water. The turbidity score is
     * 1 - variance/baseline, so this is the sharpness a clear frame reaches.
     *
     * Calibrate on the actual device before any field test: capture a frame in
     * clear water at the working depth, read the logged "Turbidity: variance=..."
     * value, and set this to that number. It is resolution-dependent, which is
     * why the analysis downscale is fixed at {@link TurbidityAnalyzer}'s working
     * size rather than the preview resolution.
     *
     * The 250f default is a starting point for the 64x64 analysis crop, chosen
     * from measured values on textured imagery. It must be re-measured for any
     * device whose camera has a different noise profile; a device that logs
     * variance far above the baseline for clear water means the value is too low
     * and optical will look artificially clean.
     */
    public static final float TURBIDITY_BASELINE_VARIANCE = 250f;

    /**
     * Fraction of the baseline below which the frame is judged unusable outright
     * rather than merely turbid. Guards against a mis-set baseline silently
     * vetoing the optical channel for every frame.
     */
    public static final float TURBIDITY_FLOOR_VARIANCE = 2f;

    /**
     * Mean centre-crop luminance (0-1) at or below which the frame is treated as
     * dark. Near-total darkness is a secondary turbidity signal: silt and night
     * both collapse the frame.
     */
    public static final float DARKNESS_LUMINANCE_THRESHOLD = 0.08f;

    /** How much of the darkness signal is folded into turbidity. */
    public static final float DARKNESS_TURBIDITY_WEIGHT = 0.50f;

    /** Sensor fusion tick for the camera, in milliseconds. */
    public static long SENSOR_TICK_MS = 500L;

    /** Rolling audio window used for the FFT, in milliseconds. */
    public static long NOISE_WINDOW_MS = 200L;

    /** Quiet baseline in dBFS used to normalise the noise score. */
    public static final float QUIET_BASELINE_DB = -60f;

    /** Dynamic range in dB mapping the noise score across 0-1. */
    public static final float NOISE_DYNAMIC_RANGE_DB = 40f;

    /** Lowest frequency of the acoustic communication band, in Hz. */
    public static final double NOISE_BAND_LOW_HZ = 1000.0;
    /** Highest frequency of the acoustic communication band, in Hz. */
    public static final double NOISE_BAND_HIGH_HZ = 30000.0;
}
