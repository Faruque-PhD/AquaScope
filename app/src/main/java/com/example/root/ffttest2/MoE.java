package com.example.root.ffttest2;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * Simple rule‑based Mixture‑of‑Experts (MoE) implementation.
 *
 * An "Expert" is any object that implements the {@link Expert} functional interface –
 * a single method that receives a bitmap and returns a {@link ExpertResult}.
 *
 * The router holds a list of {@link RuleExpertPair}s. A {@link Rule} is a predicate that
 * inspects the current {@link Constants.CommMedium} and any other runtime metrics to decide
 * whether its associated expert should be used.
 *
 * For the current project we only need two experts:
 *   1. {@link OpticalExpert} – sends the bitmap via the flash‑transmitter.
 *   2. {@link AcousticExpert} – sends the bitmap via the chirp‑based acoustic channel.
 *
 * The router evaluates the rules in order and returns the first matching expert.
 */
public class MoE {
    // -----------------------------------------------------------------------
    // Rule-based routing thresholds. Coarse heuristics until the probe metrics
    // (Constants.ambientLightLux / opticalAttenuation / acousticSNR) are
    // populated by a real channel-probing step. Values > 0 are only treated as
    // valid; the defaults (0f) mean "metric not measured yet" and are ignored.
    // -----------------------------------------------------------------------
    // Optical path is considered degraded when attenuation exceed this value.
    public static final float OPTICAL_ATTENUATION_THRESHOLD = 0.8f;
    // Optical path is considered degraded below this ambient light level (lux).
    public static final float AMBIENT_LIGHT_THRESHOLD_LUX = 20f;
    // Acoustic path is considered degraded when its SNR (dB, roughly) is lower
    // than the adaptive SNR threshold used elsewhere in the system.
    public static final int ACOUSTIC_SNR_THRESHOLD = Constants.SNR_THRESH2_2;

    /**
     * Rule-based router: given the medium the user selected in the UI, decide the
     * actual medium to transmit on, taking the (optionally measured) channel
     * quality and the MoE veto buffer into account.
     *
     * @param userSelection medium selected in the UI (Constants.currentMedium).
     * @return the actual medium to use for this transmission.
     */
    public static Constants.CommMedium route(Constants.CommMedium userSelection) {
        Constants.CommMedium actual = userSelection;

        // Rule 1: user wants optical but the optical channel is degraded -> fall back to acoustic.
        if (actual == Constants.CommMedium.OPTICAL && opticalChannelDegraded()) {
            Utils.log("MoE: optical channel degraded, switching to ACOUSTIC");
            actual = Constants.CommMedium.ACOUSTIC;
        }
        // Rule 2: user wants acoustic but the acoustic channel is degraded -> switch to optical.
        else if (actual == Constants.CommMedium.ACOUSTIC && acousticChannelDegraded()) {
            Utils.log("MoE: acoustic channel degraded, switching to OPTICAL");
            actual = Constants.CommMedium.OPTICAL;
        }
        // Rule 3: the last transmission on this same medium failed (veto) -> try the other medium.
        if (MoEManager.consumeVeto(actual)) {
            actual = (actual == Constants.CommMedium.OPTICAL) ? Constants.CommMedium.ACOUSTIC : Constants.CommMedium.OPTICAL;
            Utils.log("MoE: veto consumed, switching to " + actual);
        }

        Utils.log("MoE route: user=" + userSelection + " -> actual=" + actual
                + " (ambientLux=" + Constants.ambientLightLux
                + ", opticalAttenuation=" + Constants.opticalAttenuation
                + ", acousticSNR=" + Constants.acousticSNR + ")");
        return actual;
    }

    /** True when the optical channel metrics indicate a degraded link. */
    private static boolean opticalChannelDegraded() {
        boolean attenuationDegraded = Constants.opticalAttenuation > 0f
                && Constants.opticalAttenuation > OPTICAL_ATTENUATION_THRESHOLD;
        boolean darknessDegraded = Constants.ambientLightLux > 0f
                && Constants.ambientLightLux < AMBIENT_LIGHT_THRESHOLD_LUX;
        return attenuationDegraded || darknessDegraded;
    }

    /** True when the acoustic channel metrics indicate a degraded link. */
    private static boolean acousticChannelDegraded() {
        return Constants.acousticSNR > 0f && Constants.acousticSNR < ACOUSTIC_SNR_THRESHOLD;
    }

    // -----------------------------------------------------------------------

    /** Result wrapper for an expert execution. */
    public static class ExpertResult {
        public final Bitmap bitmap;   // processed bitmap (may be same as input)
        public final String description; // human readable tag
        public final boolean success;    // whether the transmission succeeded
        public ExpertResult(Bitmap bitmap, String description, boolean success) {
            this.bitmap = bitmap;
            this.description = description;
            this.success = success;
        }
    }

    /** Functional interface for an expert. */
    @FunctionalInterface
    public interface Expert {
        ExpertResult run(Bitmap input);
    }

    /** Functional interface for a rule that decides whether an expert should be used. */
    @FunctionalInterface
    public interface Rule {
        boolean test();
    }

    /** Pair of rule + expert. */
    private static class RuleExpertPair {
        final Rule rule;
        final Expert expert;
        RuleExpertPair(Rule r, Expert e) { this.rule = r; this.expert = e; }
    }

    private final java.util.List<RuleExpertPair> router = new java.util.ArrayList<>();

    /** Register a new rule‑expert pair. */
    public void add(Rule rule, Expert expert) {
        router.add(new RuleExpertPair(rule, expert));
    }

    /** Execute the first matching expert, or throw if none match. */
    public ExpertResult execute(Bitmap input) {
        for (RuleExpertPair rep : router) {
            if (rep.rule.test()) {
                Log.d(Constants.LOG, "MoE selected expert: " + rep.expert.getClass().getSimpleName());
                return rep.expert.run(input);
            }
        }
        throw new IllegalStateException("No MoE rule matched – check configuration");
    }
}
