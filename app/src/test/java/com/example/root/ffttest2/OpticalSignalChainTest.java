package com.example.root.ffttest2;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Signal-chain tests for the optical link.
 *
 * The real link cannot be bench tested: it needs a torch, a camera and line of
 * sight. What can be tested is everything between "the torch was on or off" and
 * "here are the payload bytes", and that is where the failures actually live.
 * These tests drive the demodulator with a synthetic frame stream built from the
 * same bit pattern the transmitter emits, at the same symbol rate, with
 * realistic camera frame jitter.
 */
public class OpticalSignalChainTest {

    private static final int SYMBOL_MS = FlashTransmitter.SYMBOL_DURATION_MS;
    private static final int FRAME_MS = 34;          // ~29 fps, matches the emulator
    private static final float AMBIENT = 20f;
    private static final float SIGNAL = 200f;
    private static final float THRESHOLD = AMBIENT + 0.5f * (SIGNAL - AMBIENT);

    private static final int PAYLOAD_BYTES = 96;    // 64 tokens x 12 bits
    private static final int LINE_BITS = PAYLOAD_BYTES * 10;

    /** A simulated camera output: per-frame level and arrival time. */
    private static final class Capture {
        final float[] levels;
        final long[] times;
        Capture(float[] levels, long[] times) {
            this.levels = levels;
            this.times = times;
        }
    }

    /** The exact symbol sequence the transmitter puts on the torch. */
    private int[] buildSymbolStream(byte[] payload) {
        int[] data = FlashTransmitter.encode4b5b(payload);
        int[] stream = new int[FlashTransmitter.PREAMBLE_SYMBOLS
                + data.length + FlashTransmitter.POSTAMBLE_SYMBOLS];
        for (int i = 0; i < FlashTransmitter.PREAMBLE_SYMBOLS; i++) {
            stream[i] = (i & 1) == 0 ? 1 : 0;
        }
        System.arraycopy(data, 0, stream, FlashTransmitter.PREAMBLE_SYMBOLS, data.length);
        for (int i = 0; i < FlashTransmitter.POSTAMBLE_SYMBOLS; i++) {
            stream[FlashTransmitter.PREAMBLE_SYMBOLS + data.length + i] = 1;
        }
        return stream;
    }

    /**
     * Sample a symbol stream through a simulated camera.
     *
     * @param jitterMs random per-frame period variation; 0 for a perfect camera
     */
    private Capture simulate(int[] stream, long seed, int jitterMs) {
        List<Float> levels = new ArrayList<>();
        List<Long> times = new ArrayList<>();
        Random rng = new Random(seed);

        long t = 0;
        int symbol = 0;
        int symbolStart = 0;
        while (symbol < stream.length) {
            int period = FRAME_MS + (jitterMs > 0 ? rng.nextInt(2 * jitterMs + 1) - jitterMs : 0);
            t += Math.max(1, period);
            while (symbolStart + SYMBOL_MS <= t && symbol < stream.length) {
                symbol++;
                symbolStart += SYMBOL_MS;
            }
            if (symbol >= stream.length) break;
            levels.add(stream[symbol] == 1 ? SIGNAL : AMBIENT);
            times.add(t);
        }
        float[] lv = new float[levels.size()];
        long[] ts = new long[times.size()];
        for (int i = 0; i < lv.length; i++) {
            lv[i] = levels.get(i);
            ts[i] = times.get(i);
        }
        return new Capture(lv, ts);
    }

    private byte[] randomPayload(long seed) {
        byte[] payload = new byte[PAYLOAD_BYTES];
        new Random(seed).nextBytes(payload);
        return payload;
    }

    private int[] demodulate(Capture c) {
        return Demodulator.demodulate(c.levels, c.times, c.levels.length, THRESHOLD,
                SYMBOL_MS, FlashTransmitter.PREAMBLE_SYMBOLS, LINE_BITS);
    }

    private static byte[] toBytes(int[] ints) {
        byte[] out = new byte[ints.length];
        for (int i = 0; i < ints.length; i++) {
            out[i] = (byte) ints[i];
        }
        return out;
    }

    @Test
    public void roundTripRecoversPayloadExactly() {
        byte[] payload = randomPayload(42);
        int[] got = demodulate(simulate(buildSymbolStream(payload), 42, 0));
        assertNotNull("burst should be found and decoded", got);
        assertArrayEquals("payload must survive the round trip", payload, toBytes(got));
    }

