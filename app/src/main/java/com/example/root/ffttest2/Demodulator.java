package com.example.root.ffttest2;

import java.util.Arrays;

/**
 * Pure signal processing for the optical link: OOK demodulation, 4B5B decoding.
 *
 * Split out of {@link OpticalReceiver} deliberately. The receiver class is bound
 * to CameraX and Android image formats, which makes it untestable without a
 * device. Everything that decides "was that bit a 1?" lives here and takes plain
 * arrays, so the whole chain can be exercised against a synthetic frame stream
 * with known ground truth.
 *
 * A symbol is decided by averaging the samples inside its time window, which is
 * a majority vote that tolerates the dropped and duplicated frames a phone camera
 * produces under load.
 */
final class Demodulator {

    private Demodulator() {}

    /**
     * Find the burst, lock onto its symbol grid, then read the payload.
     *
     * Timing recovery is the crux of this link, and the frame rate makes the
     * obvious approaches wrong.
     *
     * Chaining windows frame to frame accumulates rounding error until a window
     * straddles two symbols, so the grid is anchored on absolute time from the
     * burst start instead.
     *
     * The transmitter's actual symbol cadence is recovered: the preamble's
     * rising edges give the true period to within a couple of milliseconds, and
     * a fine period sweep at sub-millisecond resolution finishes the job. The
     * real cadence is the configured sleep plus the torch toggle and scheduler
     * latency (measured 204.7 ms against a nominal 200 ms), a 'rate error'
     * rather than an offset, so a fixed grid alone can never land on it. The
     * 4B5B code table grades every candidate: a misaligned grid turns valid
     * code words into invalid ones, so the table acts as a checksum over the
     * whole payload, and among candidates that pass it fully the most-centered
     * grid wins (a near-miss grid can read full-valid yet sit partly over a
     * symbol boundary, so validity alone is not enough). A lock that reads the
     * preamble but still yields invalid code is discarded rather than returned
     * as a plausible wrong image.
     *
     * Each symbol is sampled over its middle 60 percent, keeping samples that
     * land near a boundary out of both symbols it touches.
     *
     * @param samples     per-frame luminance in arrival order
     * @param times       arrival timestamps in ms, parallel to samples
     * @param count       number of valid entries
     * @param threshold   decision threshold; a symbol is ON if its mean >= this
     * @param symbolMs    nominal symbol duration
     * @param preambleLen expected preamble length in symbols
     * @param expectedBits payload length in line bits
     * @return payload bytes, or null if the burst was not found or was short
     */
    static int[] demodulate(float[] samples, long[] times, int count, float threshold,
                            int symbolMs, int preambleLen, int expectedBits) {
        if (count < preambleLen || threshold <= 0f) return null;

        // A receiver re-arm (burst close or medium switch) restarts the capture
        // from an empty buffer, so a shrinking count marks a new burst. The fine
        // sweep is throttled by wall clock, but a burst that starts before the
        // previous burst's throttle expired must still be allowed its first
        // sweep; reset the throttle here instead of holding back a whole burst.
        if (count < lastDemodCount - 2) {
            lastFineSweepTime = -10_000;
        }
        lastDemodCount = count;

        int start = findBurstStart(samples, count, threshold);
        if (start < 0) return null;

        // A transition can only be seen at the next frame, so the burst start is
        // at most one frame early. Search one frame either side of it.
        long nominalFrame = estimateFramePeriod(times, count);
        int groups = expectedBits / 5;
        Candidate best = new Candidate();
        best.score = -1;
        best.period = symbolMs;
        best.phase = times[start];
        lastFittedPeriodMs = 0;
        lastFittedPhaseMs = 0;
        lastFittedScore = -1;

        // Phase is not the only unknown the grid has to resolve. The nominal
        // symbol period is whatever this device was configured with, and that is
        // only guaranteed equal to the transmitter's when the two ends picked the
        // same setting. They normally will, but the whole point of the shared
        // grid is that they do not have to: the transmitter is only ever allowed
        // to send at a duration from that grid, so trying every grid entry and
        // keeping whichever one the 4B5B table scores best recovers the payload
        // even when the ends disagree. The table is the arbiter either way - a
        // wrong grid turns valid code words into invalid ones - and a correct
        // grid scores 100%, so this cannot steal a decode from a matching pair.
        // The nominal is tried first so the common case exits with one period.
        for (int candMs : candidateSymbolPeriodsMs(symbolMs)) {
            double period = candMs;
            for (long offset = -nominalFrame; offset <= nominalFrame; offset += PHASE_STEP_MS) {
                double phase = times[start] + offset;
                consider(samples, times, count, threshold, phase, period,
                        preambleLen, expectedBits, groups, best);
            }
            // A fully valid payload is conclusive; do not keep paying for the
            // remaining periods on every frame of a long burst.
            if (best.score >= groups) break;
        }

        // The integer grid cannot land on a cadence that is not a round number,
        // and the transmitter's real cadence almost never is. Each symbol is the
        // nominal sleep plus the torch toggle call and scheduler latency, which
        // measured 204.7 ms against a nominal 200 ms (2.3%). Over a 984-symbol
        // burst 2.3% is 23 symbols of drift, so even the correct phase fails the
        // grid search. The preamble's rising edges give the true period to
        // within a couple of milliseconds; a fine sweep around it (which also
        // covers the nominal) closes the residual. The window is sized by the
        // sleep skew this device could produce, not by the fit's own accuracy,
        // so a failed fit (e.g. a preamble that does not alternate because the
        // exposure pumps) still leaves the nominal reachable.
        double[] preambleFit = estimatePreamblePeriod(samples, times, count, threshold,
                start, preambleLen, symbolMs);
        double fineLo = Math.min(symbolMs, preambleFit != null ? preambleFit[1] : symbolMs)
                - FINE_WINDOW_MARGIN_MS;
        double fineHi = Math.max(symbolMs, preambleFit != null ? preambleFit[1] : symbolMs)
                + FINE_WINDOW_MARGIN_MS;

        // The sweep only makes sense once the whole payload is in the buffer -
        // until then every candidate grid reads partially and scores ~randomly -
        // and it is the most expensive thing done per frame, so it is throttled
        // to once a second. After capture the data never changes, so repeated
        // sweeps are pure repeated guesses about the same buffer.
        //
        // Completion is judged on the nominal cadence, not the fitted one. A
        // transmitter that runs long of nominal (the real case) means the actual
        // payload arrives a little later than nominal predicts, but for a
        // matched pair the sweep then opens a couple of seconds early and simply
        // keeps re-trying until the data arrives; a fit that the preamble scan
        // got wrong must not be able to hold the gate shut forever.
        boolean payloadCaptured = times[start]
                + (long) ((preambleLen + expectedBits) * symbolMs + 2 * nominalFrame)
                <= times[count - 1];
        if (best.score < groups && payloadCaptured) {
            long now = System.currentTimeMillis();
            if (now - lastFineSweepTime > FINE_SWEEP_INTERVAL_MS) {
                lastFineSweepTime = now;
                // Wide pass: step the period at 0.05 ms over the search window,
                // phases within a few ms of the current best. The phase only has
                // to land inside the wide band where the preamble alternates, so
                // every well-centered (phase, period) pair scores full validity;
                // the centeredness tiebreak then prefers the pair whose windows
                // sit truly mid-symbol over a near-miss grid that reads valid
                // but wrong (single-bit flips on a 5-bit word map to another
                // valid word half the time, so validity alone accepts corruption).
                for (double period = fineLo; period <= fineHi; period += FINE_PERIOD_STEP_MS) {
                    for (double phase = best.phase - FINE_PHASE_SPAN_MS;
                         phase <= best.phase + FINE_PHASE_SPAN_MS;
                         phase += FINE_PHASE_STEP_MS) {
                        consider(samples, times, count, threshold, phase, period,
                                preambleLen, expectedBits, groups, best);
                    }
                    if (best.score >= groups) break;
                }

                // Refine pass: tighten the step around the wide-pass winner.
                // Whoever won, a grid accurate to ~0.01 ms drifts well under a
                // symbol window across the whole burst, so this both locks the
                // center and confirms the content is decided safely away from
                // boundaries.
                for (double period = best.period - FINE_REFINE_SPAN_MS;
                     period <= best.period + FINE_REFINE_SPAN_MS;
                     period += FINE_PERIOD_STEP_REFINE_MS) {
                    for (double phase = best.phase - FINE_PHASE_SPAN_MS / 2;
                         phase <= best.phase + FINE_PHASE_SPAN_MS / 2;
                         phase += FINE_PHASE_STEP_MS / 2) {
                        consider(samples, times, count, threshold, phase, period,
                                preambleLen, expectedBits, groups, best);
                    }
                    if (best.score >= groups && best.centered >= 1.0) break;
                }

                if (best.score > 0 || lastFittedScore < 0) {
                    Utils.log("Demodulator: period fit P=" + String.format("%.2f", best.period)
                            + "ms phase=" + (long) best.phase + " score=" + best.score + "/" + groups
                            + " centered=" + String.format("%.3f", best.centered));
                }
            }
        }

        lastFittedPeriodMs = best.period;
        lastFittedPhaseMs = best.phase;
        lastFittedScore = best.score;

        if (best.bits == null || best.score < groups) return null;
        return decodeLineBits(best.bits);
    }

