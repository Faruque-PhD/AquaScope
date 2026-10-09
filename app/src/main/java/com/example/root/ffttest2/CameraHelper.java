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
import androidx.camera.core.Camera;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class CameraHelper {

	private static ImageView imageView;
	private static ImageCapture imageCapture;
	private static ProcessCameraProvider boundProvider;
	private static Activity boundActivity;

	/**
	 * Optical receive state. The demodulator is only attached when the receiver
	 * role has the optical medium selected, so the default acoustic-only and
	 * camera-capture paths bind exactly the use cases they did before.
	 */
	private static OpticalReceiver opticalReceiver;
	private static ImageAnalysis opticalAnalysis;


	public static void bindCamera(@NonNull ProcessCameraProvider cameraProvider, Activity activity, ImageView mimageview) {
		imageView = mimageview;
		boundProvider = cameraProvider;
		boundActivity = activity;

		try {

			// The optical demodulator needs frames, and the achievable analysis rate
		// is shared with everything else bound on the same camera. A 1920x1080
		// preview leaves the stream configuration with too little headroom, so
		// while the demodulator is attached the preview drops to 640x480. This
		// only affects the viewfinder resolution, not the captured image, which
		// is produced by ImageCapture at its own resolution.
		boolean opticalAttached = opticalAnalysis != null;
		Preview preview = new Preview.Builder()
				.setTargetResolution(opticalAttached ? new Size(640, 480) : new Size(1920, 1080))
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

		// Build the use-case list. By default the optical demodulator rides
		// alongside capture and the viewfinder, so nothing about aiming or
		// shuttering changes while the receiver is armed.
		//
		// Dropping preview and capture while armed is offered as an option
		// (Constants.opticalAnalysisOnly) because it looks like the obvious way
		// to raise the frame rate, and frame rate is what link time is set by.
		// On an SM-A346E it is not: 9.0 fps with the camera to itself against
		// 9.0 fps with everything bound, and the analyzer measured 0.4 ms of a
		// 112 ms frame interval, so the ceiling is the camera's exposure cycle
		// rather than anything bound to it. Taking the viewfinder away would
		// have cost the operator the thing they aim with, for nothing.
		List<androidx.camera.core.UseCase> useCases = new ArrayList<>();
		if (opticalAnalysis != null && Constants.opticalAnalysisOnly) {
			useCases.add(opticalAnalysis);
		} else {
			if (opticalAnalysis != null) {
				useCases.add(opticalAnalysis);
			}
			useCases.add(imageCapture);
			// a hidden PreviewView never provides a surface, which can stop the capture session from starting,
			// so only bind the preview when the viewfinder is actually shown
			if (Constants.preview.getVisibility() == View.VISIBLE) {
				useCases.add(preview);
			}
		}

		Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) activity, cameraSelector,
				useCases.toArray(new androidx.camera.core.UseCase[0]));
		Utils.logd("camera bound with " + useCases.size() + " use case(s)"
				+ (opticalAnalysis != null
				? (Constants.opticalAnalysisOnly
				? " (optical analysis only, preview/capture released)"
				: " (optical analysis alongside preview/capture)")
				: ""));
		} catch (Exception e) {
			imageCapture = null;
			// Full stack, not just getMessage(): a bind failure is almost always
			// the "Not in application's main thread" ISE followed by a stale UI,
			// and the message alone hides both the cause and the caller.
			Utils.log("Error binding camera: " + e.getClass().getSimpleName()
					+ " (" + e.getMessage() + ")\n"
					+ android.util.Log.getStackTraceString(e));
			showMessage("Camera could not start: " + e.getMessage());
		}
	}

	/**
	 * Attach and arm the optical demodulator.
	 *
	 * The receiver camera watches for the torch burst; without an ImageAnalysis
	 * use case bound there is nothing feeding OpticalReceiver, so the app can
	 * transmit but never receive. Called when the receiver role has the optical
	 * medium selected.
	 */
	public static synchronized void ensureOpticalReceiverArmed() {
		if (opticalReceiver == null) {
			opticalReceiver = new OpticalReceiver();
			opticalReceiver.setListener(MainActivity.OPTICAL_TOKENS_LISTENER);
		}
		if (opticalAnalysis == null) {
			opticalAnalysis = OpticalReceiver.buildAnalyzer(opticalReceiver);
		}
		opticalReceiver.arm();
		// The use case is new, so the camera has to be rebound to pick it up.
		// bindToLifecycle must run on the main thread; every current caller is on
		// it, but a future one may not be, and the failure mode off-main is a
		// swallowed ISE that leaves the preview dead. Marshal defensively.
		if (boundProvider != null && boundActivity != null && imageView != null) {
			if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
				bindCamera(boundProvider, boundActivity, imageView);
			} else {
				new android.os.Handler(android.os.Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						bindCamera(boundProvider, boundActivity, imageView);
					}
				});
			}
		}
		Utils.log("Optical receive armed");
	}

	/** Stop demodulating and drop the analysis use case from the next bind. */
	public static synchronized void disarmOpticalReceiver() {
		if (opticalReceiver != null) {
			opticalReceiver.disarm();
		}
		opticalAnalysis = null;
		opticalReceiver = null;
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
		// While the receiver is armed the analysis use case owns the camera on its
		// own and imageCapture is unbound, so a shutter press would otherwise do
		// nothing. Step the demodulator aside for the shot and put it back once
		// the capture has finished, leaving the link armed.
		final boolean temporarilyDisarmed;
		if (imageCapture == null && opticalAnalysis != null) {
			opticalAnalysis = null;
			if (boundProvider != null && boundActivity != null && imageView != null) {
				bindCamera(boundProvider, boundActivity, imageView);
			}
			temporarilyDisarmed = true;
			Utils.log("Camera: releasing optical analysis briefly to capture a photo");
		} else {
			temporarilyDisarmed = false;
		}
		if (imageCapture == null || imageView == null) {
			if (temporarilyDisarmed) {
				ensureOpticalReceiverArmed();
			}
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

							// The shutter is the cheapest turbidity sample available:
							// the operator has just pointed the camera at the thing
							// being photographed, so the frame is exactly what the
							// MoE's Layer 1 optical sensor was written for. It was
							// never called anywhere, which left ContextVector stuck
							// at (0, 0) and made the turbidity veto dead code.
							// Runs on the camera executor; the Laplacian pass on a
							// 64x64 downscale is sub-millisecond.
							ContextVector.publish(
									TurbidityAnalyzer.analyse(scaledBitmap, ContextVector.current()));

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
							if (temporarilyDisarmed) {
								// This callback runs on the camera executor, but
								// bindToLifecycle has to be called from the main
								// thread, so marshal the restore the same way
								// releaseForTorch marshals its unbindAll.
								new android.os.Handler(android.os.Looper.getMainLooper())
										.post(new Runnable() {
											@Override
											public void run() {
												ensureOpticalReceiverArmed();
												Utils.log("Camera: optical analysis re-armed after capture");
											}
										});
							}
						}
					}

					@Override
					public void onError(ImageCaptureException error) {
						Utils.logd("Error taking picture: " + error.getMessage());
						showMessage("Photo failed: " + error.getMessage());
						cameraExecutor.shutdown();
						if (temporarilyDisarmed) {
							new android.os.Handler(android.os.Looper.getMainLooper())
									.post(new Runnable() {
										@Override
										public void run() {
											ensureOpticalReceiverArmed();
											Utils.log("Camera: optical analysis re-armed after capture error");
										}
									});
						}
					}
				}
		);
	}

	/**
	 * Release the CameraX session so the torch can be driven directly.
	 *
	 * CameraX holds the camera device for as long as any use case is bound to the
	 * lifecycle, and setTorchMode on a camera another client has open fails. The
	 * optical burst needs the torch for several seconds, so the session must be
	 * torn down first and rebuilt afterwards.
	 *
	 * unbindAll() must run on the main thread; calling it from the AsyncTask
	 * background thread throws IllegalStateException("Not in application's main
	 * thread") and silently leaves the camera open, which is what made the first
	 * optical sends fail instantly. So the call is marshalled onto the UI thread
	 * and this method blocks until it has actually run.
	 *
	 * @return true when the camera is free for torch use.
	 */
	public static synchronized boolean releaseForTorch() {
		if (boundProvider == null || boundActivity == null) {
			// Nothing was ever bound, so nothing is holding the camera.
			return true;
		}
		final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
		final boolean[] ok = {false};
		android.os.Handler main = new android.os.Handler(android.os.Looper.getMainLooper());
		main.post(new Runnable() {
			@Override
			public void run() {
				try {
					boundProvider.unbindAll();
					imageCapture = null;
					ok[0] = true;
					Utils.logd("Camera session released for optical transmission");
				} catch (Exception e) {
					Utils.logd("Failed to release camera for torch: " + e);
				} finally {
					done.countDown();
				}
			}
		});
		try {
			// Bounded so a wedged main thread cannot hang the burst forever.
			done.await(5, java.util.concurrent.TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
		return ok[0];
	}

	/**
	 * Rebuild the camera session after a torch burst. No-op when the camera was
	 * never bound or never released.
	 *
	 * <p>This is called from {@code SendOpticalAsyncTask.doInBackground}, on a
	 * background thread, and {@code bindToLifecycle} throws
	 * {@code IllegalStateException("Not in application's main thread")} there.
	 * The exception is swallowed inside {@link #bindCamera}, so what you actually
	 * get is a log line and a camera that never comes back: preview and shutter
	 * dead for the rest of the session, with nothing failing loudly. So the bind
	 * is marshalled onto the main thread the same way {@link #releaseForTorch}
	 * marshals its unbindAll, and waited on with the same bound.
	 *
	 * <p>The three references are read into locals first and the class monitor is
	 * deliberately not held across the wait - holding it would block a main-thread
	 * call to {@link #ensureOpticalReceiverArmed} while this thread waits for that
	 * very thread to run the posted bind.
	 */
	public static void rebindAfterTorch() {
		final ProcessCameraProvider provider = boundProvider;
		final Activity activity = boundActivity;
		final ImageView iv = imageView;
		if (provider == null || activity == null || iv == null) {
			return;
		}
		if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
			bindCamera(provider, activity, iv);
			Utils.logd("Camera session rebound after optical transmission");
			return;
		}
		final java.util.concurrent.CountDownLatch done =
				new java.util.concurrent.CountDownLatch(1);
		new android.os.Handler(android.os.Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				try {
					bindCamera(provider, activity, iv);
				} finally {
					done.countDown();
				}
			}
		});
		try {
			done.await(5, java.util.concurrent.TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return;
		}
		Utils.logd("Camera session rebound after optical transmission");
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
