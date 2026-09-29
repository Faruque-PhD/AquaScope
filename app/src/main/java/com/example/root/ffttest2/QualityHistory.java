package com.example.root.ffttest2;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Layer 1 (expert feedback) of the MoE gating network: a per-expert ring buffer of
 * the last N transmission outcomes.
 *
 * Each entry is an ACK/NACK flag plus the SNR reported by the expert for that
 * transmission. The router reads two derived values:
 *   nack_rate  = NACK count in the window / window size
 *   last_snr   = SNR of the most recent ACK (0 when there is none)
 *
 * A rolling window is used rather than a cumulative average because underwater
 * conditions change fast: a cumulative average would smooth recent failures into
 * historical successes and hide the degradation the router is meant to react to.
 *
 * Instances are per-medium and accessed from the feedback thread (writer) and the
 * router thread (reader), so every mutation is synchronised on the instance.
 */
public class QualityHistory {
    /** Window size: the last 10 transmissions per expert, per the design doc. */
    public static final int WINDOW = 10;

    private static final class Entry {
        final boolean ack;
        final int snr;
        Entry(boolean ack, int snr) {
            this.ack = ack;
            this.snr = snr;
        }
    }

    private final Deque<Entry> window = new ArrayDeque<>(WINDOW);
    private int consecutiveNacks = 0;
    private int lastSnrValue = 0;

    /** Record the outcome of one transmission attempt. */
    public synchronized void record(boolean ack, int snr) {
        if (!ack) {
            consecutiveNacks++;
        } else {
            consecutiveNacks = 0;
            lastSnrValue = Math.max(0, snr);
        }
        window.addLast(new Entry(ack, Math.max(0, snr)));
        while (window.size() > WINDOW) {
            window.removeFirst();
        }
    }

    /** Number of NACKs inside the current window. */
    public synchronized int nackCount() {
        int n = 0;
        for (Entry e : window) {
            if (!e.ack) n++;
        }
        return n;
    }

    /**
     * Fraction of the window that failed. Reported relative to the number of
     * entries actually recorded so a partially filled window does not look
     * artificially reliable; an empty window yields 0.
     */
    public synchronized float nackRate() {
        if (window.isEmpty()) return 0f;
        return (float) nackCount() / (float) window.size();
    }

    /** SNR reported by the most recent successful transmission, or 0 if none. */
    public synchronized int lastSnr() {
        return lastSnrValue;
    }

    /** Number of transmissions recorded so far (capped at the window size). */
    public synchronized int size() {
        return window.size();
    }

    public synchronized int consecutiveNacks() {
        return consecutiveNacks;
    }

    /** Clears the penalty-relevant counters after an expert is locked out. */
    public synchronized void reset() {
        window.clear();
        consecutiveNacks = 0;
        lastSnrValue = 0;
    }
}