    /**
     * A candidate grid and its quality measures.
     */
    private static final class Candidate {
        int[] bits;
        int score;
        double centered = -1;
        double period;
        double phase;
    }

    /** Capture length of the previous demodulate call, for burst detection. */
    private static int lastDemodCount = 0;

    /**
     * Score one (phase, period) pair and fold it into the best so far.
     *
     * <p>Phase, not score, is the scalar that matters once the grid is even
     * close: any phase inside the wide band where the preamble alternates reads
     * the whole payload with the same 4B5B words, because a window that sits
     * entirely inside its symbol decides identically. The centeredness measure
     * (does the inset decision agree with the full-symbol majority?) separates
     * those full-valid candidates cheaply, and it is the grid the transmitter
     * actually used - not just any grid that decodes to valid words - that wins
     * a fully valid but near-miss tie. Comparing centeredness on every partial
     * score would swamp the sweep, so it is only computed for full-valid
     * candidates, which is exactly where a wrong-but-plausible decode hurts.
     */
    private static void consider(float[] samples, long[] times, int count, float threshold,
                                 double phase, double period, int preambleLen, int expectedBits,
                                 int groups, Candidate best) {
        int[] bits = readBits(samples, times, count, threshold, phase, period,
                preambleLen, expectedBits);
        if (bits == null) return;

        int score = scoreCodeValidity(bits, expectedBits);
        if (score < best.score) return;
        if (score == best.score && best.score >= 0 && best.centered >= 0 && score < groups) {
            // Partial-score ties carry no content risk; keep the first.
            return;
        }
        double centered = score >= groups
                ? centeredness(samples, times, count, threshold, phase, period,
                        preambleLen, expectedBits)
                : -1;
        if (score > best.score
                || (score == best.score && centered > best.centered)) {
            best.bits = bits;
            best.score = score;
            best.centered = centered;
            best.period = period;
            best.phase = phase;
        }
    }

