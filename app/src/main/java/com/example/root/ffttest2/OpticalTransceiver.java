package com.example.root.ffttest2;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Helper class that forwards a bitmap to the flash transmitter.
 */
public class OpticalTransceiver {
    /**
     * Sends the provided bitmap via the optical channel.
     *
     * @param context Context used to access the CameraManager.
     * @param bitmap  Bitmap to transmit.
     * @return true if transmission succeeded, false otherwise.
     */
    public static boolean send(Context context, Bitmap bitmap) {
        return FlashTransmitter.transmitEmbedding(context, bitmap);
    }
}
