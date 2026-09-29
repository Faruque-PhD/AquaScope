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
    // The optical receiver is a phone camera, not a photodiode, and this value is
    // bounded by the camera, not by the torch. CameraX delivers ~28-30 fps on a
    // typical device, so a symbol must be long enough to be oversampled:
    //
    //   samples_per_symbol = frame_rate * symbol_ms / 1000
    //
    // At 30 fps, 100 ms gives 3 samples per symbol, which is enough to resolve
    // the eye pattern. The 33 ms that would be appropriate for a photodiode gives
    // only 0.9 samples and cannot be demodulated at all - the receiver measures
    // and reports the achieved rate on the first frames so this can be checked on
    // the actual device before a field test.
    //
    // The cost is link time, and it is steep: a 4096-codebook image is 64 tokens
    // = 96 bytes = 960 line bits, so at 100 ms the flash is on for ~96 s. Cutting
    // that down requires either a faster sensor or a smaller payload, both of
    // which are product decisions rather than tuning.
    static final int SYMBOL_DURATION_MS = 100;
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
                    + (PREAMBLE_SYMBOLS + bits.length + POSTAMBLE_SYMBOLS) * SYMBOL_DURATION_MS / 1000f + "s burst)");
            // 3. Send preamble followed by the data bits via the torch (OOK).
            return sendBitsViaFlash(context, bits);
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
            for (int i = 0; i < PREAMBLE_SYMBOLS; i++) {
                camMgr.setTorchMode(flashCamId, (i & 1) == 0);
                Thread.sleep(SYMBOL_DURATION_MS);
            }
            for (int bit : bits) {
                camMgr.setTorchMode(flashCamId, bit == 1);
                Thread.sleep(SYMBOL_DURATION_MS);
            }
            // End-of-burst: a solid run, the inverse of the preamble pattern.
            for (int i = 0; i < POSTAMBLE_SYMBOLS; i++) {
                camMgr.setTorchMode(flashCamId, true);
                Thread.sleep(SYMBOL_DURATION_MS);
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
            } catch (CameraAccessException e) {
                Log.e(TAG, "Failed to turn off torch", e);
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