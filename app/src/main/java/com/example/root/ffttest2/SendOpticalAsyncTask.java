package com.example.root.ffttest2;

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

    public SendOpticalAsyncTask(Context context, Bitmap bitmap) {
        this.context = context;
        this.bitmap = bitmap;
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

            // Encode through the shared codec first so the optical burst carries
            // the same token bytes the acoustic path would, and the token
            // sequence is available for logging and for the receiver's ground truth.
            long[] tokens = Utils.encode_image(bitmap);
            if (tokens == null || tokens.length == 0) {
                Log.e("SendOpticalAsyncTask", "Encoder returned no tokens");
                return false;
            }
            Constants.encode_sequence = tokens;
            Utils.log("Optical send embedding: " + java.util.Arrays.toString(tokens));

            boolean ok = OpticalTransceiver.send(context, tokens);
            if (ok) {
                Utils.log("Optical transmission completed successfully");
            } else {
                Utils.log("Optical transmission failed");
            }
            return ok;
        } catch (Exception e) {
            Log.e("SendOpticalAsyncTask", "Transmission failed", e);
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
        // penalty windows stay current for the next routing decision.
        MoE.recordResult(ok, MoEManager.lastReportedSnr(), Constants.CommMedium.OPTICAL);
    }
}