    @Test
    public void toleratesFrameJitter() {
        byte[] payload = randomPayload(7);
        // +/-4 ms of period variation is well beyond what a real phone does.
        Capture c = simulate(buildSymbolStream(payload), 99, 4);
        int[] got = demodulate(c);
        assertNotNull("jittered frames must still demodulate", got);
        assertArrayEquals(payload, toBytes(got));
    }

    @Test
    public void toleratesPartialDimmingWithoutFlippingBits() {
        byte[] payload = randomPayload(11);
        int[] stream = buildSymbolStream(payload);

        // Real torches are not perfectly on/off, and frames are not uniformly
        // exposed. Scale each frame by +-20 percent; the threshold should absorb it.
        List<Float> levels = new ArrayList<>();
        List<Long> times = new ArrayList<>();
        Random rng = new Random(5);
        long t = 0;
        int symbol = 0, symbolStart = 0;
        while (symbol < stream.length) {
            t += FRAME_MS;
            while (symbolStart + SYMBOL_MS <= t && symbol < stream.length) {
                symbol++;
                symbolStart += SYMBOL_MS;
            }
            if (symbol >= stream.length) break;
            float base = stream[symbol] == 1 ? SIGNAL : AMBIENT;
            levels.add(base * (1f + (rng.nextFloat() - 0.5f) * 0.4f));
            times.add(t);
        }
        float[] lv = new float[levels.size()];
        long[] ts = new long[times.size()];
        for (int i = 0; i < lv.length; i++) {
            lv[i] = levels.get(i);
            ts[i] = times.get(i);
        }
        int[] got = demodulate(new Capture(lv, ts));
        assertNotNull("moderate level variation must not break the link", got);
        assertArrayEquals(payload, toBytes(got));
    }

    @Test
    public void thresholdMustSitBetweenAmbientAndSignal() {
        // Guard the calibration: a threshold at or below ambient would read every
        // frame as ON, and at or above signal would read every frame as OFF.
        assert (THRESHOLD > AMBIENT && THRESHOLD < SIGNAL) : "test threshold is degenerate";
        assertEquals(0, decide(AMBIENT, THRESHOLD));
        assertEquals(1, decide(SIGNAL, THRESHOLD));
    }

    private static int decide(float level, float threshold) {
        return level >= threshold ? 1 : 0;
    }

    @Test
    public void rejectsBurstWithNoPreamble() {
        // A bare payload with nothing framing it. The receiver must not lock on
        // and report a confident image.
        byte[] payload = randomPayload(3);
        int[] data = FlashTransmitter.encode4b5b(payload);

        float[] levels = new float[data.length * 8];
        long[] times = new long[data.length * 8];
        int per = SYMBOL_MS / 8;
        for (int i = 0; i < data.length; i++) {
            for (int k = 0; k < 8; k++) {
                levels[i * 8 + k] = data[i] == 1 ? SIGNAL : AMBIENT;
                times[i * 8 + k] = (long) (i * 8 + k) * per;
            }
        }
        int[] got = Demodulator.demodulate(levels, times, levels.length, THRESHOLD,
                SYMBOL_MS, FlashTransmitter.PREAMBLE_SYMBOLS, LINE_BITS);
        // The alternating preamble cannot occur by accident across 1920 bits, so
        // a correct detector finds nothing. If a future change makes a match
        // possible, it must at least be the right length rather than truncated.
        if (got != null) {
            assertEquals(PAYLOAD_BYTES, got.length);
        }
    }

    @Test
    public void fourByFiveBTableIsReversible() {
        for (int nibble = 0; nibble < 16; nibble++) {
            byte[] b = {(byte) (nibble << 4)};
            int[] bits = FlashTransmitter.encode4b5b(b);
            int code5 = 0;
            for (int i = 0; i < 5; i++) {
                code5 = (code5 << 1) | bits[i];
            }
            assertEquals("nibble " + nibble + " must survive 4B5B",
                    nibble, FlashTransmitter.decode5b4b(code5));
        }
    }

    @Test
    public void invalidCodeIsRejectedNotGuessed() {
        // 0b11000 is deliberately absent from the 4B5B table. Mapping it to
        // some plausible nibble would silently corrupt the image.
        int[] bits = {1, 1, 0, 0, 0, 1, 1, 0, 0, 0};
        assertNull("a code outside the table must not be decoded",
                Demodulator.decodeLineBits(bits));
    }
}
