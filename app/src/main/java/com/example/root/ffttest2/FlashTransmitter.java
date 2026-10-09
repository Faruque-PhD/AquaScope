package com.example.root.ffttest2;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.Log;

/**
 * Utility class to transmit image embeddings using the device flash (torch) via OOK modulation.
 * The embedding is first obtained from the provided Bitmap using the existing VQGAN encoder
 * (Utils.encode_image), converted to bytes via Utils.Embedding2Bytes, line-coded using the
 * 4B5B scheme, and each resulting bit is sent as an On-Off-Keying (OOK) flash pulse.
 *
 * This is a basic prototype implementation: timing values are conservative (10 ms per symbol),
 * preceded by a short all-ON preamble so the receiver can detect the start of a burst.
 */
public class FlashTransmitter {
    private static final String TAG = "FlashTransmitter";
    // Symbol duration in milliseconds.
    //
    // The optical receiver is a phone camera, not a photodiode, so this is
    // bounded by how many frames per second the camera actually delivers:
    //
    //   samples_per_symbol = frame_rate * symbol_ms / 1000
    //
    // The demodulator decides each symbol over the middle 60 percent of its
    // samples, so it needs at least 2 frames per symbol before a single dropped
    // frame can flip a decision; 2.7-3 is where it is comfortable.
    //
    // Measured on an SM-A346E, with the periodic rate log this app now emits:
    // the camera settles at 8.7-9.1 fps and stays there. Three separate
    // attempts to raise that all failed:
    //
    //   binding only the analysis use case, dropping preview and capture
    //                               9.0 fps
    //   binding analysis alongside preview and capture
    //                               9.0 fps
    //   asking for a 320x240 analysis stream instead of 640x480
    //                               8.8 fps
    //
    // The analyzer itself takes 0.2-0.8 ms of a 112 ms frame interval, so none
    // of this is CPU. The limit is the camera's exposure cycle, which in turn
    // tracks scene brightness - which means the rate during a real burst, with
    // a torch pointed at the lens, may well be higher than it is idle in a dim
    // room. That has to be read off the log during the field test before this
    // number is lowered.
    //
    // 300 ms gives 2.7 samples per symbol at the measured 9 fps. Going to
    // 120 ms - a tempting 2.5x cut in link time - gives 1.1, which cannot
    // resolve the eye pattern at all, so it is not a usable value on this
    // hardware until the lit burst proves the camera is faster than idle.
    //
    // Cost of the 300 ms choice: a 4096-codebook image is 64 tokens = 96 bytes
    // = 960 line bits, plus a 16-symbol preamble and an 8-symbol postamble, so
    // the flash runs 984 * 300 ms = about 295 seconds.
    static final int SYMBOL_DURATION_MS = 300;
    // Burst framing. The preamble alternates so the receiver can lock symbol
    // timing on the transitions and cannot confuse it with a long run of 1s in
    // the payload. The postamble is a solid run, which is the opposite pattern
    // and therefore an unambiguous end-of-burst marker.
    static final int PREAMBLE_SYMBOLS = 16;
    static final int POSTAMBLE_SYMBOLS = 8;

    /**
     * Transmit the embedding of a bitmap via the device flash.
     *
     * Convenience wrapper for callers that do not already hold the token array.
     *
     * @param context Context used to access the CameraManager.
     * @param bitmap  Bitmap to be encoded and sent.
     * @return true if transmission completed without errors, false otherwise.
     */
    public static boolean transmitEmbedding(Context context, Bitmap bitmap) {
        if (bitmap == null) {
            Log.e(TAG, "Bitmap is null, abort transmission");
            return false;
        }
        long[] tokens = Utils.encode_image(bitmap);
        return transmitTokens(context, tokens);
    }

    /**
     * Transmit a short known pattern instead of a real image payload.
     *
     * <p>The full 4096-codebook image is 960 line bits, which at
     * {@link #SYMBOL_DURATION_MS} is a ~5 minute flash. That is far too slow to
     * iterate on the physical link, because getting a decode needs three things
     * lined up at once: the camera aimed at the torch, the torch bright enough at
     * that distance, and the receiver's threshold tracking the room light. Each
     * of those needs several tries.
     *
     * <p>This sends a fixed 4-byte pattern, so the burst is 16 preamble + 40 line
     * bits + 8 postamble = 64 symbols, about 19 seconds. The receiver decodes it
     * with exactly the same 4B5B and symbol-clock code as the image payload, so a
     * clean result here means the framing, alignment and threshold are all good
     * and only the image decode remains unproven.
     *
     * @param context Context used to access the CameraManager.
     * @return true if transmission completed without errors, false otherwise.
     */
    public static boolean transmitLinkCheck(Context context) {
        if (context == null) {
            Log.e(TAG, "Context is null, abort link check");
            return false;
        }
        try {
            byte[] pattern = LINK_CHECK_PATTERN;
            int[] bits = encode4b5b(pattern);
            Log.i(TAG, "Link check: " + pattern.length + " bytes -> " + bits.length
                    + " line bits ("
                    + (PREAMBLE_SYMBOLS + bits.length + POSTAMBLE_SYMBOLS)
                    * Constants.opticalSymbolDurationMs / 1000f
                    + "s burst at " + Constants.opticalSymbolDurationMs + "ms symbols)");
            return sendBitsViaFlash(context, bits);
        } catch (Exception e) {
            Log.e(TAG, "Exception during link check", e);
            return false;
        }
    }

