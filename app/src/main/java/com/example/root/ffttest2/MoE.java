package com.example.root.ffttest2;

import android.graphics.Bitmap;

/**
 * MoE gating network — rule-based, no ML, hard single-expert selection per message.
 *
 * Implements the five layers from the design reference
 * ("MoE_Gating_Network_Architecture.docx"):
 *
 *   Layer 1  sensor fusion      → {@link ContextVector}, {@link QualityHistory}
 *   Layer 2  message priority   → {@link #route} emergency bypass
 *   Layer 3  hard veto checks   → turbidity veto, NACK penalty windows
 *   Layer 4  scoring engine     → weighted argmax over the two experts
 *   Layer 5  fallback           → {@link #recordResult} feeds the penalty windows
 *
 * All thresholds and weights live in {@link RouterConfig} so they can be tuned
 * without touching the routing logic. Every decision is deterministic and logged,
 * so a diver can see exactly why a medium was chosen.
 */
public final class MoE {

    private MoE() {}

    // -----------------------------------------------------------------------
    // Per-expert state. The router is effectively a singleton service: one
    // QualityHistory per medium plus its penalty-window expiry.
    // -----------------------------------------------------------------------
    private static final QualityHistory acousticHistory = new QualityHistory();
    private static final QualityHistory opticalHistory = new QualityHistory();

    private static volatile long acousticPenaltyUntil = 0L;
    private static volatile long opticalPenaltyUntil = 0L;

    /** Message priority classes (Layer 2). */
    public enum MessageType {
        /** Bypasses all scoring and broadcasts on both channels. */
        CRITICAL,
        /** Pre-coded gesture: full scoring plus an optical bonus. */
        GESTURE,
        /** Telemetry or text: full scoring, no override. */
        NORMAL
    }

    /** Outcome of one routing decision, including why it was reached. */
    public static final class Decision {
        public final Constants.CommMedium medium;
        public final boolean broadcastBoth;
        public final boolean highUncertainty;
        public final float acousticScore;
        public final float opticalScore;
        public final String reason;

        Decision(Constants.CommMedium medium, boolean broadcastBoth, boolean highUncertainty,
                 float acousticScore, float opticalScore, String reason) {
            this.medium = medium;
            this.broadcastBoth = broadcastBoth;
            this.highUncertainty = highUncertainty;
            this.acousticScore = acousticScore;
            this.opticalScore = opticalScore;
            this.reason = reason;
        }

        @Override
        public String toString() {
            return "MoE -> " + (broadcastBoth ? "BOTH" : medium)
                    + " [W_ac=" + acousticScore + " W_op=" + opticalScore + "] " + reason;
        }
    }

    // =======================================================================
    // Layer 3 + 4 + 2: the routing decision itself.
    // =======================================================================