    /**
     * Fraction of symbols whose inset-window decision matches the full-window
     * majority. A grid that straddles a boundary disagrees at that symbol; a
     * centered grid agrees everywhere.
     */
    private static double centeredness(float[] samples, long[] times, int count, float threshold,
                                       double phase, double period, int preambleLen,
                                       int expectedBits) {
        int agree = 0;
        int total = 0;
        for (int k = 0; k < preambleLen + expectedBits; k++) {
            double symStart = phase + k * period;
            if (symStart + period > times[count - 1]) break;
            int full = decide(samples, times, count, symStart, symStart + period, threshold);
            int inset = decide(samples, times, count, symStart + period * SAMPLE_INSET,
                    symStart + period * (1.0 - SAMPLE_INSET), threshold);
            agree += (full == inset) ? 1 : 0;
            total++;
        }
        return total == 0 ? 0 : agree / (double) total;
    }

    /**
     * Fit the transmitter's actual symbol period from the preamble.
     *
     * <p>The transmitter sleeps for the configured period per symbol, but the
     * torch toggle call and the scheduler ride on top of each sleep, so the real
     * cadence is a few percent longer than nominal (204.7 ms measured against a
     * configured 200 ms). That matters: 2.5 % over a 984-symbol burst is ~24
     * symbols of drift, which destroys any fixed-grid decode. Fitting the
     * period from the preamble is the only way to learn it, because the payload
     * bits are unknown until it is known.
     *
     * <p>Only the preamble's rising edges (OFF to ON) are used. The torch's ON
     * to OFF tail is a falling ramp whose threshold crossing lags the true
     * boundary, while an OFF to ON step crosses promptly; any residual latency
     * that all rising edges share in the same direction cancels in the slope,
     * leaving the period bias-free. Each edge sits on a 2-symbol boundary in the
     * alternating preamble, so the regression is over (boundary, frame time)
     * pairs, and a missing edge shifts the tail's indices uniformly, which does
     * not change the slope.
     *
     * <p>The scan stops after a nominal-scaled preamble window (one nominal
     * period per preamble symbol). Without that bound, the scan runs into the
     * payload, whose transitions are not on the 2-symbol lattice, and a stray
     * payload boundary pulls the fit off - a wrong center the fine sweep can
     * never recover from.
     *
     * @return {phase0, period} where phase0 is the symbol-0 start time, or null
     *         when fewer than 4 usable rising edges were captured
     */
    private static double[] estimatePreamblePeriod(float[] samples, long[] times, int count,
                                                   float threshold, int start, int preambleLen,
                                                   int symbolMs) {
        int cap = preambleLen / 2 + 1;
        long[] edges = new long[cap];
        int n = 0;
        long windowEnd = times[start] + (long) preambleLen * symbolMs;
        for (int i = start + 1; i < count && n < cap; i++) {
            if (times[i] > windowEnd) break;
            if (samples[i] >= threshold && samples[i - 1] < threshold) {
                edges[n++] = times[i];
            }
        }
        if (n < 4) return null;

        // The first rising edge behind the burst start is the boundary after the
        // first OFF symbol, i.e. boundary index 2 (0-indexed from symbol 0), and
        // the preamble alternates, so every further rising edge is 2 symbols
        // later. Least squares over (boundary, time) recovers phase0 as the
        // intercept and the period as the slope.
        double sx = 0, sy = 0, sxx = 0, sxy = 0;
        for (int i = 0; i < n; i++) {
            double x = 2 + 2 * i;
            double y = edges[i];
            sx += x;
            sy += y;
            sxx += x * x;
            sxy += x * y;
        }
        double den = n * sxx - sx * sx;
        if (den == 0) return null;
        double period = (n * sxy - sx * sy) / den;
        if (period <= 0 || period > 2000) return null;
        double phase0 = (sy - period * sx) / n;
        return new double[]{phase0, period};
    }