    /**
     * The known bytes a link check sends. Deliberately not all zeros or all ones:
     * a uniform pattern can decode cleanly even when the threshold is drifting,
     * whereas alternating nibbles force every bit boundary to be exercised.
     */
    static final byte[] LINK_CHECK_PATTERN = {0x1A, (byte) 0xC3, 0x5A, (byte) 0xA5};

    /**
     * Transmit an already-encoded token array via the device flash.
     *
     * This is the entry point the send path uses. The tokens come from the same
     * {@link Utils#encode_image} call the acoustic path uses, so both media carry
     * the identical 12-bit token payload and the receiver reconstructs the same
     * image either way.
     *
     * @param context Context used to access the CameraManager.
     * @param tokens  token array, each entry a 12-bit codebook index.
     * @return true if transmission completed without errors, false otherwise.
     */
    public static boolean transmitTokens(Context context, long[] tokens) {
        if (context == null) {
            Log.e(TAG, "Context is null, abort transmission");
            return false;
        }
        if (tokens == null || tokens.length == 0) {
            Log.e(TAG, "No tokens to transmit");
            return false;
        }
        try {
            // 1. Tokens -> 12-bit bytes, the same step the acoustic path uses.
            byte[] embedding = Utils.Embedding2Bytes(tokens);
            if (embedding == null || embedding.length == 0) {
                Log.e(TAG, "Failed to obtain embedding bytes");
                return false;
            }
            // 2. Apply 4B5B line coding to get a run-length-friendly bit stream.
            int[] bits = encode4b5b(embedding);
            Log.i(TAG, "Transmitting " + tokens.length + " tokens -> " + embedding.length
                    + " bytes -> " + bits.length + " line bits ("
                    + (PREAMBLE_SYMBOLS + bits.length + POSTAMBLE_SYMBOLS)
                    * Constants.opticalSymbolDurationMs / 1000f
                    + "s burst at " + Constants.opticalSymbolDurationMs + "ms symbols)");
            // 3. Send preamble followed by the data bits via the torch (OOK).
            long burstStart = System.currentTimeMillis();
            boolean ok = sendBitsViaFlash(context, bits);
            if (ok) {
                long elapsed = System.currentTimeMillis() - burstStart;
                int symbols = PREAMBLE_SYMBOLS + bits.length + POSTAMBLE_SYMBOLS;
                Log.i(TAG, "Optical transmission completed: " + symbols + " symbols in " + elapsed
                        + "ms -> " + String.format("%.1f", elapsed / (double) symbols)
                        + "ms per symbol (nominal " + Constants.opticalSymbolDurationMs + "ms)");
            }
            return ok;
        } catch (Exception e) {
            Log.e(TAG, "Exception during flash transmission", e);
            return false;
        }
    }

    // ---------------------------------------------------------------------
    // 4B5B line coding – maps each 4-bit nibble to a 5-bit pattern.
    // ---------------------------------------------------------------------
    private static final int[] FOUR_B_TO_FIVE_B = {
            0b11110, // 0x0
            0b01001, // 0x1
            0b10100, // 0x2
            0b10101, // 0x3
            0b01010, // 0x4
            0b01011, // 0x5
            0b01110, // 0x6
            0b01111, // 0x7
            0b10010, // 0x8
            0b10011, // 0x9
            0b10110, // 0xA
            0b10111, // 0xB
            0b11010, // 0xC
            0b11011, // 0xD
            0b11100, // 0xE
            0b11101  // 0xF
    };

    /**
     * Encode a byte array using the 4B5B scheme and return an array of bits (0/1).
     * Package-private so the optical receiver shares the exact same table.
     */
    static int[] encode4b5b(byte[] data) {
        int[] bits = new int[data.length * 10]; // each byte -> 2 nibbles -> 10 bits
        int idx = 0;
        for (byte b : data) {
            int high = (b >> 4) & 0x0F;
            int low = b & 0x0F;
            int high5 = FOUR_B_TO_FIVE_B[high];
            int low5 = FOUR_B_TO_FIVE_B[low];
            for (int i = 4; i >= 0; i--) {
                bits[idx++] = (high5 >> i) & 1;
            }
            for (int i = 4; i >= 0; i--) {
                bits[idx++] = (low5 >> i) & 1;
            }
        }
        return bits;
    }

    /**
     * Reverse map for 4B5B. Shared with the receiver; a code that is not in the
     * table was corrupted on the channel and is reported as such rather than
     * silently mapped to a wrong nibble.
     */
    static int decode5b4b(int fiveBits) {
        for (int nibble = 0; nibble < FOUR_B_TO_FIVE_B.length; nibble++) {
            if (FOUR_B_TO_FIVE_B[nibble] == fiveBits) return nibble;
        }
        return -1;
    }