    /**
     * Run the full gating decision for one outgoing message.
     *
     * @param userPreference the medium the user selected in the UI. This is the
     *                       tie-breaker of last resort only; the environment
     *                       decides, per the design doc.
     * @param type           message priority class (Layer 2).
     */
    public static Decision decide(Constants.CommMedium userPreference, MessageType type) {
        ContextVector ctx = ContextVector.current();
        float turbidity = ctx.turbidityScore;
        float noise = ctx.noiseScore;
        long now = System.currentTimeMillis();

        // ---- Layer 2: message priority ----------------------------------
        // A CRITICAL message bypasses vetoes and scoring entirely and goes out
        // on both channels; reliability matters more than efficiency.
        if (type == MessageType.CRITICAL) {
            Utils.log("MoE: CRITICAL message, bypassing scoring and broadcasting on both channels");
            return new Decision(userPreference, true, false, -1f, -1f,
                    "emergency broadcast (Layer 2 override)");
        }

        // ---- Layer 3: hard veto checks -----------------------------------
        boolean opticalVetoed = false;
        StringBuilder vetoReason = new StringBuilder();

        // 6.1 Turbidity threshold: above the veto, light cannot reach the
        // receiver reliably no matter how bright the LED is.
        if (turbidity > RouterConfig.TURBIDITY_VETO_THRESHOLD) {
            opticalVetoed = true;
            vetoReason.append("turbidity ").append(fmt(turbidity))
                      .append(" > ").append(fmt(RouterConfig.TURBIDITY_VETO_THRESHOLD)).append("; ");
        }

        // 6.2 Optical NACK penalty window: catches failures the camera cannot
        // see, e.g. night diving where the water is clear but pulses are not
        // detectable against the background.
        if (opticalHistory.nackRate() > RouterConfig.NACK_PENALTY_THRESHOLD) {
            opticalPenaltyUntil = now + RouterConfig.PENALTY_WINDOW_SECONDS * 1000L;
            opticalHistory.reset();
        }
        if (now < opticalPenaltyUntil) {
            opticalVetoed = true;
            vetoReason.append("optical penalty window ").append(secondsLeft(opticalPenaltyUntil)).append("s; ");
        }

        // 6.3 Acoustic NACK penalty window. Both conditions must hold: a high
        // NACK rate alone can be transient, and high noise alone does not
        // guarantee failure because acoustic filtering exists.
        boolean acousticVetoed = false;
        if (acousticHistory.nackRate() > RouterConfig.NACK_PENALTY_THRESHOLD
                && noise > RouterConfig.ACOUSTIC_NOISE_VETO_THRESHOLD) {
            acousticPenaltyUntil = now + RouterConfig.PENALTY_WINDOW_SECONDS * 1000L;
            acousticHistory.reset();
        }
        if (now < acousticPenaltyUntil) {
            acousticVetoed = true;
            vetoReason.append("acoustic penalty window ").append(secondsLeft(acousticPenaltyUntil)).append("s; ");
        }

        // ---- Layer 4: scoring engine -------------------------------------
        float wAcoustic = RouterConfig.ACOUSTIC_NOISE_WEIGHT * (1f - noise)
                + RouterConfig.ACOUSTIC_SNR_WEIGHT * (acousticHistory.lastSnr() / 100f)
                + RouterConfig.ACOUSTIC_NACK_WEIGHT * (1f - acousticHistory.nackRate());

        float wOptical = RouterConfig.OPTICAL_TURBIDITY_WEIGHT * (1f - turbidity)
                + RouterConfig.OPTICAL_SNR_WEIGHT * (opticalHistory.lastSnr() / 100f)
                + RouterConfig.OPTICAL_NACK_WEIGHT * (1f - opticalHistory.nackRate());

        // 7.3 Hard argmax, with the gesture bonus applied to optical first.
        if (type == MessageType.GESTURE && wOptical > 0.4f && !opticalVetoed) {
            wOptical += RouterConfig.GESTURE_OPTICAL_BONUS;
        }

        // A vetoed expert scores 0.0 regardless of what the formula produced.
        if (opticalVetoed) wOptical = 0f;
        if (acousticVetoed) wAcoustic = 0f;

        Constants.CommMedium chosen;
        boolean highUncertainty = false;
        String reason;

        if (opticalVetoed && acousticVetoed) {
            // Degraded mode: both unusable. Fall back to acoustic, which is the
            // more reliable default in the primary (murky) environment, and
            // raise the uncertainty flag rather than failing silently.
            chosen = Constants.CommMedium.ACOUSTIC;
            highUncertainty = true;
            reason = "both experts vetoed, acoustic by default with high-uncertainty flag; " + vetoReason;
        } else if (opticalVetoed) {
            chosen = Constants.CommMedium.ACOUSTIC;
            reason = "optical vetoed; " + vetoReason;
        } else if (acousticVetoed) {
            chosen = Constants.CommMedium.OPTICAL;
            reason = "acoustic vetoed; " + vetoReason;
        } else if (Math.abs(wAcoustic - wOptical) < RouterConfig.TIE_EPSILON) {
            // Ties break in favour of acoustic: the more reliable default.
            chosen = Constants.CommMedium.ACOUSTIC;
            reason = "scores tied within " + RouterConfig.TIE_EPSILON + ", acoustic wins the tie";
        } else if (wOptical > wAcoustic) {
            chosen = Constants.CommMedium.OPTICAL;
            reason = "optical scored higher";
        } else {
            chosen = Constants.CommMedium.ACOUSTIC;
            reason = "acoustic scored higher";
        }

        Decision d = new Decision(chosen, false, highUncertainty, wAcoustic, wOptical, reason);
        Utils.log("MoE decide: " + d
                + " | turbidity=" + fmt(turbidity) + " noise=" + fmt(noise)
                + " nackA=" + fmt(acousticHistory.nackRate()) + "/" + acousticHistory.size()
                + " snrA=" + acousticHistory.lastSnr()
                + " nackO=" + fmt(opticalHistory.nackRate()) + "/" + opticalHistory.size()
                + " snrO=" + opticalHistory.lastSnr());
        return d;
    }

