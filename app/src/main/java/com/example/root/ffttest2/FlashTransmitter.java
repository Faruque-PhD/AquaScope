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
    // Symbol duration in milliseconds (adjust as needed for device capabilities)
    private static final int SYMBOL_DURATION_MS = 10;
    // Number of all-ON symbols transmitted before the data as a simple preamble
    private static final int PREAMBLE_SYMBOLS = 8;

    /**
     * Transmit the embedding of a bitmap via the device flash.
     *
     * @param context Context used to access the CameraManager.
     * @param bitmap  Bitmap to be encoded and sent.
     * @return true if transmission completed without errors, false otherwise.
     */
    public static boolean transmitEmbedding(Context context, Bitmap bitmap) {
        if (context == null) {
            Log.e(TAG, "Context is null, abort transmission");
            return false;
        }
        if (bitmap == null) {
            Log.e(TAG, "Bitmap is null, abort transmission");
            return false;
        }
        try {
            // 1. Encode the bitmap into embedding tokens with the existing VQGAN encoder.
            long[] tokens = Utils.encode_image(bitmap);
            if (tokens == null || tokens.length == 0) {
                Log.e(TAG, "Failed to obtain embedding tokens");
                return false;
            }
            byte[] embedding = Utils.Embedding2Bytes(tokens);
            if (embedding == null || embedding.length == 0) {
                Log.e(TAG, "Failed to obtain embedding bytes");
                return false;
            }
            // 2. Apply 4B5B line coding to get a run-length-friendly bit stream.
            int[] bits = encode4b5b(embedding);
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
     */
    private static int[] encode4b5b(byte[] data) {
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
            // Preamble: all-ON symbols so the receiver can detect the burst start.
            for (int i = 0; i < PREAMBLE_SYMBOLS; i++) {
                camMgr.setTorchMode(flashCamId, true);
                Thread.sleep(SYMBOL_DURATION_MS);
            }
            for (int bit : bits) {
                camMgr.setTorchMode(flashCamId, bit == 1);
                Thread.sleep(SYMBOL_DURATION_MS);
            }
            // End-of-burst: a short all-ON marker then off.
            camMgr.setTorchMode(flashCamId, true);
            Thread.sleep(SYMBOL_DURATION_MS);
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