    /**
     * Symbol periods to try, nominal first, then the shared grid, deduplicated.
     *
     * <p>The nominal is first so the common (matching) case scores full validity
     * on the first period and the grid loop above exits immediately. The grid
     * values after it cover the case where this receiver and the transmitter were
     * configured differently but both picked from {@link
     * Constants#OPTICAL_SYMBOL_MS_OPTIONS}.
     */
    private static int[] candidateSymbolPeriodsMs(int nominal) {
        boolean[] seen = new boolean[701];
        int[] out = new int[Constants.OPTICAL_SYMBOL_MS_OPTIONS.length + 1];
        int n = 0;
        if (nominal > 0 && nominal <= 700) {
            seen[nominal] = true;
            out[n++] = nominal;
        }
        for (int o : Constants.OPTICAL_SYMBOL_MS_OPTIONS) {
            if (!seen[o]) {
                seen[o] = true;
                out[n++] = o;
            }
        }
        return Arrays.copyOf(out, n);
    }

    /** Phase search resolution in ms; finer than one frame is unnecessary. */
    private static final long PHASE_STEP_MS = 2;

    /**
     * Display diagnostics for the receiver, which logs them on a failed burst.
     * Reset at the top of {@link #demodulate}.
     */
    static volatile double lastFittedPeriodMs;
    static volatile double lastFittedPhaseMs;
    static volatile int lastFittedScore;

    /** Resolution of the fine period sweep. */
    private static final double FINE_PERIOD_STEP_MS = 0.05;

    /** Polish resolution for the second (tight) pass of the fine sweep. */
    private static final double FINE_PERIOD_STEP_REFINE_MS = 0.01;

    /** Phase grid and half-span used around the current best fit. */
    private static final double FINE_PHASE_STEP_MS = 2;
    private static final double FINE_PHASE_SPAN_MS = 4;

    /** Half-span of the tight pass around the wide-pass winner. */
    private static final double FINE_REFINE_SPAN_MS = 0.6;

    /** Per-burst window margin either side of the fitted/nominal period. */
    private static final double FINE_WINDOW_MARGIN_MS = 12;