    /**
     * Backwards-compatible entry point used by the send path. Resolves the
     * medium for a normal-priority image message.
     */
    public static Constants.CommMedium route(Constants.CommMedium userSelection) {
        return decide(userSelection, MessageType.NORMAL).medium;
    }

    // =======================================================================
    // Layer 5: feedback from the experts.
    // =======================================================================

    /**
     * Record the outcome of a transmission so the router's NACK rate, SNR and
     * penalty windows stay current.
     *
     * @param success whether the transmission was acknowledged.
     * @param snr     SNR reported by the expert, 0-100 (clamped).
     * @param medium  which expert carried the message.
     */
    public static void recordResult(boolean success, int snr, Constants.CommMedium medium) {
        if (medium == null) return;
        QualityHistory h = (medium == Constants.CommMedium.OPTICAL) ? opticalHistory : acousticHistory;
        h.record(success, Math.min(100, Math.max(0, snr)));
        Utils.log("MoE feedback: medium=" + medium + " ack=" + success + " snr=" + snr
                + " nackRate=" + fmt(h.nackRate()) + " consecNacks=" + h.consecutiveNacks());

        // 8.1 Penalty window on consecutive NACKs, independent of the rolling
        // rate check above.
        if (h.consecutiveNacks() >= RouterConfig.CONSECUTIVE_NACK_LIMIT) {
            long until = System.currentTimeMillis() + RouterConfig.PENALTY_WINDOW_SECONDS * 1000L;
            if (medium == Constants.CommMedium.OPTICAL) {
                opticalPenaltyUntil = until;
            } else {
                acousticPenaltyUntil = until;
            }
            Utils.log("MoE: " + medium + " locked out for "
                    + RouterConfig.PENALTY_WINDOW_SECONDS + "s after "
                    + h.consecutiveNacks() + " consecutive NACKs");
        }
    }

    /** Convenience overload for callers that have no SNR measurement. */
    public static void recordResult(boolean success, Constants.CommMedium medium) {
        recordResult(success, success ? 50 : 0, medium);
    }

    /** True while the given medium is inside its penalty window. */
    public static boolean isPenalised(Constants.CommMedium medium) {
        long until = (medium == Constants.CommMedium.OPTICAL) ? opticalPenaltyUntil : acousticPenaltyUntil;
        return System.currentTimeMillis() < until;
    }

    /** Manually clear a penalty window (the UI override from section 8.1). */
    public static void clearPenalty(Constants.CommMedium medium) {
        if (medium == Constants.CommMedium.OPTICAL) {
            opticalPenaltyUntil = 0L;
            opticalHistory.reset();
        } else {
            acousticPenaltyUntil = 0L;
            acousticHistory.reset();
        }
        Utils.log("MoE: penalty window cleared for " + medium);
    }

    /** Wipe all routing history; used when a new experiment session starts. */
    public static void reset() {
        acousticHistory.reset();
        opticalHistory.reset();
        acousticPenaltyUntil = 0L;
        opticalPenaltyUntil = 0L;
    }

    /** Short status line for the UI. */
    public static String getStatusString() {
        ContextVector ctx = ContextVector.current();
        return "turb " + fmt(ctx.turbidityScore)
                + " | noise " + fmt(ctx.noiseScore)
                + " | nackA " + fmt(acousticHistory.nackRate())
                + " | nackO " + fmt(opticalHistory.nackRate())
                + (isPenalised(Constants.CommMedium.ACOUSTIC) ? " | ACOUSTIC LOCKED" : "")
                + (isPenalised(Constants.CommMedium.OPTICAL) ? " | OPTICAL LOCKED" : "");
    }

    private static long secondsLeft(long until) {
        return Math.max(0L, (until - System.currentTimeMillis()) / 1000L);
    }

    private static String fmt(float v) {
        return String.format(java.util.Locale.US, "%.2f", v);
    }
}
