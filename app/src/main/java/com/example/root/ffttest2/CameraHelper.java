package com.example.root.ffttest2;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Size;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
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
import androidx.camera.core.Camera;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class CameraHelper {

	private static ImageView imageView;
	private static ImageCapture imageCapture;


	public static void bindCamera(@NonNull ProcessCameraProvider cameraProvider, Activity activity, ImageView mimageview) {
		imageView = mimageview;

		try {

			Preview preview = new Preview.Builder()
					.setTargetResolution(new Size(1920,1080))
					.build();

			CameraSelector cameraSelector = new CameraSelector.Builder()
					.requireLensFacing(CameraSelector.LENS_FACING_BACK)
					.build();
			Constants.preview.setScaleType(PreviewView.ScaleType.FIT_CENTER);

			preview.setSurfaceProvider(Constants.preview.getSurfaceProvider());
			imageCapture = new ImageCapture.Builder()
					.setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
					.build();

			// may be called again after permission is granted, so clear any earlier binding first
			cameraProvider.unbindAll();
			// a hidden PreviewView never provides a surface, which can stop the capture session from starting,
			// so only bind the preview when the viewfinder is actually shown
			Camera camera;
			if (Constants.preview.getVisibility() == View.VISIBLE) {
				camera = cameraProvider.bindToLifecycle((LifecycleOwner) activity, cameraSelector, imageCapture, preview);
			} else {
				camera = cameraProvider.bindToLifecycle((LifecycleOwner) activity, cameraSelector, imageCapture);
			}
			Utils.logd("camera bound");
		} catch (Exception e) {
			imageCapture = null;
			Utils.logd("Error binding camera: " + e.getMessage());
			showMessage("Camera could not start: " + e.getMessage());
		}
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
		Utils.log("camera capture");
		if (imageCapture == null || imageView == null) {
			showMessage("Camera is not ready. Allow camera access, then press Camera again.");
			return;
		}
		ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();
		imageCapture.takePicture(cameraExecutor,
				new ImageCapture.OnImageCapturedCallback() {
					@Override
					public void onCaptureSuccess(@NonNull ImageProxy image) {
						try {
							Bitmap bitmap = imageProxyToBitmap(image);
							Utils.log("bitmap size " + bitmap.getHeight() + " " + bitmap.getWidth());
							Bitmap rotatedBitmap = rotateBitmap(bitmap, 90); // Rotate the bitmap
							Bitmap croppedBitmap = cropCenterSquare(rotatedBitmap);
							Bitmap scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, Constants.compressImageSize, Constants.compressImageSize, true);
							Constants.currentCameraCapture = scaledBitmap;
							imageView.post(new Runnable() {
								@Override
								public void run() {
									imageView.setImageBitmap(scaledBitmap);
								}
							});
						} catch (Exception e) {
							Utils.logd("Error processing camera image: " + e.getMessage());
							showMessage("Photo could not be processed: " + e.getMessage());
						} finally {
							image.close();
							cameraExecutor.shutdown();
						}
					}

					@Override
					public void onError(ImageCaptureException error) {
						Utils.logd("Error taking picture: " + error.getMessage());
						showMessage("Photo failed: " + error.getMessage());
						cameraExecutor.shutdown();
					}
				}
		);
	}

	private static void showMessage(String message) {
		Activity activity = MainActivity.av;
		if (activity == null) {
			return;
		}
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
			}
		});
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
