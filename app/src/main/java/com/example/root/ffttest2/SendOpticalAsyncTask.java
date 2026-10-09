package com.example.root.ffttest2;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

/**
 * AsyncTask that transmits the clicked image over the optical channel.
 *
 * The bitmap is encoded with exactly the same codec the acoustic path uses
 * ({@link Utils#encode_image}, then {@link Utils#Embedding2Bytes}) so the two
 * media carry identical bits and the receiver reconstructs the same image either
 * way. Only the physical layer differs: LoRa/underwater acoustics on one side,
 * torch OOK on the other.
 *
 * Params is Object (not Void) because {@link Constants#task} is a raw AsyncTask,
 * so Android supplies an Object[] to doInBackground and a Void[] cast would crash.
 */
public class SendOpticalAsyncTask extends AsyncTask<Object, Void, Boolean> {
    private final Context context;
    private final Bitmap bitmap;
    /**
     * Tokens already produced by a shared encode, or null to encode here.
     *
     * The acoustic path takes its payload from Utils.Embedding2Bytes applied to
     * Constants.encode_sequence, which Utils.encode_image fills in. Passing the
     * same array in here means both media carry byte-identical payload instead of
     * each running its own encode. Left null while only one medium is ever active,
     * where this task is the sole encoder and the bits are identical by
     * construction.
     */
    private final long[] tokens;

    public SendOpticalAsyncTask(Context context, Bitmap bitmap) {
        this(context, bitmap, null);
    }

    public SendOpticalAsyncTask(Context context, Bitmap bitmap, long[] preEncodedTokens) {
        this.context = context;
        this.bitmap = bitmap;
        this.tokens = preEncodedTokens;
    }

    @Override
    protected Boolean doInBackground(Object... ignored) {
        boolean cameraReleased = false;
        try {
            // The torch is driven through CameraManager, which cannot take the
            // camera while CameraX holds it open. Hand the device over for the
            // duration of the burst and take it back afterwards.
            cameraReleased = CameraHelper.releaseForTorch();
            if (!cameraReleased) {
                Utils.log("Optical send: could not release the camera, torch may fail");
            }

            // A link check carries a short known pattern rather than an image, so
            // the physical link can be validated in ~19s instead of waiting out a
            // ~5 minute image burst. Everything around it is identical: the same
            // camera handover, the same preamble, the same 4B5B framing.
            if (Constants.opticalLinkCheck) {
                boolean ok = FlashTransmitter.transmitLinkCheck(context);
                if (ok) {
                    Utils.log("Optical link check completed; expect "
                            + FlashTransmitter.LINK_CHECK_PATTERN.length + " bytes back on the receiver");
                } else {
                    Utils.log("Optical link check failed");
                    MainActivity.showUserMessage(
                            context instanceof Activity ? (Activity) context : MainActivity.av,
                            "Optical link check failed. Check that another app is not "
                                    + "using the camera or torch, then try again.");
                }
                return ok;
            }

            // Encode through the shared codec first so the optical burst carries
            // the same token bytes the acoustic path would, and the token
            // sequence is available for logging and for the receiver's ground truth.
            // A pre-encoded array is used verbatim so no second encode can diverge.
            long[] toSend = (tokens != null && tokens.length > 0) ? tokens : Utils.encode_image(bitmap);
            if (toSend == null || toSend.length == 0) {
                Log.e("SendOpticalAsyncTask", "Encoder returned no tokens");
                return false;
            }
            // Keep the shared static in step with what is actually going on the air.
            Constants.encode_sequence = toSend;
            // Mirror the acoustic path's UI/logging: show the sent image and record
            // ground-truth artifacts so end2endCam behaves identically optically.
            try {
                if (MainActivity.mImageView != null) {
                    final Bitmap show = bitmap;
                    MainActivity.mImageView.post(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.mImageView.setImageBitmap(show);
                        }
                    });
                }
                if (Constants.allowLog && bitmap != null) {
                    FileOperations.saveBitmapToFile(
                            context instanceof Activity ? (Activity) context : MainActivity.av,
                            bitmap,
                            Utils.genName(Constants.SignalType.Raw_Input_Bitmap, 0) + ".png");
                }
            } catch (Throwable t) {
                Utils.log("Optical send: failed to update UI/log for sent image: " + t);
            }
            Utils.log("Optical send embedding: " + java.util.Arrays.toString(toSend));

            boolean ok = OpticalTransceiver.send(context, toSend);
            if (ok) {
                Utils.log("Optical transmission completed successfully");
            } else {
                Utils.log("Optical transmission failed");
                // The burst is invisible when it dies, so the operator gets told
                // rather than left watching an unresponsive app.
                MainActivity.showUserMessage(
                        context instanceof Activity ? (Activity) context : MainActivity.av,
                        "Optical transmission failed. Check that another app is not "
                                + "using the camera or torch, then try again.");
            }
            return ok;
        } catch (Exception e) {
            Log.e("SendOpticalAsyncTask", "Transmission failed", e);
            MainActivity.showUserMessage(
                    context instanceof Activity ? (Activity) context : MainActivity.av,
                    "Optical transmission failed: " + e.getMessage());
            return false;
        } finally {
            // Always give the camera back, even if the burst threw, otherwise the
            // preview and the shutter stay dead for the rest of the session.
            if (cameraReleased) {
                CameraHelper.rebindAfterTorch();
            }
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        boolean ok = success != null && success;
        // Layer 5 feedback: report the outcome so the NACK rate, SNR term and
        // penalty windows stay current for the next routing decision. The
        // neutral overload is used rather than the previous
        // MoEManager.lastReportedSnr(), which was the *acoustic* feedback SNR:
        // the sender has no optical measurement of its own here, and reporting
        // someone else's number as optical quality would corrupt the score.
        // The receiving device records the real optical quality when its burst
        // arrives, in OpticalReceiver.demodulate.
        MoE.recordResult(ok, Constants.CommMedium.OPTICAL);

        // Hand the device back to the receiver, exactly as the acoustic path does
        // in SendChirpAsyncTask.onPostExecute. Without this the app stays in the
        // Alice/transmit role after a flash burst and never stops transmitting.
        if (Constants.sp1 != null) {
            Constants.sp1 = null;
        }
        Constants.user = Constants.User.Bob;
        // Hand the channel back to acoustic when the burst succeeded, so the
        // next transmission alternates. Runs before startMethod so the device
        // arms its receive side for the medium it is about to use.
        if (ok) {
            MainActivity.autoSwitchMedium(Constants.CommMedium.OPTICAL);
        }
        // Optical reception is armed by startMethod on the OPTICAL branch, so the
        // demodulator is bound to the camera again now that this device is the
        // receiver rather than the transmitter.
        MainActivity.startMethod(context instanceof Activity ? (Activity) context : MainActivity.av);
    }
}
