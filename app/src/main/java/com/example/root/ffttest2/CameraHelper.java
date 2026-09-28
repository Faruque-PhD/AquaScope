package com.example.root.ffttest2;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.widget.ImageView;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class CameraHelper {

	private static ImageView imageView;
	private static ImageCapture imageCapture;
	private static final ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();


	public static boolean bindCamera(@NonNull ProcessCameraProvider cameraProvider, Activity activity, ImageView mimageview) {
		imageView = mimageview;

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

			cameraProvider.bindToLifecycle((LifecycleOwner) activity, cameraSelector, imageCapture, preview);
			return true;
		} catch (Exception e) {
			imageCapture = null;
			Utils.logd("Error binding camera: " + e);
			return false;
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
