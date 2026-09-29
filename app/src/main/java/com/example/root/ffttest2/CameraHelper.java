package com.example.root.ffttest2;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import android.graphics.ImageFormat;
import android.graphics.BitmapFactory;
import java.nio.ByteBuffer;

import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.lifecycle.LifecycleOwner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class CameraHelper {

	private static ImageView imageView;
	private static ImageCapture imageCapture;
	private static ImageAnalysis imageAnalysis;
	private static final ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();
	private static ProcessCameraProvider boundProvider;
	private static Activity boundActivity;
	private static OpticalReceiver opticalReceiver;

	/**
	 * Register the optical receiver and (re)bind the camera so its analysis use
	 * case is active. Called on the receiver role only.
	 */
	public static synchronized void setOpticalReceiver(OpticalReceiver receiver) {
		opticalReceiver = receiver;
	}

	/** Arm or disarm optical reception and rebuild the camera session to match. */
	public static synchronized void setOpticalReceiveEnabled(boolean enabled) {
		if (opticalReceiver == null) return;
		if (enabled) {
			opticalReceiver.arm();
		} else {
			opticalReceiver.disarm();
		}
		if (boundProvider != null && boundActivity != null && imageView != null) {
			bindCamera(boundProvider, boundActivity, imageView);
		}
	}


	public static boolean bindCamera(@NonNull ProcessCameraProvider cameraProvider, Activity activity, ImageView mimageview) {
		imageView = mimageview;
		boundActivity = activity;
		boundProvider = cameraProvider;

		try {
			cameraProvider.unbindAll();

			Preview preview = new Preview.Builder()
					.build();

			CameraSelector cameraSelector = new CameraSelector.Builder()
					.requireLensFacing(CameraSelector.LENS_FACING_BACK)
					.build();
			// PERFORMANCE mode (SurfaceView) works around a camera-view 1.0.0-alpha31 crash
			// ("Unexpected rotation value -1") that only occurs in the TextureView path.
			Constants.preview.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);
			Constants.preview.setScaleType(PreviewView.ScaleType.FIT_CENTER);

			preview.setSurfaceProvider(Constants.preview.getSurfaceProvider());
			imageCapture = new ImageCapture.Builder()
					.setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
					.build();

			if (opticalReceiver != null && opticalReceiver.isArmed()) {
				// Only add the analysis use case while the receiver is armed: it
				// costs a full camera pass per frame and is otherwise pure overhead.
				imageAnalysis = OpticalReceiver.buildAnalyzer(opticalReceiver);
			} else {
				imageAnalysis = null;
			}

			if (imageAnalysis != null) {
				cameraProvider.bindToLifecycle((LifecycleOwner) activity, cameraSelector,
						imageCapture, preview, imageAnalysis);
			} else {
				cameraProvider.bindToLifecycle((LifecycleOwner) activity, cameraSelector, imageCapture, preview);
			}
			return true;
		} catch (Exception e) {
			imageCapture = null;
			Utils.logd("Error binding camera: " + e);
			return false;
		}
	}

	/**
	 * Release the CameraX session so the torch can be driven directly.
	 *
	 * CameraX holds the camera device for as long as any use case is bound to the
	 * lifecycle, and setTorchMode on a camera that another client has open fails.
	 * The optical burst needs the torch for several seconds, so the session has to
	 * be torn down first and rebuilt afterwards.
	 *
	 * @return true if the camera is now free for torch use.
	 */
	public static synchronized boolean releaseForTorch() {
		if (boundProvider == null || boundActivity == null) {
			// Nothing was ever bound, so nothing is holding the camera.
			return true;
		}
		try {
			boundProvider.unbindAll();
			imageCapture = null;
			Utils.logd("Camera session released for optical transmission");
			return true;
		} catch (Exception e) {
			Utils.logd("Failed to release camera for torch: " + e);
			return false;
		}
	}

	/**
	 * Rebuild the camera session after a torch burst. Safe to call when the camera
	 * was never released, in which case it is a no-op.
	 */
	public static synchronized void rebindAfterTorch() {
		if (boundProvider == null || boundActivity == null || imageView == null) {
			return;
		}
		bindCamera(boundProvider, boundActivity, imageView);
		Utils.logd("Camera session rebound after optical transmission");
	}

	public static Bitmap imageProxyToBitmap(ImageProxy image) {
		// Ensure the image format is JPEG
		if (image.getFormat() != ImageFormat.JPEG) {
			Utils.log("image format " + image.getFormat());
			throw new IllegalArgumentException("Unsupported image format");
		}

		// Get the ByteBuffer containing the JPEG data
		ByteBuffer buffer = image.getPlanes()[0].getBuffer();
		byte[] bytes = new byte[buffer.capacity()];
		buffer.get(bytes);

		// Decode the JPEG byte array to a Bitmap
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
	}

	public static void takePicture2() {
		if (imageCapture == null || imageView == null) {
			Utils.logd("Camera capture requested before the camera was ready");
			return;
		}

		Utils.log("camera capture");
		imageCapture.takePicture(cameraExecutor,
				new ImageCapture.OnImageCapturedCallback() {
					@Override
					public void onCaptureSuccess(@NonNull ImageProxy image) {
						try {
							Bitmap bitmap = imageProxyToBitmap(image);
							if (bitmap == null) {
								Utils.logd("Camera returned an undecodable JPEG frame");
								return;
							}
						Bitmap rotatedBitmap = rotateBitmap(bitmap, image.getImageInfo().getRotationDegrees());
						Bitmap croppedBitmap = cropCenterSquare(rotatedBitmap);
						Bitmap scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, Constants.compressImageSize, Constants.compressImageSize, true);
						Constants.currentCameraCapture = scaledBitmap;
						MainActivity.mBitmap = scaledBitmap;
						// Flag the capture as current for the end-to-end modes so the
						// send path uses this clicked frame rather than a preloaded asset.
						Constants.hasFreshCameraCapture = true;
						// Feed the MoE sensor fusion pipeline with the live frame.
						SensorFusion.get(imageView.getContext()).submitFrame(scaledBitmap);
						imageView.post(() -> {
							imageView.setImageBitmap(scaledBitmap);
							Utils.log("Picture captured and set as active bitmap");
						});
						} catch (Exception e) {
							Utils.logd("Unable to process captured image: " + e);
						} finally {
							image.close();
						}
					}

					@Override
					public void onError(ImageCaptureException error) {
						Utils.logd("Camera capture failed: " + error);
					}
				}
		);
	}

	private static Bitmap cropCenterSquare(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int newWidth = (width > height) ? height : width;
		int newHeight = (width > height) ? height : width;

		int cropW = (width - newWidth) / 2;
		int cropH = (height - newHeight) / 2;

		return Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);
	}

	private static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
		Matrix matrix = new Matrix();
		matrix.postRotate(degrees);
		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	}

}