    /** How often the (expensive) fine period sweep may run, in ms. */
    private static final long FINE_SWEEP_INTERVAL_MS = 1000;

    private static volatile long lastFineSweepTime = -10_000;

    /** Fraction of each symbol skipped at its edges when sampling. */
    private static final double SAMPLE_INSET = 0.20;

    /**
     * Median inter-frame interval, used to bound the phase search.
     *
     * The median is used rather than the mean because the first frame after a
     * burst boundary can be late, and a mean would be skewed by it.
     */
    private static long estimateFramePeriod(long[] times, int count) {
        if (count < 3) return 0;
        long[] gaps = new long[count - 1];
        for (int i = 1; i < count; i++) {
            gaps[i - 1] = times[i] - times[i - 1];
        }
        Arrays.sort(gaps);
        return gaps[gaps.length / 2];
    }

    /**
     * Read preamble and payload off a fixed grid.
     *
     * @return the payload bits, or null if the preamble does not alternate or the
     *         burst is too short to contain the whole payload
     */
    private static int[] readBits(float[] samples, long[] times, int count, float threshold,
                                  double phase, double period, int preambleLen,
                                  int expectedBits) {
        int[] bits = new int[expectedBits];
        for (int k = 0; k < preambleLen + expectedBits; k++) {
            double symStart = phase + k * period;
            double from = symStart + period * SAMPLE_INSET;
            double to = symStart + period * (1.0 - SAMPLE_INSET);
            if (to > times[count - 1]) {
                return null; // burst ended early
            }
            int bit = decide(samples, times, count, from, to, threshold);
            if (k < preambleLen) {
                if (bit != ((k & 1) == 0 ? 1 : 0)) return null;
            } else {
                bits[k - preambleLen] = bit;
            }
        }
        return bits;
    }

    /**
     * Number of 5-bit groups that are valid 4B5B code words.
     *
     * Used to compare candidate phases. A correct grid maps every group to a
     * defined code; a misaligned one lands in the unused codes.
     */
    private static int scoreCodeValidity(int[] bits, int expectedBits) {
        int groups = expectedBits / 5;
        int score = 0;
        for (int g = 0; g < groups; g++) {
            if (FlashTransmitter.decode5b4b(bitsAt(bits, g * 5)) >= 0) {
                score++;
            }
        }
        return score;
    }

    /** First index of a run that starts with the torch ON, or -1. */
    private static int findBurstStart(float[] samples, int count, float threshold) {
        for (int i = 0; i < count; i++) {
            if (samples[i] < threshold) continue;
            // index 0 is a valid start: the receiver may have begun sampling
            // while the first preamble symbol was already lit.
            if (i == 0) return 0;
            if (samples[i - 1] < threshold) return i;
        }
        return -1;
    }

    /**
     * Mean level over an absolute time window, thresholded.
     *
     * @return 1 for ON, 0 for OFF
     */
    private static int decide(float[] samples, long[] times, int count, double fromMs,
                              double toMs, float threshold) {
        int lo = lowerBound(times, count, (long) Math.ceil(fromMs));
        double sum = 0;
        int n = 0;
        for (int i = lo; i < count && times[i] <= toMs; i++) {
            sum += samples[i];
            n++;
        }
        if (n == 0) return 0;
        return (sum / n) >= threshold ? 1 : 0;
    }

    private static int lowerBound(long[] times, int count, long value) {
        int lo = 0, hi = count;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (times[mid] < value) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return lo;
    }

    /**
     * 4B5B decode. A 5-bit group absent from the table is a channel error, so the
     * whole burst is rejected rather than mapped to a plausible wrong nibble.
     */
    static int[] decodeLineBits(int[] bits) {
        int byteCount = bits.length / 10;
        int[] out = new int[byteCount];
        for (int b = 0; b < byteCount; b++) {
            int hi = FlashTransmitter.decode5b4b(bitsAt(bits, b * 10));
            int lo = FlashTransmitter.decode5b4b(bitsAt(bits, b * 10 + 5));
            if (hi < 0 || lo < 0) return null;
            out[b] = (hi << 4) | lo;
        }
        return out;
    }

    private static int bitsAt(int[] bits, int offset) {
        int v = 0;
        for (int i = 0; i < 5; i++) {
            v = (v << 1) | bits[offset + i];
        }
        return v;
    }
}
