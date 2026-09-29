package com.example.root.ffttest2;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Thin wrapper over the torch transmitter.
 *
 * The sender encodes the clicked image once with {@link Utils#encode_image} and
 * hands the token array here, so the optical burst carries exactly the same
 * 12-bit token payload the acoustic path transmits. Encoding is deliberately not
 * repeated inside the transmitter: a second encode pass would risk picking up a
 * different tokenisation (the encoder is not guaranteed to be bit-deterministic
 * across warm-ups) and the two media would then disagree on the image.
 */
public class OpticalTransceiver {

    private OpticalTransceiver() {}

    /**
     * Transmit an already-encoded embedding over the optical channel.
     *
     * @param context Context used to access the CameraManager.
     * @param tokens  token array from {@link Utils#encode_image(Bitmap)}.
     * @return true if the burst completed without errors.
     */
    public static boolean send(Context context, long[] tokens) {
        return FlashTransmitter.transmitTokens(context, tokens);
    }

    /**
     * Encode and transmit a bitmap. Kept for callers that do not already hold the
     * token array.
     */
    public static boolean send(Context context, Bitmap bitmap) {
        if (bitmap == null) return false;
        long[] tokens = Utils.encode_image(bitmap);
        if (tokens == null || tokens.length == 0) return false;
        return send(context, tokens);
    }
}