    // ---------------------------------------------------------------------
    // Send bits using the device torch (OOK). Runs synchronously, so it must
    // be invoked from a background thread.
    // ---------------------------------------------------------------------

    /**
     * How long to keep retrying the torch while the camera is still held.
     *
     * ProcessCameraProvider.unbindAll() only *requests* the teardown, so for a
     * short window after it returns the camera device is still owned by CameraX
     * and setTorchMode fails with
     * "Torch for camera X is not available due to an existing camera user"
     * (ServiceSpecificException, code 7). Retrying for a couple of seconds is
     * what makes the handoff reliable.
     */
    private static final long TORCH_RELEASE_TIMEOUT_MS = 3000;
    private static final long TORCH_RETRY_INTERVAL_MS = 100;

    /**
     * Drive the torch, waiting for the camera to become free.
     *
     * @return true when the torch was driven successfully.
     */
    private static boolean setTorchWithRetry(CameraManager camMgr, String flashCamId, boolean on) {
        long deadline = System.currentTimeMillis() + TORCH_RELEASE_TIMEOUT_MS;
        boolean lastBusy = false;
        do {
            try {
                camMgr.setTorchMode(flashCamId, on);
                if (lastBusy) {
                    Log.i(TAG, "torch became available after waiting for the camera, continuing burst");
                }
                return true;
            } catch (CameraAccessException e) {
                // CAMERA_IN_USE is the retryable one: the camera device is still
                // owned by CameraX while it finishes tearing the session down.
                // Anything else (no flash unit, disabled by policy) is terminal.
                String reason = e.getMessage() == null ? "" : e.getMessage();
                boolean busy = e.getReason() == CameraAccessException.CAMERA_IN_USE
                        || reason.contains("CAMERA_IN_USE")
                        || reason.contains("existing camera user");
                if (!busy) {
                    Log.e(TAG, "setTorchMode(" + on + ") failed permanently: " + e);
                    return false;
                }
                lastBusy = true;
            } catch (RuntimeException e) {
                // ServiceSpecificException code 7: the camera is still held by
                // another client. This is a race, not a hard failure.
                lastBusy = true;
            }
            try {
                Thread.sleep(TORCH_RETRY_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        } while (System.currentTimeMillis() < deadline);

        Log.e(TAG, "torch still unavailable after " + TORCH_RELEASE_TIMEOUT_MS
                + "ms (camera held by another client); giving up");
        return false;
    }

    private static boolean sendBitsViaFlash(Context ctx, int[] bits) {
        CameraManager camMgr = (CameraManager) ctx.getSystemService(Context.CAMERA_SERVICE);
        if (camMgr == null) {
            Log.e(TAG, "CameraManager not available");
            return false;
        }
        String flashCamId = findFlashCameraId(camMgr);
        if (flashCamId == null) {
            Log.e(TAG, "No flash-capable camera found");
            return false;
        }
        try {
            // Preamble: alternating symbols so the receiver can detect the burst
            // start and lock its symbol clock on the transitions.
            if (!setTorchWithRetry(camMgr, flashCamId, true)) return false;
            for (int i = 0; i < PREAMBLE_SYMBOLS; i++) {
                camMgr.setTorchMode(flashCamId, (i & 1) == 0);
                Thread.sleep(Constants.opticalSymbolDurationMs);
            }
            for (int bit : bits) {
                camMgr.setTorchMode(flashCamId, bit == 1);
                Thread.sleep(Constants.opticalSymbolDurationMs);
            }
            // End-of-burst: a solid run, the inverse of the preamble pattern.
            for (int i = 0; i < POSTAMBLE_SYMBOLS; i++) {
                camMgr.setTorchMode(flashCamId, true);
                Thread.sleep(Constants.opticalSymbolDurationMs);
            }
            camMgr.setTorchMode(flashCamId, false);
            return true;
        } catch (CameraAccessException e) {
            Log.e(TAG, "Torch mode error", e);
            return false;
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted during flash transmission", e);
            Thread.currentThread().interrupt();
            return false;
        } finally {
            try {
                camMgr.setTorchMode(flashCamId, false);
            } catch (CameraAccessException | RuntimeException e) {
                // RuntimeException included on purpose: leaving the torch stuck
                // on is worse than failing to turn it off cleanly, and it must
                // not propagate out of finally.
                Log.e(TAG, "Failed to turn off torch: " + e);
            }
        }
    }

    private static String findFlashCameraId(CameraManager camMgr) {
        try {
            for (String id : camMgr.getCameraIdList()) {
                CameraCharacteristics caps = camMgr.getCameraCharacteristics(id);
                Boolean hasFlash = caps.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                if (hasFlash != null && hasFlash) {
                    return id;
                }
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Unable to enumerate cameras", e);
        }
        return null;
    }
}