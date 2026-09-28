package com.example.root.ffttest2;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

/**
 * AsyncTask that forwards the captured bitmap to the optical transmitter.
 * It simply calls {@link FlashTransmitter#transmitEmbedding(Context, Bitmap)} on a background thread.
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
        try {
            return FlashTransmitter.transmitEmbedding(context, bitmap);
        } catch (Exception e) {
            Log.e("SendOpticalAsyncTask", "Transmission failed", e);
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        // Record (medium, result) in MoE buffer for future routing decisions
        MoEManager.recordResult(success != null && success, Constants.CommMedium.OPTICAL);
        if (success) {
            Utils.log("Optical transmission completed successfully");
        } else {
            Utils.log("Optical transmission failed");
        }
    }
}
