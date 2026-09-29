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
     * obvious approaches both wrong.
     *
     * Chaining windows frame to frame accumulates rounding error until a window
     * straddles two symbols, so the grid is anchored on absolute time from the
     * burst start instead.
     *
     * Measuring the period from the preamble is worse than not measuring it. The
     * camera samples every ~34 ms while symbols last 100 ms, so a transition is
     * only ever seen at the next frame. The preamble therefore reports 102 ms
     * against a true 100 ms, and a proportional loop cannot detect the error
     * because the observed and predicted transitions land on the same frame
     * grid. A 2 percent period error is 19 symbols of drift by the end of a
     * 960-symbol payload, and it fails silently: windows straddle boundaries and
     * average two symbols into a level sitting near the threshold.
     *
     * So the nominal period is used, which is accurate to well under a frame,
     * and only the unknown phase is searched. The transmitter's symbol clock is
     * close to nominal and the sampling is asynchronous to it, so the residual is
     * a single offset, not a rate error. Phases are tried on a grid finer than
     * one frame and the winner is chosen by the 4B5B code table: a misaligned
     * grid turns valid code words into invalid ones, so the table acts as a
     * checksum over the whole payload and a 2 percent chance of a false accept
     * is not a concern. A lock that reads the preamble but still yields invalid
     * code is discarded rather than returned as a plausible wrong image.
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

        int start = findBurstStart(samples, count, threshold);
        if (start < 0) return null;

        // A transition can only be seen at the next frame, so the burst start is
        // at most one frame early. Search one frame either side of it.
        long nominalFrame = estimateFramePeriod(times, count);
        double period = symbolMs;
        int[] best = null;
        int bestScore = -1;

        for (long offset = -nominalFrame; offset <= nominalFrame; offset += PHASE_STEP_MS) {
            double phase = times[start] + offset;
            int[] bits = readBits(samples, times, count, threshold, phase, period,
                    preambleLen, expectedBits);
            if (bits == null) continue;

            int score = scoreCodeValidity(bits, expectedBits);
            if (score > bestScore) {
                bestScore = score;
                best = bits;
            }
        }

        int groups = expectedBits / 5;
        if (best == null || bestScore < groups) return null;
        return decodeLineBits(best);
    }

    /** Phase search resolution in ms; finer than one frame is unnecessary. */
    private static final long PHASE_STEP_MS = 2;

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
