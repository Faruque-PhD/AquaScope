package com.example.root.ffttest2;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Layer 1 of the MoE gating network, optical half: turns a camera frame into a
 * normalised turbidity score.
 *
 * Two metrics are computed over a centre crop, per the design reference:
 *
 *   Variance of Laplacian — measures sharpness. Suspended particles scatter the
 *   light before it forms a coherent image, so silty water produces extreme blur
 *   and a low variance. This is the primary signal:
 *       turbidity = 1 - clamp(variance / baseline, 0, 1)
 *
 *   Mean luminance of the same crop — a secondary check. Near-total darkness
 *   (night dive or heavy silt) also indicates an unusable optical path, because
 *   the receiver has no ambient reference to detect pulses against.
 *
 * Both are computed on a small downscale, which keeps the Laplacian pass
 * sub-millisecond on a mobile CPU as the doc assumes.
 */
public final class TurbidityAnalyzer {

    private TurbidityAnalyzer() {}

    /** Working resolution for the analysis; small on purpose, this is not display. */
    private static final int WORK_SIZE = 64;

    /**
     * Analyse a frame and return the ContextVector with the turbidity fields set.
     * The noise score is carried over from the previous vector because this class
     * only owns the optical half of the sensor pipeline.
     */
    public static ContextVector analyse(Bitmap frame, ContextVector previous) {
        float prevNoise = (previous == null) ? 0f : previous.noiseScore;
        if (frame == null) {
            return new ContextVector(0f, prevNoise, 0f, 0f);
        }

        Bitmap crop = centreCropToSquare(frame);
        Bitmap small = Bitmap.createScaledBitmap(crop, WORK_SIZE, WORK_SIZE, true);
        2026-10-07 12:22:17.296 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=no preference, reason=boost timeout, vri=VRI[MainActivity]@17e0af0
        2026-10-07 12:22:20.345 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 26.3 fps, 7.9 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.45ms of 38.0ms interval
        2026-10-07 12:22:25.348 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 27.0 fps, 8.1 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.33ms of 37.0ms interval
        2026-10-07 12:22:30.378 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 26.3 fps, 7.9 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.51ms of 38.0ms interval
        2026-10-07 12:22:31.049 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:22:31.050 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=high hint, reason=touch, vri=VRI[MainActivity]@17e0af0
        2026-10-07 12:22:31.055 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:22:34.051 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:22:34.056 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=no preference, reason=boost timeout, vri=VRI[MainActivity]@17e0af0
        2026-10-07 12:22:34.067 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=high hint, reason=touch, vri=VRI[MainActivity]@17e0af0
        2026-10-07 12:22:34.275 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:22:35.381 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 21.6 fps, 6.5 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.40ms of 46.2ms interval
        2026-10-07 12:22:35.440 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:22:35.540 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:22:38.542 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=no preference, reason=boost timeout, vri=VRI[MainActivity]@17e0af0
        2026-10-07 12:22:38.552 22905-22905 log                     com.example.aquascope                D  onstop
        2026-10-07 12:22:38.552 22905-22905 log                     com.example.aquascope                E  start startmethod from startWrapper
        2026-10-07 12:22:38.554 22905-22905 log                     com.example.aquascope                E  startMethod: manual veto selected OPTICAL; sending on the flash channel only
        2026-10-07 12:22:38.558 22905-22905 DeferrableSurface       com.example.aquascope                D  surface closed,  useCount=1 closed=true androidx.camera.core.impl.ImmediateSurface@b9b3f3e
        2026-10-07 12:22:38.558 22905-22949 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Use cases [androidx.camera.core.ImageAnalysis-ba4bffa1-730f-4b3c-8d16-fbac0ec039da43822420, androidx.camera.core.ImageCapture-047f384e-d2c4-4b54-a2b7-418d13cfb44d209556733] now DETACHED for camera
        2026-10-07 12:22:38.558 22905-22949 UseCaseAttachState      com.example.aquascope                D  All use case: [] for camera: 0
        2026-10-07 12:22:38.558 22905-22905 DeferrableSurface       com.example.aquascope                D  surface closed,  useCount=1 closed=true androidx.camera.core.impl.ImmediateSurface@b7018bb
        2026-10-07 12:22:38.558 22905-22949 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Resetting Capture Session
        2026-10-07 12:22:38.559 22905-22905 log                     com.example.aquascope                D  Camera session released for optical transmission
        2026-10-07 12:22:38.560 22905-22949 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Releasing session in state OPENED
        2026-10-07 12:22:38.561 22905-22949 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [] for camera: 0
        2026-10-07 12:22:38.563 22905-22949 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [] for camera: 0
        2026-10-07 12:22:38.565 22905-22949 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Closing camera.
        2026-10-07 12:22:38.565 22905-22949 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Transitioning camera internal state: OPENED --> CLOSING
        2026-10-07 12:22:38.565 22905-22949 CameraStateRegistry     com.example.aquascope                D  Recalculating open cameras:
        Camera                                       State
        -------------------------------------------------------------------
                Camera@a0800e8[id=1]                         UNKNOWN
        Camera@bbc0e3d[id=2]                         UNKNOWN
        Camera@bd6def[id=0]                          CLOSING
                -------------------------------------------------------------------
                Open count: 1 (Max allowed: 1)
        2026-10-07 12:22:38.565 22905-22949 CameraStateMachine      com.example.aquascope                D  New public camera state CameraState{type=CLOSING, error=null} from CLOSING and null
        2026-10-07 12:22:38.565 22905-22949 CameraStateMachine      com.example.aquascope                D  Publishing new public camera state CameraState{type=CLOSING, error=null}
        2026-10-07 12:22:38.566 22905-22949 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Resetting Capture Session
        2026-10-07 12:22:38.566 22905-22949 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Releasing session in state CLOSING
        2026-10-07 12:22:38.566 22905-22949 CaptureSession          com.example.aquascope                D  onSessionFinished()
        2026-10-07 12:22:38.634 22905-23114 CameraManagerGlobal     com.example.aquascope                I  Camera 0 facing CAMERA_FACING_BACK state now CAMERA_STATE_IDLE for client com.example.aquascope API Level 2 User Id 0Device Id 0
        2026-10-07 12:22:38.898 22905-23114 CameraManagerGlobal     com.example.aquascope                I  postSingleUpdate device: camera id 0 status STATUS_PRESENT
        2026-10-07 12:22:38.898 22905-23114 CameraManagerGlobal     com.example.aquascope                I  postSingleUpdate device: camera id 0 status STATUS_PRESENT
        2026-10-07 12:22:38.899 22905-23114 CameraManagerGlobal     com.example.aquascope                I  postSingleUpdate device: camera id 0 status STATUS_PRESENT
        2026-10-07 12:22:38.901 22905-23114 CameraManagerGlobal     com.example.aquascope                I  Camera 0 facing CAMERA_FACING_BACK state now CAMERA_STATE_CLOSED for client com.example.aquascope API Level 2 User Id 0Device Id 0
        2026-10-07 12:22:38.910 22905-22974 ample.aquascope         com.example.aquascope                W  Long monitor contention with owner CameraX-core_camera_2 (22949) at void android.hardware.camera2.impl.CameraDeviceImpl.close()(CameraDeviceImpl.java:1833) waiters=0 in void android.hardware.camera2.impl.CameraDeviceImpl$CameraDeviceCallbacks.onCaptureStarted(android.hardware.camera2.impl.CaptureResultExtras, long) for 332ms
        2026-10-07 12:22:38.911 22905-22949 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [] for camera: 0
        2026-10-07 12:22:38.914 22905-22948 DeferrableSurface       com.example.aquascope                D  use count-1,  useCount=0 closed=true androidx.camera.core.impl.ImmediateSurface@b7018bb
        2026-10-07 12:22:38.915 22905-22948 DeferrableSurface       com.example.aquascope                D  Surface no longer in use[total_surfaces=2, used_surfaces=1](androidx.camera.core.impl.ImmediateSurface@b7018bb}
2026-10-07 12:22:38.915 22905-22948 DeferrableSurface       com.example.aquascope                D  Surface terminated[total_surfaces=1, used_surfaces=1](androidx.camera.core.impl.ImmediateSurface@b7018bb}
2026-10-07 12:22:38.915 22905-22948 DeferrableSurface       com.example.aquascope                D  use count-1,  useCount=0 closed=true androidx.camera.core.impl.ImmediateSurface@b9b3f3e
2026-10-07 12:22:38.915 22905-22948 DeferrableSurface       com.example.aquascope                D  Surface no longer in use[total_surfaces=1, used_surfaces=0](androidx.camera.core.impl.ImmediateSurface@b9b3f3e}
        2026-10-07 12:22:38.915 22905-22948 DeferrableSurface       com.example.aquascope                D  Surface terminated[total_surfaces=0, used_surfaces=0](androidx.camera.core.impl.ImmediateSurface@b9b3f3e}
        2026-10-07 12:22:38.915 22905-22948 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} CameraDevice.onClosed()
2026-10-07 12:22:38.916 22905-22948 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Transitioning camera internal state: CLOSING --> INITIALIZED
2026-10-07 12:22:38.916 22905-22948 CameraStateRegistry     com.example.aquascope                D  Recalculating open cameras:
Camera                                       State
                                                                                                    -------------------------------------------------------------------
Camera@a0800e8[id=1]                         UNKNOWN
Camera@bbc0e3d[id=2]                         UNKNOWN
Camera@bd6def[id=0]                          CLOSED
                                                                                                    -------------------------------------------------------------------
Open count: 0 (Max allowed: 1)
        2026-10-07 12:22:38.917 22905-22948 CameraStateMachine      com.example.aquascope                D  New public camera state CameraState{type=CLOSED, error=null} from CLOSED and null
        2026-10-07 12:22:38.917 22905-22948 CameraStateMachine      com.example.aquascope                D  Publishing new public camera state CameraState{type=CLOSED, error=null}
2026-10-07 12:22:38.917 22905-22905 BufferQueueConsumer     com.example.aquascope                D  [ImageReader-1920x1440f21m2-22905-4](id:597900000008,api:0,p:-1,c:22905) disconnect
2026-10-07 12:22:38.918 22905-22905 BufferQueueConsumer     com.example.aquascope                D  [ImageReader-720x720f23m4-22905-2](id:597900000006,api:0,p:-1,c:22905) disconnect
2026-10-07 12:22:38.918 22905-22905 BufferQueueConsumer     com.example.aquascope                D  [ImageReader-720x720f23m4-22905-3](id:597900000007,api:0,p:-1,c:22905) disconnect
2026-10-07 12:22:40.054 22905-10693 log                     com.example.aquascope                D  use codebook 4096 do nothing
2026-10-07 12:22:40.054 22905-10693 log                     com.example.aquascope                E  Optical send embedding: [1167, 1609, 3465, 2847, 2777, 3305, 303, 209, 1681, 2130, 2841, 4051, 3845, 2429, 12, 617, 3817, 859, 2294, 1988, 2251, 309, 2178, 1089, 2491, 525, 2127, 2060, 1450, 2566, 1328, 2897, 540, 3172, 147, 630, 3198, 2548, 1089, 409, 3043, 568, 496, 810, 1718, 2943, 3920, 2738, 1248, 2629, 3162, 1312, 1066, 3280, 1958, 242, 858, 3874, 3023, 3813, 3090, 3563, 407, 355]
        2026-10-07 12:22:40.055 22905-10693 FlashTransmitter        com.example.aquascope                I  Transmitting 64 tokens -> 96 bytes -> 960 line bits (295.2s burst)
2026-10-07 12:27:39.273 22905-10693 log                     com.example.aquascope                E  Optical transmission completed successfully
2026-10-07 12:27:39.301 22905-22905 CameraOrientationUtil   com.example.aquascope                D  getRelativeImageRotation: destRotationDegrees=0, sourceRotationDegrees=90, isOppositeFacing=true, result=90
        2026-10-07 12:27:39.302 22905-22905 CameraOrientationUtil   com.example.aquascope                D  getRelativeImageRotation: destRotationDegrees=0, sourceRotationDegrees=90, isOppositeFacing=true, result=90
        2026-10-07 12:27:39.307 22905-22905 BufferQueueConsumer     com.example.aquascope                D  [](id:597900000009,api:0,p:-1,c:22905) connect: controlledByApp=true
        2026-10-07 12:27:39.309 22905-22905 CameraOrientationUtil   com.example.aquascope                D  getRelativeImageRotation: destRotationDegrees=0, sourceRotationDegrees=90, isOppositeFacing=true, result=90
        2026-10-07 12:27:39.309 22905-22905 BufferQueueConsumer     com.example.aquascope                D  [](id:59790000000a,api:0,p:-1,c:22905) connect: controlledByApp=true
        2026-10-07 12:27:39.309 22905-22905 CameraOrientationUtil   com.example.aquascope                D  getRelativeImageRotation: destRotationDegrees=0, sourceRotationDegrees=90, isOppositeFacing=true, result=90
        2026-10-07 12:27:39.310 22905-22905 DeferrableSurface       com.example.aquascope                D  Surface created[total_surfaces=1, used_surfaces=0](androidx.camera.core.impl.ImmediateSurface@80ae5e6}
        2026-10-07 12:27:39.314 22905-22905 BufferQueueConsumer     com.example.aquascope                D  [](id:59790000000b,api:0,p:-1,c:22905) connect: controlledByApp=true
        2026-10-07 12:27:39.314 22905-22905 DeferrableSurface       com.example.aquascope                D  Surface created[total_surfaces=2, used_surfaces=0](androidx.camera.core.impl.ImmediateSurface@920ec3}
        2026-10-07 12:27:39.316 22905-22905 log                     com.example.aquascope                D  camera bound with 2 use case(s) (optical analysis alongside preview/capture)
        2026-10-07 12:27:39.316 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Use case androidx.camera.core.ImageCapture-1be26293-e89f-4861-9724-7e750e588d9a213696843 ACTIVE
2026-10-07 12:27:39.317 22905-10693 log                     com.example.aquascope                D  Camera session rebound after optical transmission
2026-10-07 12:27:39.317 22905-22946 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [] for camera: 0
        2026-10-07 12:27:39.319 22905-22905 log                     com.example.aquascope                E  MoE feedback: medium=OPTICAL ack=true snr=50 nackRate=0.00 consecNacks=0
        2026-10-07 12:27:39.319 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Use case androidx.camera.core.ImageAnalysis-ba4bffa1-730f-4b3c-8d16-fbac0ec039da43822420 ACTIVE
2026-10-07 12:27:39.319 22905-22946 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [] for camera: 0
        2026-10-07 12:27:39.319 22905-22905 log                     com.example.aquascope                E  Bob is the receiver; optical selection means receive-only, not transmit
2026-10-07 12:27:39.319 22905-22905 log                     com.example.aquascope                E  OpticalReceiver: armed, waiting for a flash burst
2026-10-07 12:27:39.321 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Use case androidx.camera.core.ImageCapture-1be26293-e89f-4861-9724-7e750e588d9a213696843 ACTIVE
2026-10-07 12:27:39.321 22905-22946 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [] for camera: 0
        2026-10-07 12:27:39.322 22905-22905 DeferrableSurface       com.example.aquascope                D  surface closed,  useCount=0 closed=true androidx.camera.core.impl.ImmediateSurface@80ae5e6
2026-10-07 12:27:39.322 22905-22905 DeferrableSurface       com.example.aquascope                D  Surface terminated[total_surfaces=1, used_surfaces=0](androidx.camera.core.impl.ImmediateSurface@80ae5e6}
        2026-10-07 12:27:39.322 22905-22905 DeferrableSurface       com.example.aquascope                D  surface closed,  useCount=0 closed=true androidx.camera.core.impl.ImmediateSurface@920ec3
2026-10-07 12:27:39.322 22905-22905 DeferrableSurface       com.example.aquascope                D  Surface terminated[total_surfaces=0, used_surfaces=0](androidx.camera.core.impl.ImmediateSurface@920ec3}
        2026-10-07 12:27:39.324 22905-22946 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [] for camera: 0
        2026-10-07 12:27:39.327 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Use cases [androidx.camera.core.ImageAnalysis-ba4bffa1-730f-4b3c-8d16-fbac0ec039da43822420, androidx.camera.core.ImageCapture-1be26293-e89f-4861-9724-7e750e588d9a213696843] now ATTACHED
2026-10-07 12:27:39.328 22905-22946 UseCaseAttachState      com.example.aquascope                D  All use case: [androidx.camera.core.ImageCapture-1be26293-e89f-4861-9724-7e750e588d9a213696843, androidx.camera.core.ImageAnalysis-ba4bffa1-730f-4b3c-8d16-fbac0ec039da43822420] for camera: 0
        2026-10-07 12:27:39.329 22905-22946 Camera2CameraImpl       com.example.aquascope                D  mMeteringRepeating is ATTACHED, SessionConfig Surfaces: 2, CaptureConfig Surfaces: 1
        2026-10-07 12:27:39.330 22905-22946 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [androidx.camera.core.ImageCapture-1be26293-e89f-4861-9724-7e750e588d9a213696843, androidx.camera.core.ImageAnalysis-ba4bffa1-730f-4b3c-8d16-fbac0ec039da43822420] for camera: 0
        2026-10-07 12:27:39.331 22905-22905 CameraOrientationUtil   com.example.aquascope                D  getRelativeImageRotation: destRotationDegrees=0, sourceRotationDegrees=90, isOppositeFacing=true, result=90
        2026-10-07 12:27:39.331 22905-22905 CameraOrientationUtil   com.example.aquascope                D  getRelativeImageRotation: destRotationDegrees=0, sourceRotationDegrees=90, isOppositeFacing=true, result=90
        2026-10-07 12:27:39.333 22905-22905 BufferQueueConsumer     com.example.aquascope                D  [](id:59790000000c,api:0,p:-1,c:22905) connect: controlledByApp=true
        2026-10-07 12:27:39.333 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Resetting Capture Session
2026-10-07 12:27:39.333 22905-22905 CameraOrientationUtil   com.example.aquascope                D  getRelativeImageRotation: destRotationDegrees=0, sourceRotationDegrees=90, isOppositeFacing=true, result=90
        2026-10-07 12:27:39.333 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Releasing session in state INITIALIZED
2026-10-07 12:27:39.334 22905-22905 BufferQueueConsumer     com.example.aquascope                D  [](id:59790000000d,api:0,p:-1,c:22905) connect: controlledByApp=true
        2026-10-07 12:27:39.334 22905-22905 CameraOrientationUtil   com.example.aquascope                D  getRelativeImageRotation: destRotationDegrees=0, sourceRotationDegrees=90, isOppositeFacing=true, result=90
        2026-10-07 12:27:39.334 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Attempting to force open the camera.
        2026-10-07 12:27:39.334 22905-22946 CameraStateRegistry     com.example.aquascope                D  tryOpenCamera(Camera@bd6def[id=0]) [Available Cameras: 1, Already Open: false (Previous state: CLOSED)] --> SUCCESS
2026-10-07 12:27:39.334 22905-22905 DeferrableSurface       com.example.aquascope                D  Surface created[total_surfaces=1, used_surfaces=0](androidx.camera.core.impl.ImmediateSurface@13f2817}
        2026-10-07 12:27:39.335 22905-22946 CameraStateRegistry     com.example.aquascope                D  Recalculating open cameras:
Camera                                       State
                                                                                                    -------------------------------------------------------------------
Camera@a0800e8[id=1]                         UNKNOWN
Camera@bbc0e3d[id=2]                         UNKNOWN
Camera@bd6def[id=0]                          OPENING
                                                                                                    -------------------------------------------------------------------
Open count: 1 (Max allowed: 1)
        2026-10-07 12:27:39.335 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Opening camera.
        2026-10-07 12:27:39.335 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Transitioning camera internal state: INITIALIZED --> OPENING
2026-10-07 12:27:39.336 22905-22946 CameraStateMachine      com.example.aquascope                D  New public camera state CameraState{type=OPENING, error=null} from OPENING and null
        2026-10-07 12:27:39.336 22905-22946 CameraStateMachine      com.example.aquascope                D  Publishing new public camera state CameraState{type=OPENING, error=null}
2026-10-07 12:27:39.336 22905-22946 UseCaseAttachState      com.example.aquascope                D  All use case: [androidx.camera.core.ImageCapture-1be26293-e89f-4861-9724-7e750e588d9a213696843, androidx.camera.core.ImageAnalysis-ba4bffa1-730f-4b3c-8d16-fbac0ec039da43822420] for camera: 0
        2026-10-07 12:27:39.337 22905-22905 BufferQueueConsumer     com.example.aquascope                D  [](id:59790000000e,api:0,p:-1,c:22905) connect: controlledByApp=true
        2026-10-07 12:27:39.337 22905-22905 DeferrableSurface       com.example.aquascope                D  Surface created[total_surfaces=2, used_surfaces=0](androidx.camera.core.impl.ImmediateSurface@749c70}
        2026-10-07 12:27:39.338 22905-22905 log                     com.example.aquascope                D  camera bound with 2 use case(s) (optical analysis alongside preview/capture)
        2026-10-07 12:27:39.338 22905-22905 log                     com.example.aquascope                E  Optical receive armed
2026-10-07 12:27:39.339 22905-22905 BufferQueueConsumer     com.example.aquascope                D  [ImageReader-720x720f23m4-22905-5](id:597900000009,api:0,p:-1,c:22905) disconnect
2026-10-07 12:27:39.339 22905-22905 BufferQueueConsumer     com.example.aquascope                D  [ImageReader-720x720f23m4-22905-6](id:59790000000a,api:0,p:-1,c:22905) disconnect
2026-10-07 12:27:39.339 22905-22905 BufferQueueConsumer     com.example.aquascope                D  [ImageReader-1920x1440f21m2-22905-7](id:59790000000b,api:0,p:-1,c:22905) disconnect
2026-10-07 12:27:39.347 22905-22974 CameraManagerGlobal     com.example.aquascope                I  Camera 0 facing CAMERA_FACING_BACK state now CAMERA_STATE_OPENING for client com.example.aquascope API Level 2 User Id 0Device Id 0
        2026-10-07 12:27:39.349 22905-22974 CameraManagerGlobal     com.example.aquascope                I  postSingleUpdate device: camera id 0 status STATUS_NOT_AVAILABLE
2026-10-07 12:27:39.349 22905-22974 CameraManagerGlobal     com.example.aquascope                I  postSingleUpdate device: camera id 0 status STATUS_NOT_AVAILABLE
2026-10-07 12:27:39.349 22905-22974 CameraManagerGlobal     com.example.aquascope                I  postSingleUpdate device: camera id 0 status STATUS_NOT_AVAILABLE
2026-10-07 12:27:39.368 22905-22974 CameraManagerGlobal     com.example.aquascope                I  Camera 0 facing CAMERA_FACING_BACK state now CAMERA_STATE_OPEN for client com.example.aquascope API Level 2 User Id 0Device Id 0
        2026-10-07 12:27:39.369 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Use case androidx.camera.core.ImageAnalysis-ba4bffa1-730f-4b3c-8d16-fbac0ec039da43822420 ACTIVE
2026-10-07 12:27:39.369 22905-22946 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [androidx.camera.core.ImageCapture-1be26293-e89f-4861-9724-7e750e588d9a213696843, androidx.camera.core.ImageAnalysis-ba4bffa1-730f-4b3c-8d16-fbac0ec039da43822420] for camera: 0
        2026-10-07 12:27:39.370 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Use case androidx.camera.core.ImageCapture-1be26293-e89f-4861-9724-7e750e588d9a213696843 ACTIVE
2026-10-07 12:27:39.370 22905-22946 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [androidx.camera.core.ImageCapture-1be26293-e89f-4861-9724-7e750e588d9a213696843, androidx.camera.core.ImageAnalysis-ba4bffa1-730f-4b3c-8d16-fbac0ec039da43822420] for camera: 0
        2026-10-07 12:27:39.370 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Use cases [androidx.camera.core.ImageAnalysis-ba4bffa1-730f-4b3c-8d16-fbac0ec039da43822420, androidx.camera.core.ImageCapture-1be26293-e89f-4861-9724-7e750e588d9a213696843] now DETACHED for camera
2026-10-07 12:27:39.371 22905-22946 UseCaseAttachState      com.example.aquascope                D  All use case: [] for camera: 0
        2026-10-07 12:27:39.371 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Resetting Capture Session
2026-10-07 12:27:39.371 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Releasing session in state OPENING
2026-10-07 12:27:39.373 22905-22946 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [] for camera: 0
        2026-10-07 12:27:39.373 22905-22946 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [] for camera: 0
        2026-10-07 12:27:39.374 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Closing camera.
        2026-10-07 12:27:39.374 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Transitioning camera internal state: OPENING --> CLOSING
2026-10-07 12:27:39.374 22905-22946 CameraStateRegistry     com.example.aquascope                D  Recalculating open cameras:
Camera                                       State
                                                                                                    -------------------------------------------------------------------
Camera@a0800e8[id=1]                         UNKNOWN
Camera@bbc0e3d[id=2]                         UNKNOWN
Camera@bd6def[id=0]                          CLOSING
                                                                                                    -------------------------------------------------------------------
Open count: 1 (Max allowed: 1)
        2026-10-07 12:27:39.374 22905-22946 CameraStateMachine      com.example.aquascope                D  New public camera state CameraState{type=CLOSING, error=null} from CLOSING and null
        2026-10-07 12:27:39.374 22905-22946 CameraStateMachine      com.example.aquascope                D  Publishing new public camera state CameraState{type=CLOSING, error=null}
2026-10-07 12:27:39.374 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Use case androidx.camera.core.ImageCapture-024c6543-52c5-4865-9927-e6aa224ad09d103060283 ACTIVE
2026-10-07 12:27:39.378 22905-22946 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [] for camera: 0
        2026-10-07 12:27:39.379 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Use case androidx.camera.core.ImageAnalysis-ba4bffa1-730f-4b3c-8d16-fbac0ec039da43822420 ACTIVE
2026-10-07 12:27:39.380 22905-22946 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [] for camera: 0
        2026-10-07 12:27:39.382 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Use case androidx.camera.core.ImageCapture-024c6543-52c5-4865-9927-e6aa224ad09d103060283 ACTIVE
2026-10-07 12:27:39.382 22905-22946 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [] for camera: 0
        2026-10-07 12:27:39.383 22905-22946 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [] for camera: 0
        2026-10-07 12:27:39.385 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Use cases [androidx.camera.core.ImageAnalysis-ba4bffa1-730f-4b3c-8d16-fbac0ec039da43822420, androidx.camera.core.ImageCapture-024c6543-52c5-4865-9927-e6aa224ad09d103060283] now ATTACHED
2026-10-07 12:27:39.386 22905-22946 UseCaseAttachState      com.example.aquascope                D  All use case: [androidx.camera.core.ImageCapture-024c6543-52c5-4865-9927-e6aa224ad09d103060283, androidx.camera.core.ImageAnalysis-ba4bffa1-730f-4b3c-8d16-fbac0ec039da43822420] for camera: 0
        2026-10-07 12:27:39.386 22905-22946 Camera2CameraImpl       com.example.aquascope                D  mMeteringRepeating is ATTACHED, SessionConfig Surfaces: 2, CaptureConfig Surfaces: 1
        2026-10-07 12:27:39.387 22905-22946 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [androidx.camera.core.ImageCapture-024c6543-52c5-4865-9927-e6aa224ad09d103060283, androidx.camera.core.ImageAnalysis-ba4bffa1-730f-4b3c-8d16-fbac0ec039da43822420] for camera: 0
        2026-10-07 12:27:39.388 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Resetting Capture Session
2026-10-07 12:27:39.388 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Releasing session in state CLOSING
2026-10-07 12:27:39.388 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Transitioning camera internal state: CLOSING --> REOPENING
2026-10-07 12:27:39.388 22905-22946 CameraStateRegistry     com.example.aquascope                D  Recalculating open cameras:
Camera                                       State
                                                                                                    -------------------------------------------------------------------
Camera@a0800e8[id=1]                         UNKNOWN
Camera@bbc0e3d[id=2]                         UNKNOWN
Camera@bd6def[id=0]                          OPENING
                                                                                                    -------------------------------------------------------------------
Open count: 1 (Max allowed: 1)
        2026-10-07 12:27:39.388 22905-22946 CameraStateMachine      com.example.aquascope                D  New public camera state CameraState{type=OPENING, error=null} from OPENING and null
        2026-10-07 12:27:39.388 22905-22946 CameraStateMachine      com.example.aquascope                D  Publishing new public camera state CameraState{type=OPENING, error=null}
2026-10-07 12:27:39.388 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Use case androidx.camera.core.ImageAnalysis-ba4bffa1-730f-4b3c-8d16-fbac0ec039da43822420 ACTIVE
2026-10-07 12:27:39.389 22905-22946 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [androidx.camera.core.ImageCapture-024c6543-52c5-4865-9927-e6aa224ad09d103060283, androidx.camera.core.ImageAnalysis-ba4bffa1-730f-4b3c-8d16-fbac0ec039da43822420] for camera: 0
        2026-10-07 12:27:39.390 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Use case androidx.camera.core.ImageCapture-024c6543-52c5-4865-9927-e6aa224ad09d103060283 ACTIVE
2026-10-07 12:27:39.390 22905-22946 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [androidx.camera.core.ImageCapture-024c6543-52c5-4865-9927-e6aa224ad09d103060283, androidx.camera.core.ImageAnalysis-ba4bffa1-730f-4b3c-8d16-fbac0ec039da43822420] for camera: 0
        2026-10-07 12:27:39.391 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} CameraDevice.onOpened()
2026-10-07 12:27:39.391 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Transitioning camera internal state: REOPENING --> OPENED
2026-10-07 12:27:39.391 22905-22946 CameraStateRegistry     com.example.aquascope                D  Recalculating open cameras:
Camera                                       State
                                                                                                    -------------------------------------------------------------------
Camera@a0800e8[id=1]                         UNKNOWN
Camera@bbc0e3d[id=2]                         UNKNOWN
Camera@bd6def[id=0]                          OPEN
                                                                                                    -------------------------------------------------------------------
Open count: 1 (Max allowed: 1)
        2026-10-07 12:27:39.391 22905-22946 CameraStateMachine      com.example.aquascope                D  New public camera state CameraState{type=OPEN, error=null} from OPEN and null
        2026-10-07 12:27:39.391 22905-22946 CameraStateMachine      com.example.aquascope                D  Publishing new public camera state CameraState{type=OPEN, error=null}
2026-10-07 12:27:39.391 22905-22946 UseCaseAttachState      com.example.aquascope                D  All use case: [androidx.camera.core.ImageCapture-024c6543-52c5-4865-9927-e6aa224ad09d103060283, androidx.camera.core.ImageAnalysis-ba4bffa1-730f-4b3c-8d16-fbac0ec039da43822420] for camera: 0
        2026-10-07 12:27:39.392 22905-22946 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [androidx.camera.core.ImageCapture-024c6543-52c5-4865-9927-e6aa224ad09d103060283, androidx.camera.core.ImageAnalysis-ba4bffa1-730f-4b3c-8d16-fbac0ec039da43822420] for camera: 0
        2026-10-07 12:27:39.393 22905-22946 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [androidx.camera.core.ImageCapture-024c6543-52c5-4865-9927-e6aa224ad09d103060283, androidx.camera.core.ImageAnalysis-ba4bffa1-730f-4b3c-8d16-fbac0ec039da43822420] for camera: 0
        2026-10-07 12:27:39.393 22905-22946 SyncCaptureSessionBase  com.example.aquascope                D  [androidx.camera.camera2.internal.SynchronizedCaptureSessionBaseImpl@3656cd] getSurface...done
2026-10-07 12:27:39.394 22905-22946 CaptureSession          com.example.aquascope                D  Opening capture session.
2026-10-07 12:27:39.395 22905-22946 DeferrableSurface       com.example.aquascope                D  New surface in use[total_surfaces=2, used_surfaces=1](androidx.camera.core.impl.ImmediateSurface@749c70}
        2026-10-07 12:27:39.395 22905-22946 DeferrableSurface       com.example.aquascope                D  use count+1, useCount=1 androidx.camera.core.impl.ImmediateSurface@749c70
2026-10-07 12:27:39.395 22905-22946 DeferrableSurface       com.example.aquascope                D  New surface in use[total_surfaces=2, used_surfaces=2](androidx.camera.core.impl.ImmediateSurface@13f2817}
        2026-10-07 12:27:39.395 22905-22946 DeferrableSurface       com.example.aquascope                D  use count+1, useCount=1 androidx.camera.core.impl.ImmediateSurface@13f2817
        2026-10-07 12:27:39.432 22905-22946 CaptureSession          com.example.aquascope                D  Attempting to send capture request onConfigured
2026-10-07 12:27:39.432 22905-22946 CaptureSession          com.example.aquascope                D  Issuing request for session.
2026-10-07 12:27:39.434 22905-22946 CaptureSession          com.example.aquascope                D  CameraCaptureSession.onConfigured() mState=OPENED
2026-10-07 12:27:39.434 22905-22946 CaptureSession          com.example.aquascope                D  CameraCaptureSession.onReady() OPENED
2026-10-07 12:27:39.436 22905-22974 CameraManagerGlobal     com.example.aquascope                I  Camera 0 facing CAMERA_FACING_BACK state now CAMERA_STATE_ACTIVE for client com.example.aquascope API Level 2 User Id 0Device Id 0
        2026-10-07 12:27:40.012 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[0,75][1080,2340] display=[0,75][1080,2340] parentFrame=[0,0][0,0]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:27:40.047 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 0.0 fps, 0.0 samples per 300ms symbol - TOO FEW to resolve the eye pattern, raise the symbol duration | analysis 720x720 0.62ms of 33535.8ms interval
2026-10-07 12:27:42.501 22905-22905 InsetsController        com.example.aquascope                I  onStateChanged: host=com.example.aquascope/com.example.root.ffttest2.MainActivity, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {3 mType=ime mFrame=[0,0][0,0] mAttachedInsets=null mVisible=false mFlags=INVALID mSideHint=NONE mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=false mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:27:42.504 22905-22905 InsetsController        com.example.aquascope                I  onStateChanged: host=com.example.aquascope/com.example.root.ffttest2.MainActivity, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=false mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {3 mType=ime mFrame=[0,0][0,0] mAttachedInsets=null mVisible=false mFlags=INVALID mSideHint=NONE mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=false mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:27:42.504 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[0,75][1080,2340] display=[0,75][1080,2340] parentFrame=[0,0][0,0]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:27:45.305 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 22.2 fps, 6.7 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.60ms of 45.0ms interval
2026-10-07 12:27:50.560 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 22.4 fps, 6.7 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.50ms of 44.7ms interval
2026-10-07 12:27:55.825 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 22.6 fps, 6.8 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.59ms of 44.3ms interval
2026-10-07 12:28:01.089 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 22.2 fps, 6.7 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.71ms of 45.1ms interval
2026-10-07 12:28:06.331 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 22.2 fps, 6.7 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.60ms of 45.0ms interval
2026-10-07 12:28:11.588 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 22.3 fps, 6.7 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.29ms of 44.9ms interval
2026-10-07 12:28:16.847 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 22.6 fps, 6.8 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.41ms of 44.3ms interval
2026-10-07 12:28:17.293 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: burst detected, ambient=31.309977 signal=69.015305 threshold=48.277374 frameRate=22.4fps
2026-10-07 12:28:17.293 22905-10637 log                     com.example.aquascope                D  ContextVector: turbidity=0.7258674 noise=0.0 luminance=0.57903713 laplacian=0.05397445
        2026-10-07 12:28:17.294 22905-10637 log                     com.example.aquascope                E  MoE: optical pulse -> contrast=0.55 turbidity=0.73 quality=27
        2026-10-07 12:28:21.980 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 26.9 fps, 8.1 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.46ms of 37.2ms interval
2026-10-07 12:28:26.992 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 27.1 fps, 8.1 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.28ms of 36.9ms interval
2026-10-07 12:28:31.997 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 27.9 fps, 8.4 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.32ms of 35.9ms interval
2026-10-07 12:28:37.029 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 27.0 fps, 8.1 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.37ms of 37.0ms interval
2026-10-07 12:28:40.755 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:28:40.758 22905-22905 GestureDetector         com.example.aquascope                I  obtain mCurrentDownEvent. id: 414935537 caller: android.view.ScaleGestureDetector.onTouchEvent:311 com.jjoe64.graphview.Viewport.onTouchEvent:655 com.jjoe64.graphview.GraphView.onTouchEvent:448
        2026-10-07 12:28:40.759 22905-22905 GestureDetector         com.example.aquascope                I  obtain mCurrentDownEvent. id: 414935537 caller: com.jjoe64.graphview.Viewport.onTouchEvent:656 com.jjoe64.graphview.GraphView.onTouchEvent:448 android.view.View.performOnTouchCallback:17597
        2026-10-07 12:28:40.759 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=high hint, reason=touch, vri=VRI[MainActivity]@17e0af0
2026-10-07 12:28:40.778 22905-22905 GestureDetector         com.example.aquascope                I  obtain mCurrentMotionEventRaw. action: 2 id: 412619989
        2026-10-07 12:28:40.779 22905-22905 GestureDetector         com.example.aquascope                I  obtain mCurrentMotionEventRaw. action: 2 id: 412619989
        2026-10-07 12:28:40.941 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:28:41.934 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:28:41.964 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:28:41.986 22905-22905 log                     com.example.aquascope                E  MoE auto-switch enabled; completed transmissions will alternate medium
2026-10-07 12:28:42.035 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 27.4 fps, 8.2 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.54ms of 36.6ms interval
2026-10-07 12:28:43.102 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:28:43.175 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:28:43.179 22905-22905 log                     com.example.aquascope                E  MoE auto-switch disabled; medium stays on the manual selection
2026-10-07 12:28:46.175 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=no preference, reason=boost timeout, vri=VRI[MainActivity]@17e0af0
2026-10-07 12:28:47.035 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 28.5 fps, 8.5 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.40ms of 35.1ms interval
2026-10-07 12:28:50.211 22905-22911 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 43MB AllocSpace bytes, 278(4448KB) LOS objects, 46% free, 54MB/101MB, paused 437us,5.420ms total 48.691ms
2026-10-07 12:28:52.067 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 27.4 fps, 8.2 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.59ms of 36.6ms interval
2026-10-07 12:28:57.088 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 26.5 fps, 8.0 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.56ms of 37.7ms interval
2026-10-07 12:29:02.094 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 27.2 fps, 8.2 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.45ms of 36.8ms interval
2026-10-07 12:29:07.109 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 26.2 fps, 7.9 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.38ms of 38.1ms interval
2026-10-07 12:29:12.113 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 27.4 fps, 8.2 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.55ms of 36.4ms interval
2026-10-07 12:29:17.124 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 27.0 fps, 8.1 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.28ms of 37.0ms interval
2026-10-07 12:29:22.138 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 27.0 fps, 8.1 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.29ms of 37.0ms interval
2026-10-07 12:29:27.152 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 27.0 fps, 8.1 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.41ms of 37.0ms interval
2026-10-07 12:29:32.173 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 26.9 fps, 8.1 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.45ms of 37.2ms interval
2026-10-07 12:29:37.185 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 27.0 fps, 8.1 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.57ms of 37.0ms interval
2026-10-07 12:29:37.568 22905-22911 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 40MB AllocSpace bytes, 279(7732KB) LOS objects, 48% free, 50MB/97MB, paused 489us,5.078ms total 47.979ms
2026-10-07 12:29:42.199 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 26.9 fps, 8.1 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.58ms of 37.1ms interval
2026-10-07 12:29:45.721 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:29:45.722 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=high hint, reason=touch, vri=VRI[MainActivity]@17e0af0
2026-10-07 12:29:46.017 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:29:47.214 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 25.8 fps, 7.7 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.40ms of 38.8ms interval
2026-10-07 12:29:48.819 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:29:48.917 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:29:48.920 22905-22905 log                     com.example.aquascope                E  camera capture
2026-10-07 12:29:48.920 22905-22905 CameraOrientationUtil   com.example.aquascope                D  getRelativeImageRotation: destRotationDegrees=0, sourceRotationDegrees=90, isOppositeFacing=true, result=90
        2026-10-07 12:29:48.920 22905-22905 ImageCapture            com.example.aquascope                D  Send image capture request [current, pending] = [0, 1]
        2026-10-07 12:29:48.921 22905-22905 ImageCapture            com.example.aquascope                D  issueTakePicture
2026-10-07 12:29:48.923 22905-22949 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Issue capture request
2026-10-07 12:29:48.923 22905-22949 CaptureSession          com.example.aquascope                D  Issuing capture request.
2026-10-07 12:29:49.227 22905-10946 Kumiho-Kumiho           com.example.aquascope                I  getPackageName: com.example.aquascope
2026-10-07 12:29:49.228 22905-10946 skia                    com.example.aquascope                D  SkJpegCodec::onGetPixels + (1920, 1440)
        2026-10-07 12:29:49.258 22905-10946 skia                    com.example.aquascope                D  SkJpegCodec::onGetPixels -
        2026-10-07 12:29:49.258 22905-10946 Kumiho-Kumiho           com.example.aquascope                I  getPackageName: com.example.aquascope
2026-10-07 12:29:49.258 22905-10946 log                     com.example.aquascope                E  bitmap size 1440 1920
        2026-10-07 12:29:49.295 22905-10946 log                     com.example.aquascope                D  Turbidity: variance below floor (0.05865557 < 2.0), frame has no usable detail - check camera or recalibrate baseline
2026-10-07 12:29:49.295 22905-10946 log                     com.example.aquascope                D  Turbidity: variance=0.05865557 meanLum=0.44657758 turbidity=0.9997654
        2026-10-07 12:29:49.295 22905-10946 log                     com.example.aquascope                D  ContextVector: turbidity=0.9997654 noise=0.0 luminance=0.44657758 laplacian=0.05865557
        2026-10-07 12:29:51.078 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:29:51.158 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:29:52.263 22905-10637 log                     com.example.aquascope                E  OpticalReceiver: camera delivers 26.4 fps, 7.9 samples per 300ms symbol - sufficient to demodulate | analysis 720x720 0.39ms of 37.9ms interval
2026-10-07 12:29:53.607 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:29:53.615 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:29:53.622 22905-22905 log                     com.example.aquascope                E  manual medium selection: ACOUSTIC (auto-switch off)
2026-10-07 12:29:54.435 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:29:54.758 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:29:57.758 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=no preference, reason=boost timeout, vri=VRI[MainActivity]@17e0af0
2026-10-07 12:30:01.804 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:30:01.805 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=high hint, reason=touch, vri=VRI[MainActivity]@17e0af0
2026-10-07 12:30:01.966 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:30:04.966 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=no preference, reason=boost timeout, vri=VRI[MainActivity]@17e0af0
2026-10-07 12:30:08.048 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:30:08.050 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=high hint, reason=touch, vri=VRI[MainActivity]@17e0af0
2026-10-07 12:30:08.056 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:30:08.081 22905-22905 WindowManager           com.example.aquascope                I  WindowManagerGlobal#addView, ty=1002, view=android.widget.PopupWindow$PopupDecorView{d2e847f V.E...... R.....I. 0,0-0,0}, caller=android.view.WindowManagerImpl.addView:167 android.widget.PopupWindow.invokePopup:1686 android.widget.PopupWindow.showAsDropDown:1493
        2026-10-07 12:30:08.082 22905-22905 ViewRootImpl            com.example.aquascope                I  dVRR is disabled
2026-10-07 12:30:08.082 22905-22919 NativeCust...ncyManager com.example.aquascope                D  [NativeCFMS] BpCustomFrequencyManager::BpCustomFrequencyManager()
2026-10-07 12:30:08.091 22905-22905 InputTransport          com.example.aquascope                D  Input channel constructed: '181ff9a', fd=183
        2026-10-07 12:30:08.092 22905-22905 InsetsController        com.example.aquascope                I  onStateChanged: host=PopupWindow:539819e, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=false mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=false mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:30:08.092 22905-22905 VRI[PopupW...e]@2674c4c com.example.aquascope                I  synced displayState. AttachInfo displayState=2
2026-10-07 12:30:08.092 22905-22905 VRI[PopupW...e]@2674c4c com.example.aquascope                I  setView = android.widget.PopupWindow$PopupDecorView@d2e847f IsHRR=false mFlingFrameRateChange=0 TM=true
        2026-10-07 12:30:08.093 22905-22905 WindowOnBackDispatcher  com.example.aquascope                D  setTopOnBackInvokedCallback (unwrapped): android.view.ViewRootImpl$$ExternalSyntheticLambda16@fe2e138
2026-10-07 12:30:08.101 22905-22905 BufferQueueConsumer     com.example.aquascope                D  [](id:59790000000f,api:0,p:-1,c:22905) connect: controlledByApp=false
        2026-10-07 12:30:08.102 22905-22905 BLASTBufferQueue        com.example.aquascope                I  [VRI[PopupWindow:539819e]@2674c4c#2](f:0,a:0,s:0) constructor()
2026-10-07 12:30:08.102 22905-22905 BLASTBufferQueue_Java   com.example.aquascope                I  new BLASTBufferQueue, mName= VRI[PopupWindow:539819e]@2674c4c mNativeObject= 0xb4000078aafc8270 caller= android.view.ViewRootImpl.updateBlastSurfaceIfNeeded:3603 android.view.ViewRootImpl.relayoutWindow:11813 android.view.ViewRootImpl.performTraversals:4840 android.view.ViewRootImpl.doTraversal:3959 android.view.ViewRootImpl$TraversalRunnable.run:13034 android.view.Choreographer$CallbackRecord.run:1961 android.view.Choreographer$CallbackRecord.run:1970 android.view.Choreographer.doCallbacks:1423 android.view.Choreographer.doFrame:1348 android.view.Choreographer$FrameDisplayEventReceiver.run:1930
        2026-10-07 12:30:08.102 22905-22905 BLASTBufferQueue_Java   com.example.aquascope                I  update, w= 709 h= 945 mName = VRI[PopupWindow:539819e]@2674c4c mNativeObject= 0xb4000078aafc8270 sc.mNativeObject= 0xb4000077ea0f1050 format= -3 caller= android.view.ViewRootImpl.updateBlastSurfaceIfNeeded:3608 android.view.ViewRootImpl.relayoutWindow:11813 android.view.ViewRootImpl.performTraversals:4840 android.view.ViewRootImpl.doTraversal:3959 android.view.ViewRootImpl$TraversalRunnable.run:13034 android.view.Choreographer$CallbackRecord.run:1961
        2026-10-07 12:30:08.102 22905-22905 BLASTBufferQueue        com.example.aquascope                I  [VRI[PopupWindow:539819e]@2674c4c#2](f:0,a:0,s:0) update width=709 height=945 format=-3 mTransformHint=0
        2026-10-07 12:30:08.103 22905-22905 VRI[PopupW...e]@2674c4c com.example.aquascope                I  Relayout returned: old=(506,1333,945,2008) new=(506,1333,945,2008) relayoutAsync=false req=(439,675)0 dur=8 res=0x3 s={true 0xb40000787a12cca0} ch=true seqId=0
        2026-10-07 12:30:08.104 22905-22905 VRI[PopupW...e]@2674c4c com.example.aquascope                I  performConfigurationChange setNightDimText nightDimLevel=0
        2026-10-07 12:30:08.107 22905-22905 VRI[PopupW...e]@2674c4c com.example.aquascope                D  mThreadedRenderer.initialize() mSurface={isValid=true 0xb40000787a12cca0} hwInitialized=true
        2026-10-07 12:30:08.112 22905-22905 AbsListView             com.example.aquascope                D   in onLayout changed
2026-10-07 12:30:08.113 22905-22905 VRI[PopupW...e]@2674c4c com.example.aquascope                D  reportNextDraw android.view.ViewRootImpl.performTraversals:5507 android.view.ViewRootImpl.doTraversal:3959 android.view.ViewRootImpl$TraversalRunnable.run:13034 android.view.Choreographer$CallbackRecord.run:1961 android.view.Choreographer$CallbackRecord.run:1970
        2026-10-07 12:30:08.115 22905-22905 VRI[PopupW...e]@2674c4c com.example.aquascope                D  Setup new sync=wmsSync-VRI[PopupWindow:539819e]@2674c4c#8
        2026-10-07 12:30:08.115 22905-22905 VRI[PopupW...e]@2674c4c com.example.aquascope                I  Creating new active sync group VRI[PopupWindow:539819e]@2674c4c#9
        2026-10-07 12:30:08.115 22905-22905 VRI[PopupW...e]@2674c4c com.example.aquascope                D  registerCallbacksForSync syncBuffer=false
2026-10-07 12:30:08.118 22905-22954 VRI[PopupW...e]@2674c4c com.example.aquascope                D  Received frameDrawingCallback syncResult=0 frameNum=1.
        2026-10-07 12:30:08.119 22905-22954 VRI[PopupW...e]@2674c4c com.example.aquascope                I  mWNT: t=0xb4000076fa1a7420 mBlastBufferQueue=0xb4000078aafc8270 fn= 1 HdrRenderState mRenderHdrSdrRatio=1.0 caller= android.view.ViewRootImpl$12.onFrameDraw:15561 android.view.ThreadedRenderer$1.onFrameDraw:751 <bottom of call stack>
        2026-10-07 12:30:08.119 22905-22954 VRI[PopupW...e]@2674c4c com.example.aquascope                I  Setting up sync and frameCommitCallback
2026-10-07 12:30:08.121 22905-22919 BLASTBufferQueue        com.example.aquascope                I  [VRI[PopupWindow:539819e]@2674c4c#2](f:0,a:0,s:0) onFrameAvailable the first frame is available
2026-10-07 12:30:08.121 22905-22919 BLASTBufferQueue        com.example.aquascope                I  [VRI[PopupWindow:539819e]@2674c4c#2](f:0,a:1,s:0) acquireNextBufferLocked size=709x945 mFrameNumber=1 applyTransaction=true mTimestamp=222282152908094(auto) mPendingTransactions.size=0 graphicBufferId=98376225914926 transform=0
        2026-10-07 12:30:08.121 22905-22919 VRI[PopupW...e]@2674c4c com.example.aquascope                I  Received frameCommittedCallback lastAttemptedDrawFrameNum=1 didProduceBuffer=true
        2026-10-07 12:30:08.122 22905-22919 HWUI                    com.example.aquascope                D  CFMS:: SetUp Pid : 22905    Tid : 22919
        2026-10-07 12:30:08.122 22905-22905 VRI[PopupW...e]@2674c4c com.example.aquascope                D  reportDrawFinished seqId=0
2026-10-07 12:30:08.124 22905-22919 HWUI                    com.example.aquascope                D  HWUI - treat SMPTE_170M as sRGB
2026-10-07 12:30:08.125 22905-22905 VRI[PopupW...e]@2674c4c com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[506,1333][945,2008] display=[-100000,-100000][100000,100000] parentFrame=[0,0][0,0] attachedFrame=[0,75][1080,2340]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:30:08.125 22905-22905 VRI[PopupW...e]@2674c4c com.example.aquascope                I  handleResized mSyncSeqId = 0
2026-10-07 12:30:08.125 22905-22905 VRI[PopupW...e]@2674c4c com.example.aquascope                D  reportNextDraw android.view.ViewRootImpl.handleResized:2995 android.view.ViewRootImpl.-$$Nest$mhandleResized:0 android.view.ViewRootImpl$W.resized:14200 android.app.servertransaction.WindowStateResizeItem.execute:81 android.app.servertransaction.WindowStateTransactionItem.execute:62
        2026-10-07 12:30:08.125 22905-22905 ImeFocusController      com.example.aquascope                I  onPreWindowFocus: skipped hasWindowFocus=false mHasImeFocus=true
        2026-10-07 12:30:08.126 22905-22905 ImeFocusController      com.example.aquascope                I  onPostWindowFocus: skipped hasWindowFocus=false mHasImeFocus=true
        2026-10-07 12:30:08.132 22905-22905 VRI[PopupW...e]@2674c4c com.example.aquascope                D  Setup new sync=wmsSync-VRI[PopupWindow:539819e]@2674c4c#10
        2026-10-07 12:30:08.132 22905-22905 VRI[PopupW...e]@2674c4c com.example.aquascope                I  Creating new active sync group VRI[PopupWindow:539819e]@2674c4c#11
        2026-10-07 12:30:08.133 22905-22905 VRI[PopupW...e]@2674c4c com.example.aquascope                D  registerCallbacksForSync syncBuffer=false
2026-10-07 12:30:08.133 22905-22953 VRI[PopupW...e]@2674c4c com.example.aquascope                D  Received frameDrawingCallback syncResult=0 frameNum=2.
        2026-10-07 12:30:08.133 22905-22953 VRI[PopupW...e]@2674c4c com.example.aquascope                I  Setting up sync and frameCommitCallback
2026-10-07 12:30:08.137 22905-22919 VRI[PopupW...e]@2674c4c com.example.aquascope                I  Received frameCommittedCallback lastAttemptedDrawFrameNum=2 didProduceBuffer=true
        2026-10-07 12:30:08.137 22905-22905 VRI[PopupW...e]@2674c4c com.example.aquascope                D  reportDrawFinished seqId=0
2026-10-07 12:30:08.156 22905-22905 VRI[PopupW...e]@2674c4c com.example.aquascope                D  mThreadedRenderer.initializeIfNeeded()#2 mSurface={isValid=true 0xb40000787a12cca0}
        2026-10-07 12:30:08.156 22905-22905 ImeFocusController      com.example.aquascope                I  onPreWindowFocus: skipped hasWindowFocus=true mHasImeFocus=false
        2026-10-07 12:30:08.156 22905-22905 ImeFocusController      com.example.aquascope                I  onPostWindowFocus: skipped hasWindowFocus=true mHasImeFocus=false
        2026-10-07 12:30:11.057 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=no preference, reason=boost timeout, vri=VRI[MainActivity]@17e0af0
2026-10-07 12:30:16.576 22905-22905 VRI[PopupW...e]@2674c4c com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:30:16.577 22905-22905 VRI[PopupW...e]@2674c4c com.example.aquascope                I  call setFrameRateCategory for touch hint category=high hint, reason=touch, vri=VRI[PopupWindow:539819e]@2674c4c
2026-10-07 12:30:16.647 22905-22905 VRI[PopupW...e]@2674c4c com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:30:16.648 22905-22905 AbsListView             com.example.aquascope                D  onTouchUp() mTouchMode : 0
        2026-10-07 12:30:16.716 22905-22905 BLASTBufferQueue_Java   com.example.aquascope                I  update, w= 709 h= 945 mName = VRI[PopupWindow:539819e]@2674c4c mNativeObject= 0xb4000078aafc8270 sc.mNativeObject= 0xb4000077ea0f1050 format= -3 caller= android.view.ViewRootImpl.updateBlastSurfaceIfNeeded:3592 android.view.ViewRootImpl.relayoutWindow:11813 android.view.ViewRootImpl.performTraversals:4840 android.view.ViewRootImpl.doTraversal:3959 android.view.ViewRootImpl$TraversalRunnable.run:13034 android.view.Choreographer$CallbackRecord.run:1961
        2026-10-07 12:30:16.716 22905-22905 VRI[PopupW...e]@2674c4c com.example.aquascope                I  Relayout returned: old=(506,1333,945,2008) new=(506,1333,945,2008) relayoutAsync=true req=(439,675)0 dur=0 res=0x0 s={true 0xb40000787a12cca0} ch=false seqId=0
        2026-10-07 12:30:16.718 22905-22905 VRI[PopupW...e]@2674c4c com.example.aquascope                I  registerCallbackForPendingTransactions
2026-10-07 12:30:16.723 22905-22954 VRI[PopupW...e]@2674c4c com.example.aquascope                I  mWNT: t=0xb4000076fa1bac50 mBlastBufferQueue=0xb4000078aafc8270 fn= 22 HdrRenderState mRenderHdrSdrRatio=1.0 caller= android.view.ViewRootImpl$10.onFrameDraw:6615 android.view.ViewRootImpl$4.onFrameDraw:2485 android.view.ThreadedRenderer$1.onFrameDraw:751
        2026-10-07 12:30:16.757 22905-22905 log                     com.example.aquascope                D  onstop
2026-10-07 12:30:17.058 22905-22905 log                     com.example.aquascope                E  Bob is the receiver; starting the acoustic listen loop
2026-10-07 12:30:17.061 22905-10967 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/5_1791355874776 Bob-AdaptParams-0.txt false
        2026-10-07 12:30:17.064 22905-10967 log                     com.example.aquascope                E  appendtofile Bob-Feedback-log.txt false
        2026-10-07 12:30:17.065 22905-10967 fifo                    com.example.aquascope                E  Bob-Sounding-0-0
        2026-10-07 12:30:17.066 22905-10967 recorder                com.example.aquascope                E  minbuffersize 7680
        2026-10-07 12:30:17.093 22905-10967 len                     com.example.aquascope                E  sig length 72000,Sounding
2026-10-07 12:30:17.094 22905-10971 recorder                com.example.aquascope                E  start 1
        2026-10-07 12:30:17.367 22905-22905 ImeFocusController      com.example.aquascope                I  onPreWindowFocus: skipped hasWindowFocus=false mHasImeFocus=false
        2026-10-07 12:30:17.368 22905-22905 ImeFocusController      com.example.aquascope                I  onPostWindowFocus: skipped hasWindowFocus=false mHasImeFocus=false
        2026-10-07 12:30:17.369 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                D  mThreadedRenderer.initializeIfNeeded()#2 mSurface={isValid=true 0xb40000787a100880}
        2026-10-07 12:30:17.373 22905-22905 InputMethodManagerUtils com.example.aquascope                D  startInputInner - Id : 0
        2026-10-07 12:30:17.373 22905-22905 InputMethodManager      com.example.aquascope                I  startInputInner - IInputMethodManagerGlobalInvoker.startInputOrWindowGainedFocus
2026-10-07 12:30:17.376 22905-22905 Choreographer           com.example.aquascope                I  Skipped 36 frames!  The application may be doing too much work on its main thread.
        2026-10-07 12:30:17.377 22905-22905 WindowManager           com.example.aquascope                I  WindowManagerGlobal#removeView, ty=1002, view=android.widget.PopupWindow$PopupDecorView{d2e847f V.E...... R.....ID 0,0-439,675}, caller=android.view.WindowManagerGlobal.removeView:654 android.view.WindowManagerImpl.removeViewImmediate:254 android.widget.PopupWindow.dismissImmediate:2380
        2026-10-07 12:30:17.377 22905-22905 WindowOnBackDispatcher  com.example.aquascope                D  setTopOnBackInvokedCallback (unwrapped): null
        2026-10-07 12:30:17.379 22905-23114 InputTransport          com.example.aquascope                D  Input channel constructed: 'ClientS', fd=298
        2026-10-07 12:30:17.382 22905-22905 VRI[PopupW...e]@2674c4c com.example.aquascope                I  dispatchDetachedFromWindow
2026-10-07 12:30:17.383 22905-22905 BLASTBufferQueue        com.example.aquascope                I  [VRI[PopupWindow:539819e]@2674c4c#2](f:0,a:3,s:0) destructor()
2026-10-07 12:30:17.383 22905-22905 BufferQueueConsumer     com.example.aquascope                D  [VRI[PopupWindow:539819e]@2674c4c#2(BLAST Consumer)2](id:59790000000f,api:0,p:-1,c:22905) disconnect
2026-10-07 12:30:17.389 22905-22905 InputTransport          com.example.aquascope                D  Input channel destroyed: '181ff9a', fd=183
        2026-10-07 12:30:17.396 22905-22905 InsetsController        com.example.aquascope                D  hide(ime())
        2026-10-07 12:30:17.397 22905-22905 ImeTracker              com.example.aquascope                I  com.example.aquascope:905149c9: onCancelled at PHASE_CLIENT_ALREADY_HIDDEN
2026-10-07 12:30:17.653 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[0,75][1080,2340] display=[0,75][1080,2340] parentFrame=[0,0][0,0]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:30:18.345 22905-22912 InputTransport          com.example.aquascope                D  Input channel destroyed: 'ClientS', fd=298
        2026-10-07 12:30:18.808 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:18.808 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:18.809 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:18.809 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:18.811 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:18.816 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:18.816 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:18.816 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:18.817 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:18.817 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:18.817 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:18.817 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:18.817 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:18.817 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:18.818 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:18.819 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:18.824 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:18.824 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:18.824 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:18.824 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:18.825 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:18.825 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:19.008 22905-10967 log                     com.example.aquascope                E  Listening... (0.08)
        2026-10-07 12:30:19.285 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:19.285 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:19.285 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:19.285 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:19.287 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:19.292 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:19.292 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:19.293 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:19.293 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:19.293 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:19.293 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:19.293 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:19.293 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:19.294 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:19.294 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:19.296 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:19.305 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:19.305 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:19.305 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:19.305 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:19.306 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:19.306 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:19.462 22905-10967 log                     com.example.aquascope                E  Listening... (0.21)
        2026-10-07 12:30:19.545 22905-22911 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 12MB AllocSpace bytes, 113(9632KB) LOS objects, 46% free, 55MB/103MB, paused 244us,19.376ms total 74.550ms
2026-10-07 12:30:19.871 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:19.871 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:19.871 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:19.871 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:19.875 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:19.880 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:19.880 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:19.881 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:19.881 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:19.882 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:19.882 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:19.882 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:19.883 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:19.883 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:19.884 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:19.886 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:19.891 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:19.891 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:19.891 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:19.891 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:19.891 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:19.891 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:20.052 22905-10967 log                     com.example.aquascope                E  Listening... (0.21)
        2026-10-07 12:30:20.169 22905-22905 InsetsController        com.example.aquascope                I  onStateChanged: host=com.example.aquascope/com.example.root.ffttest2.MainActivity, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {3 mType=ime mFrame=[0,0][0,0] mAttachedInsets=null mVisible=false mFlags=INVALID mSideHint=NONE mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=false mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:30:20.173 22905-22905 InsetsController        com.example.aquascope                I  onStateChanged: host=com.example.aquascope/com.example.root.ffttest2.MainActivity, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=false mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {3 mType=ime mFrame=[0,0][0,0] mAttachedInsets=null mVisible=false mFlags=INVALID mSideHint=NONE mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=false mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:30:20.174 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[0,75][1080,2340] display=[0,75][1080,2340] parentFrame=[0,0][0,0]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:30:20.348 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:20.348 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:20.348 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:20.348 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:20.350 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:20.355 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:20.355 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:20.356 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:20.356 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:20.356 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:20.356 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:20.356 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:20.356 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:20.357 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:20.357 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:20.358 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:20.364 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:20.364 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:20.365 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:20.365 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:20.365 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:20.366 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:20.472 22905-10967 log                     com.example.aquascope                E  Listening... (0.13)
        2026-10-07 12:30:20.826 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:20.826 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:20.827 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:20.827 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:20.828 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:20.834 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:20.834 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:20.834 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:20.834 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:20.835 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:20.835 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:20.835 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:20.835 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:20.836 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:20.837 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:20.841 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:20.846 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:20.846 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:20.847 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:20.847 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:20.847 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:20.847 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:20.955 22905-10967 log                     com.example.aquascope                E  Listening... (0.12)
        2026-10-07 12:30:21.283 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:21.283 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:21.284 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:21.284 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:21.285 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:21.290 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:21.290 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:21.290 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:21.290 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:21.291 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:21.291 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:21.291 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:21.291 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:21.291 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:21.292 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:21.293 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:21.298 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:21.298 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:21.298 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:21.298 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:21.299 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:21.299 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:21.461 22905-22911 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 34MB AllocSpace bytes, 278(20MB) LOS objects, 34% free, 92MB/140MB, paused 195us,5.588ms total 36.359ms
2026-10-07 12:30:21.462 22905-10967 log                     com.example.aquascope                E  Listening... (0.12)
        2026-10-07 12:30:21.849 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:21.849 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:21.849 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:21.849 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:21.851 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:21.855 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:21.855 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:21.856 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:21.856 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:21.856 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:21.856 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:21.856 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:21.856 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:21.857 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:21.857 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:21.858 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:21.863 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:21.863 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:21.863 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:21.863 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:21.864 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:21.864 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:21.921 22905-10967 log                     com.example.aquascope                E  Listening... (0.08)
        2026-10-07 12:30:22.348 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:22.348 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:22.349 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:22.349 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:22.350 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:22.355 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:22.355 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:22.355 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:22.355 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:22.356 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:22.356 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:22.356 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:22.356 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:22.356 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:22.356 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:22.358 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:22.363 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:22.363 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:22.363 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:22.363 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:22.363 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:22.363 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:22.508 22905-10967 log                     com.example.aquascope                E  Listening... (0.11)
        2026-10-07 12:30:22.813 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:22.813 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:22.814 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:22.814 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:22.815 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:22.820 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:22.820 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:22.820 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:22.820 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:22.821 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:22.821 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:22.821 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:22.821 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:22.821 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:22.821 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:22.823 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:22.828 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:22.828 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:22.828 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:22.828 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:22.828 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:22.828 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:22.927 22905-10967 log                     com.example.aquascope                E  Listening... (0.03)
        2026-10-07 12:30:23.075 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:30:23.077 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=high hint, reason=touch, vri=VRI[MainActivity]@17e0af0
2026-10-07 12:30:23.275 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:23.275 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:23.276 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:23.276 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:23.277 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:23.284 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:23.284 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:23.284 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:23.284 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:23.285 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:23.285 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:23.285 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:23.285 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:23.285 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:23.286 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:23.288 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:23.293 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:23.293 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:23.293 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:23.293 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:23.293 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:23.293 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:23.323 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:30:23.409 22905-10967 log                     com.example.aquascope                E  Listening... (0.03)
        2026-10-07 12:30:23.877 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:23.877 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:23.877 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:23.877 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:23.878 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:23.885 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:23.885 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:23.885 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:23.885 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:23.885 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:23.885 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:23.885 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:23.885 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:23.886 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:23.886 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:23.887 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:23.892 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:23.892 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:23.893 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:23.893 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:23.893 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:23.893 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:24.089 22905-10967 log                     com.example.aquascope                E  Listening... (0.17)
        2026-10-07 12:30:24.133 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:30:24.330 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:24.330 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:24.330 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:24.330 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:24.332 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:24.337 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:24.337 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:24.338 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:24.338 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:24.338 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:24.338 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:24.338 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:24.338 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:24.339 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:24.339 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:24.341 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:24.348 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:24.348 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:24.348 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:24.348 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:24.349 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:24.349 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:24.397 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:30:24.459 22905-10967 log                     com.example.aquascope                E  Listening... (0.17)
        2026-10-07 12:30:24.698 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:30:24.763 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:30:24.800 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:24.800 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:24.800 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:24.800 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:24.802 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:24.806 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:24.806 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:24.807 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:24.807 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:24.807 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:24.807 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:24.807 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:24.807 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:24.808 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:24.808 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:24.809 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:24.815 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:24.815 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:24.815 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:24.815 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:24.815 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:24.815 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:24.921 22905-10967 log                     com.example.aquascope                E  Listening... (0.10)
        2026-10-07 12:30:25.310 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:25.310 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:25.311 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:25.311 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:25.313 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:25.321 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:25.321 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:25.322 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:25.322 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:25.322 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:25.322 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:25.322 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:25.322 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:25.323 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:25.323 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:25.325 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:25.329 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:25.329 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:25.330 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:25.330 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:25.330 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:25.330 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:25.488 22905-10967 log                     com.example.aquascope                E  Listening... (0.06)
        2026-10-07 12:30:25.873 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:25.873 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:25.873 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:25.873 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:25.874 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:25.879 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:25.879 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:25.880 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:25.880 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:25.881 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:25.881 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:25.881 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:25.881 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:25.882 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:25.882 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:25.884 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:25.889 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:25.889 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:25.890 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:25.890 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:25.890 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:25.890 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:26.040 22905-10967 log                     com.example.aquascope                E  Listening... (0.07)
        2026-10-07 12:30:26.328 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:26.328 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:26.328 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:26.328 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:26.330 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:26.335 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:26.335 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:26.335 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:26.335 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:26.335 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:26.335 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:26.335 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:26.335 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:26.336 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:26.336 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:26.337 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:26.342 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:26.342 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:26.342 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:26.342 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:26.343 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:26.343 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:26.447 22905-10967 log                     com.example.aquascope                E  Listening... (0.08)
        2026-10-07 12:30:26.816 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:26.816 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:26.816 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:26.816 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:26.818 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:26.824 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:26.824 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:26.825 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:26.825 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:26.825 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:26.825 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:26.825 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:26.825 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:26.825 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:26.826 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:26.827 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:26.832 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:26.832 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:26.832 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:26.832 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:26.833 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:26.833 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:26.986 22905-10967 log                     com.example.aquascope                E  Listening... (0.09)
        2026-10-07 12:30:27.293 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:27.293 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:27.293 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:27.293 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:27.295 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:27.300 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:27.300 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:27.300 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:27.300 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:27.301 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:27.301 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:27.301 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:27.301 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:27.301 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:27.301 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:27.303 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:27.308 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:27.308 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:27.308 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:27.308 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:27.308 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:27.308 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:27.410 22905-10967 log                     com.example.aquascope                E  Listening... (0.12)
        2026-10-07 12:30:27.566 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:30:27.650 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:30:27.659 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[0,75][1080,2340] display=[0,75][1080,2340] parentFrame=[0,0][0,0]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:30:27.861 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:27.861 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:27.862 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:27.862 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:27.863 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:27.869 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:27.869 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:27.869 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:27.869 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:27.869 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:27.869 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:27.869 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:27.870 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:27.870 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:27.870 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:27.872 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:27.877 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:27.877 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:27.877 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:27.877 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:27.877 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:27.877 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:27.990 22905-10967 log                     com.example.aquascope                E  Listening... (0.09)
        2026-10-07 12:30:28.338 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:28.338 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:28.338 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:28.338 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:28.339 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:28.344 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:28.344 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:28.345 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:28.345 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:28.345 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:28.345 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:28.345 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:28.345 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:28.345 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:28.346 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:28.347 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:28.352 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:28.352 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:28.352 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:28.352 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:28.353 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:28.353 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:28.409 22905-10967 log                     com.example.aquascope                E  Listening... (0.11)
        2026-10-07 12:30:28.866 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:28.866 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:28.866 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:28.866 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:28.868 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:28.881 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:28.881 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:28.881 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:28.881 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:28.882 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:28.882 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:28.882 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:28.882 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:28.882 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:28.884 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:28.889 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:28.899 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:28.899 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:28.899 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:28.899 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:28.899 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:28.899 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:29.071 22905-22905 InsetsController        com.example.aquascope                I  onStateChanged: host=com.example.aquascope/com.example.root.ffttest2.MainActivity, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {3 mType=ime mFrame=[0,0][0,0] mAttachedInsets=null mVisible=false mFlags=INVALID mSideHint=NONE mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:30:29.081 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                D  WindowInsets changed: navigationBars:[0,0,0,135]
        2026-10-07 12:30:29.087 22905-22905 ImeFocusController      com.example.aquascope                I  onPreWindowFocus: skipped hasWindowFocus=false mHasImeFocus=true
        2026-10-07 12:30:29.092 22905-22905 ImeFocusController      com.example.aquascope                I  onPostWindowFocus: skipped hasWindowFocus=false mHasImeFocus=true
        2026-10-07 12:30:29.139 22905-22911 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 34MB AllocSpace bytes, 281(22MB) LOS objects, 4% free, 99MB/103MB, paused 2.233ms,8.823ms total 152.258ms
2026-10-07 12:30:29.341 22905-10967 log                     com.example.aquascope                E  Listening... (0.18)
        2026-10-07 12:30:29.432 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:29.433 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:29.433 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:29.433 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:29.434 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:29.453 22905-22911 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 50MB AllocSpace bytes, 402(29MB) LOS objects, 35% free, 87MB/135MB, paused 3.848ms,5.031ms total 312.944ms
2026-10-07 12:30:29.479 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:29.479 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:29.480 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:29.480 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:29.480 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:29.480 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:29.480 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
2026-10-07 12:30:29.481 22905-10967 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
2026-10-07 12:30:29.481 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
2026-10-07 12:30:29.481 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
2026-10-07 12:30:29.483 22905-10967 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
2026-10-07 12:30:29.492 22905-10967 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
2026-10-07 12:30:29.492 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
2026-10-07 12:30:29.492 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
2026-10-07 12:30:29.493 22905-10967 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
2026-10-07 12:30:29.493 22905-10967 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
2026-10-07 12:30:29.493 22905-10967 FFT_DEBUG               com.example.aquascope                D  Cleaning up
2026-10-07 12:30:29.515 22905-22905 log                     com.example.aquascope                D  onpause
2026-10-07 12:30:29.527 22905-22905 SensorManager           com.example.aquascope                D  unregisterListener
2026-10-07 12:30:29.528 22905-22905 log                     com.example.aquascope                D  onstop
2026-10-07 12:30:29.528 22905-22905 thread                  com.example.aquascope                E  halting......
        2026-10-07 12:30:29.541 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  stopped(true) old = false
        2026-10-07 12:30:29.542 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                D  WindowStopped on com.example.aquascope/com.example.root.ffttest2.MainActivity set to true
        2026-10-07 12:30:29.553 22905-22919 HWUI                    com.example.aquascope                D  CacheManager::trimMemory(20)
2026-10-07 12:30:29.575 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Use cases [androidx.camera.core.ImageAnalysis-ba4bffa1-730f-4b3c-8d16-fbac0ec039da43822420, androidx.camera.core.ImageCapture-024c6543-52c5-4865-9927-e6aa224ad09d103060283] now DETACHED for camera
2026-10-07 12:30:29.585 22905-22946 UseCaseAttachState      com.example.aquascope                D  All use case: [] for camera: 0
        2026-10-07 12:30:29.597 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Resetting Capture Session
2026-10-07 12:30:29.628 22905-10967 log                     com.example.aquascope                E  Listening... (0.00)
        2026-10-07 12:30:29.629 22905-10967 System.err              com.example.aquascope                W  java.lang.InterruptedException
2026-10-07 12:30:29.629 22905-10967 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Native Method)
2026-10-07 12:30:29.629 22905-10967 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep0(Thread.java:689)
2026-10-07 12:30:29.629 22905-10967 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Thread.java:667)
2026-10-07 12:30:29.629 22905-10967 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Thread.java:580)
2026-10-07 12:30:29.629 22905-10967 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.OfflineRecorder.get_FIFO(OfflineRecorder.java:134)
2026-10-07 12:30:29.629 22905-10967 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.Utils.waitForChirp(Utils.java:1759)
2026-10-07 12:30:29.629 22905-10967 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.work(SendChirpAsyncTask.java:599)
2026-10-07 12:30:29.629 22905-10967 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.doInBackground(SendChirpAsyncTask.java:230)
2026-10-07 12:30:29.629 22905-10967 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.doInBackground(SendChirpAsyncTask.java:34)
2026-10-07 12:30:29.629 22905-10967 System.err              com.example.aquascope                W  	at android.os.AsyncTask$3.call(AsyncTask.java:396)
2026-10-07 12:30:29.629 22905-10967 System.err              com.example.aquascope                W  	at java.util.concurrent.FutureTask.run(FutureTask.java:328)
2026-10-07 12:30:29.629 22905-10967 System.err              com.example.aquascope                W  	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1100)
2026-10-07 12:30:29.629 22905-10967 System.err              com.example.aquascope                W  	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
2026-10-07 12:30:29.629 22905-10967 System.err              com.example.aquascope                W  	at java.lang.Thread.run(Thread.java:1572)
2026-10-07 12:30:29.643 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Releasing session in state OPENED
2026-10-07 12:30:29.643 22905-22946 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [] for camera: 0
        2026-10-07 12:30:29.646 22905-22919 BLASTBufferQueue        com.example.aquascope                I  [VRI[MainActivity]@17e0af0#0](f:0,a:2,s:0) destructor()
2026-10-07 12:30:29.647 22905-22919 BufferQueueConsumer     com.example.aquascope                D  [VRI[MainActivity]@17e0af0#0(BLAST Consumer)0](id:597900000000,api:0,p:-1,c:22905) disconnect
2026-10-07 12:30:29.647 22905-22919 HWUI                    com.example.aquascope                D  CacheManager::trimMemory(20)
2026-10-07 12:30:29.652 22905-22946 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [] for camera: 0
        2026-10-07 12:30:29.654 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Closing camera.
        2026-10-07 12:30:29.654 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Transitioning camera internal state: OPENED --> CLOSING
2026-10-07 12:30:29.655 22905-22946 CameraStateRegistry     com.example.aquascope                D  Recalculating open cameras:
Camera                                       State
                                                                                                    -------------------------------------------------------------------
Camera@a0800e8[id=1]                         UNKNOWN
Camera@bbc0e3d[id=2]                         UNKNOWN
Camera@bd6def[id=0]                          CLOSING
                                                                                                    -------------------------------------------------------------------
Open count: 1 (Max allowed: 1)
        2026-10-07 12:30:29.655 22905-22946 CameraStateMachine      com.example.aquascope                D  New public camera state CameraState{type=CLOSING, error=null} from CLOSING and null
        2026-10-07 12:30:29.655 22905-22946 CameraStateMachine      com.example.aquascope                D  Publishing new public camera state CameraState{type=CLOSING, error=null}
2026-10-07 12:30:29.656 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Resetting Capture Session
2026-10-07 12:30:29.657 22905-22946 Camera2CameraImpl       com.example.aquascope                D  {Camera@bd6def[id=0]} Releasing session in state CLOSING
2026-10-07 12:30:29.658 22905-22946 CaptureSession          com.example.aquascope                D  onSessionFinished()
2026-10-07 12:30:29.679 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  Relayout returned: old=(0,75,1080,2340) new=(0,75,1080,2340) relayoutAsync=false req=(1080,2265)4 dur=29 res=0x2 s={false 0x0} ch=false seqId=0
        2026-10-07 12:30:29.680 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                D  Not drawing due to not visible. Reason=View.INVISIBLE
2026-10-07 12:30:29.682 22905-22919 HWUI                    com.example.aquascope                D  CacheManager::trimMemory(20)
2026-10-07 12:30:29.715 22905-22905 InputTransport          com.example.aquascope                D  Input channel destroyed: 'ClientS', fd=186
        2026-10-07 12:30:29.741 22905-10971 thread                  com.example.aquascope                E  recording thread end elegantly......
        2026-10-07 12:30:29.774 22905-22905 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  handleAppVisibility mAppVisible = true visible = false
        2026-10-07 12:30:29.787 22905-22974 CameraManagerGlobal     com.example.aquascope                I  Camera 0 facing CAMERA_FACING_BACK state now CAMERA_STATE_IDLE for client com.example.aquascope API Level 2 User Id 0Device Id 0
        2026-10-07 12:30:29.812 22905-22905 BBA2                    com.example.aquascope                D  setIsFg isFg = false; delayValue 3999ms
2026-10-07 12:30:29.978 22905-22905 DeferrableSurface       com.example.aquascope                D  surface closed,  useCount=1 closed=true androidx.camera.core.impl.ImmediateSurface@13f2817
        2026-10-07 12:30:29.979 22905-22905 DeferrableSurface       com.example.aquascope                D  surface closed,  useCount=1 closed=true androidx.camera.core.impl.ImmediateSurface@749c70
2026-10-07 12:30:29.986 22905-22905 log                     com.example.aquascope                D  destroy!!!
        2026-10-07 12:30:29.987 22905-22905 WindowOnBackDispatcher  com.example.aquascope                D  setTopOnBackInvokedCallback (unwrapped): null
        ---------------------------- PROCESS ENDED (22905) for package com.example.aquascope ----------------------------
        ---------------------------- PROCESS STARTED (11133) for package com.example.aquascope ----------------------------
        2026-10-07 12:30:31.066 11133-11133 nativeloader            com.example.aquascope                D  Load libframework-connectivity-tiramisu-jni.so using APEX ns com_android_tethering for caller /apex/com.android.tethering/javalib/framework-connectivity-t.jar: ok
2026-10-07 12:30:31.071 11133-11133 ActivityThread          com.example.aquascope                D  setConscryptValidator
2026-10-07 12:30:31.073 11133-11133 ActivityThread          com.example.aquascope                D  setConscryptValidator - put
2026-10-07 12:30:31.123 11133-11133 CompatChangeReporter    com.example.aquascope                D  Compat change id reported: 171979766; UID 10501; state: ENABLED
2026-10-07 12:30:31.378 11133-11133 nativeloader            com.example.aquascope                D  Configuring clns-9 for other apk /data/app/~~q2lYqEkTFd7eE7Xp06EmZA==/com.example.aquascope-u9L4NFTcMQPc_Rs9PgUKyQ==/base.apk. target_sdk_version=31, uses_libraries=, library_path=/data/app/~~q2lYqEkTFd7eE7Xp06EmZA==/com.example.aquascope-u9L4NFTcMQPc_Rs9PgUKyQ==/lib/arm64:/data/app/~~q2lYqEkTFd7eE7Xp06EmZA==/com.example.aquascope-u9L4NFTcMQPc_Rs9PgUKyQ==/base.apk!/lib/arm64-v8a, permitted_path=/data:/mnt/expand:/data/user/0/com.example.aquascope
2026-10-07 12:30:31.395 11133-11133 GraphicsEnvironment     com.example.aquascope                V  Currently set values for:
        2026-10-07 12:30:31.395 11133-11133 GraphicsEnvironment     com.example.aquascope                V    angle_gl_driver_selection_pkgs=[]
        2026-10-07 12:30:31.395 11133-11133 GraphicsEnvironment     com.example.aquascope                V    angle_gl_driver_selection_values=[]
        2026-10-07 12:30:31.395 11133-11133 GraphicsEnvironment     com.example.aquascope                V  com.example.aquascope is not listed in per-application setting
2026-10-07 12:30:31.396 11133-11133 GraphicsEnvironment     com.example.aquascope                V  ANGLE allowlist from config: com.dreamgames.royalmatch com.dts.freefiremax com.dxx.firenow com.gramgames.mergedragons com.ludo.king com.mojang.minecraftpe com.my.defense com.nintendo.zaka com.os.airforce com.playrix.fishdomdd.gplay io.teslatech.callbreak jp.konami.prospia net.peakgames.toonblast
2026-10-07 12:30:31.396 11133-11133 GraphicsEnvironment     com.example.aquascope                V  No special selections for ANGLE, returning default driver choice
2026-10-07 12:30:31.396 11133-11133 GraphicsEnvironment     com.example.aquascope                V  App is not on the allowlist for updatable production driver.
2026-10-07 12:30:31.508 11133-11133 ActivityThread          com.example.aquascope                D  com.example.aquascope will use render engine as VK
2026-10-07 12:30:31.509 11133-11133 HWUI                    com.example.aquascope                D  HWUI - treat SMPTE_170M as sRGB
2026-10-07 12:30:31.513 11133-11220 DisplayManager          com.example.aquascope                I  Choreographer implicitly registered for the refresh rate.
2026-10-07 12:30:31.514 11133-11220 HWUI                    com.example.aquascope                D  CacheManager constructor. deviceInfo=(1080, 1920)
        2026-10-07 12:30:31.514 11133-11220 vulkan                  com.example.aquascope                D  searching for layers in '/data/app/~~q2lYqEkTFd7eE7Xp06EmZA==/com.example.aquascope-u9L4NFTcMQPc_Rs9PgUKyQ==/lib/arm64'
        2026-10-07 12:30:31.514 11133-11220 vulkan                  com.example.aquascope                D  searching for layers in '/data/app/~~q2lYqEkTFd7eE7Xp06EmZA==/com.example.aquascope-u9L4NFTcMQPc_Rs9PgUKyQ==/base.apk!/lib/arm64-v8a'
        2026-10-07 12:30:31.516 11133-11220 vulkan                  com.example.aquascope                D  searching for layers in '/data/app/~~q2lYqEkTFd7eE7Xp06EmZA==/com.example.aquascope-u9L4NFTcMQPc_Rs9PgUKyQ==/lib/arm64'
        2026-10-07 12:30:31.517 11133-11220 vulkan                  com.example.aquascope                D  searching for layers in '/data/app/~~q2lYqEkTFd7eE7Xp06EmZA==/com.example.aquascope-u9L4NFTcMQPc_Rs9PgUKyQ==/base.apk!/lib/arm64-v8a'
        2026-10-07 12:30:31.529 11133-11220 MMUD                    com.example.aquascope                D  mtk_memory_debug_init com.example.aquascope 0xb4000079977fe400
        2026-10-07 12:30:31.533 11133-11133 DesktopExperienceFlags  com.example.aquascope                D  Toggle override initialized to: false
        2026-10-07 12:30:31.536 11133-11235 ample.aquascope         com.example.aquascope                E  setpriority success to 100 : mali-cmar-backend
2026-10-07 12:30:31.551 11133-11133 ashmem                  com.example.aquascope                E  Pinning is deprecated since Android Q. Please use trim or other methods.
        2026-10-07 12:30:31.591 11133-11133 nativeloader            com.example.aquascope                D  Load /data/app/~~q2lYqEkTFd7eE7Xp06EmZA==/com.example.aquascope-u9L4NFTcMQPc_Rs9PgUKyQ==/base.apk!/lib/arm64-v8a/libnative-lib.so using class loader ns clns-9 (caller=/data/app/~~q2lYqEkTFd7eE7Xp06EmZA==/com.example.aquascope-u9L4NFTcMQPc_Rs9PgUKyQ==/base.apk!classes3.dex): ok
2026-10-07 12:30:31.615 11133-11133 DesktopModeFlags        com.example.aquascope                D  Toggle override initialized to: OVERRIDE_UNSET
2026-10-07 12:30:31.623 11133-11133 DecorView               com.example.aquascope                I  setWindowBackground: isPopOver=false color=fffafafa d=android.graphics.drawable.ColorDrawable@1219b35
2026-10-07 12:30:31.630 11133-11133 ample.aquascope         com.example.aquascope                I  hiddenapi: Accessing hidden method Landroid/view/View;->computeFitSystemWindows(Landroid/graphics/Rect;Landroid/graphics/Rect;)Z (runtime_flags=0, domain=platform, api=unsupported) from Landroidx/appcompat/widget/ViewUtils; (domain=app, TargetSdkVersion=31) using reflection: allowed
2026-10-07 12:30:31.630 11133-11133 ample.aquascope         com.example.aquascope                I  hiddenapi: Accessing hidden method Landroid/view/ViewGroup;->makeOptionalFitsSystemWindows()V (runtime_flags=0, domain=platform, api=unsupported) from Landroidx/appcompat/widget/ViewUtils; (domain=app, TargetSdkVersion=31) using reflection: allowed
2026-10-07 12:30:31.633 11133-11226 MMUD                    com.example.aquascope                D  get_leak_baseline 3246391296
        2026-10-07 12:30:32.035 11133-11133 Kumiho-Kumiho           com.example.aquascope                I  getPackageName: com.example.aquascope
2026-10-07 12:30:32.035 11133-11133 Kumiho-Kumiho           com.example.aquascope                E  updateList - failed to retrieve policy '/data/system/Kumiho/Kumiho.policy' retrying with default
2026-10-07 12:30:32.035 11133-11133 Kumiho-Kumiho           com.example.aquascope                D  updateList: inserting 'com.whatsapp' in the Kumiho client list
2026-10-07 12:30:32.035 11133-11133 Kumiho-Kumiho           com.example.aquascope                D  updateList: inserting 'com.facebook.orca' in the Kumiho client list
2026-10-07 12:30:32.035 11133-11133 Kumiho-Kumiho           com.example.aquascope                D  updateList: inserting 'org.telegram.messenger' in the Kumiho client list
2026-10-07 12:30:32.035 11133-11133 Kumiho-Kumiho           com.example.aquascope                D  updateList: inserting 'com.kakao.talk' in the Kumiho client list
2026-10-07 12:30:32.035 11133-11133 Kumiho-Kumiho           com.example.aquascope                D  updateList: inserting 'jp.naver.line.android' in the Kumiho client list
2026-10-07 12:30:32.035 11133-11133 Kumiho-Kumiho           com.example.aquascope                D  updateList: inserting 'com.discord' in the Kumiho client list
2026-10-07 12:30:32.035 11133-11133 Kumiho-Kumiho           com.example.aquascope                D  updateList: inserting 'com.tencent.mm' in the Kumiho client list
2026-10-07 12:30:32.035 11133-11133 Kumiho-Kumiho           com.example.aquascope                D  updateList: inserting 'com.snapchat.android' in the Kumiho client list
2026-10-07 12:30:32.038 11133-11133 Kumiho-Kumiho           com.example.aquascope                I  getPackageName: com.example.aquascope
2026-10-07 12:30:32.041 11133-11133 Kumiho-Kumiho           com.example.aquascope                I  getPackageName: com.example.aquascope
2026-10-07 12:30:32.043 11133-11133 Kumiho-Kumiho           com.example.aquascope                I  getPackageName: com.example.aquascope
2026-10-07 12:30:32.045 11133-11133 Kumiho-Kumiho           com.example.aquascope                I  getPackageName: com.example.aquascope
2026-10-07 12:30:32.048 11133-11133 Kumiho-Kumiho           com.example.aquascope                I  getPackageName: com.example.aquascope
2026-10-07 12:30:32.050 11133-11133 Kumiho-Kumiho           com.example.aquascope                I  getPackageName: com.example.aquascope
2026-10-07 12:30:32.052 11133-11133 Kumiho-Kumiho           com.example.aquascope                I  getPackageName: com.example.aquascope
2026-10-07 12:30:32.054 11133-11133 Kumiho-Kumiho           com.example.aquascope                I  getPackageName: com.example.aquascope
2026-10-07 12:30:32.054 11133-11133 skia                    com.example.aquascope                D  SkJpegCodec::onGetPixels + (1920, 1080)
        2026-10-07 12:30:32.065 11133-11133 skia                    com.example.aquascope                D  SkJpegCodec::onGetPixels -
        2026-10-07 12:30:32.066 11133-11133 Kumiho-Kumiho           com.example.aquascope                I  getPackageName: com.example.aquascope
2026-10-07 12:30:32.320 11133-11133 nativeloader            com.example.aquascope                D  Load /data/app/~~q2lYqEkTFd7eE7Xp06EmZA==/com.example.aquascope-u9L4NFTcMQPc_Rs9PgUKyQ==/base.apk!/lib/arm64-v8a/libpytorch_jni_lite.so using class loader ns clns-9 (caller=/data/app/~~q2lYqEkTFd7eE7Xp06EmZA==/com.example.aquascope-u9L4NFTcMQPc_Rs9PgUKyQ==/base.apk): ok
2026-10-07 12:30:32.321 11133-11133 nativeloader            com.example.aquascope                D  Load libtorch-code-gen.so using class loader ns clns-9 (caller=/data/app/~~q2lYqEkTFd7eE7Xp06EmZA==/com.example.aquascope-u9L4NFTcMQPc_Rs9PgUKyQ==/base.apk): dlopen failed: library "libtorch-code-gen.so" not found
2026-10-07 12:30:32.321 11133-11133 native                  com.example.aquascope                W  [W LegacyTypeDispatch.h:79] Warning: AutoNonVariableTypeMode is deprecated and will be removed in 1.10 release. For kernel implementations please use AutoDispatchBelowADInplaceOrView instead, If you are looking for a user facing API to enable running your inference-only workload, please use c10::InferenceMode. Using AutoDispatchBelowADInplaceOrView in user code is under risk of producing silent wrong result in some edge cases. See Note [AutoDispatchBelowAutograd] for more details. (function operator())
        2026-10-07 12:30:32.876 11133-11133 libc                    com.example.aquascope                W  Access denied finding property "ro.mediatek.platform"
        2026-10-07 12:30:32.877 11133-11133 libc                    com.example.aquascope                W  Access denied finding property "ro.chipname"
        2026-10-07 12:30:34.165 11133-11133 nativeloader            com.example.aquascope                D  Load /data/app/~~q2lYqEkTFd7eE7Xp06EmZA==/com.example.aquascope-u9L4NFTcMQPc_Rs9PgUKyQ==/base.apk!/lib/arm64-v8a/libfbjni.so using class loader ns clns-9 (caller=/data/app/~~q2lYqEkTFd7eE7Xp06EmZA==/com.example.aquascope-u9L4NFTcMQPc_Rs9PgUKyQ==/base.apk): ok
2026-10-07 12:30:36.197 11133-11133 ImageSegmentation       com.example.aquascope                D  default position oflite_optimized_seg_240p.ptl is 5
        2026-10-07 12:30:36.204 11133-11285 CameraManagerGlobal     com.example.aquascope                I  Connecting to camera service
2026-10-07 12:30:36.207 11133-11285 VendorTagDescriptor     com.example.aquascope                D  addVendorDescriptor: vendor tag id 8932615658386372252 added
2026-10-07 12:30:36.219 11133-11145 CameraManagerGlobal     com.example.aquascope                I  Camera 0 facing CAMERA_FACING_BACK state now CAMERA_STATE_CLOSED for client com.example.aquascope API Level 2 User Id 0Device Id 0
        2026-10-07 12:30:36.220 11133-11145 CameraManagerGlobal     com.example.aquascope                I  Camera 1 facing CAMERA_FACING_FRONT state now CAMERA_STATE_CLOSED for client com.samsung.android.smartface API Level 2 User Id 0Device Id 0
        2026-10-07 12:30:36.220 11133-11145 CameraManagerGlobal     com.example.aquascope                I  Camera 2 facing CAMERA_FACING_FRONT state now CAMERA_STATE_CLOSED for client android.system API Level 2 User Id 0Device Id 0
        2026-10-07 12:30:36.221 11133-11145 CameraManagerGlobal     com.example.aquascope                I  Camera 23 facing CAMERA_FACING_BACK state now CAMERA_STATE_CLOSED for client android.system API Level 2 User Id 0Device Id 0
        2026-10-07 12:30:36.221 11133-11145 CameraManagerGlobal     com.example.aquascope                I  Camera 3 facing CAMERA_FACING_FRONT state now CAMERA_STATE_CLOSED for client android.system API Level 2 User Id 0Device Id 0
        2026-10-07 12:30:36.221 11133-11145 CameraManagerGlobal     com.example.aquascope                I  Camera 50 facing CAMERA_FACING_BACK state now CAMERA_STATE_CLOSED for client android.system API Level 2 User Id 0Device Id 0
        2026-10-07 12:30:36.222 11133-11145 CameraManagerGlobal     com.example.aquascope                I  Camera 54 facing CAMERA_FACING_BACK state now CAMERA_STATE_CLOSED for client android.system API Level 2 User Id 0Device Id 0
        2026-10-07 12:30:36.260 11133-11285 CameraRepository        com.example.aquascope                D  Added camera: 0
        2026-10-07 12:30:36.275 11133-11285 Camera2CameraInfo       com.example.aquascope                I  Device Level: INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED
2026-10-07 12:30:36.276 11133-11285 CameraManager           com.example.aquascope                I  registerAvailabilityCallback: Is device callback = false
        2026-10-07 12:30:36.276 11133-11285 CameraManagerGlobal     com.example.aquascope                I  postSingleUpdate device: camera id 0 status STATUS_PRESENT
2026-10-07 12:30:36.276 11133-11285 CameraManagerGlobal     com.example.aquascope                I  postSingleUpdate device: camera id 1 status STATUS_PRESENT
2026-10-07 12:30:36.276 11133-11285 CameraManagerGlobal     com.example.aquascope                I  postSingleUpdate device: camera id 2 status STATUS_PRESENT
2026-10-07 12:30:36.276 11133-11285 CameraRepository        com.example.aquascope                D  Added camera: 1
        2026-10-07 12:30:36.277 11133-11285 Camera2CameraInfo       com.example.aquascope                I  Device Level: INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED
2026-10-07 12:30:36.277 11133-11285 CameraManager           com.example.aquascope                I  registerAvailabilityCallback: Is device callback = false
        2026-10-07 12:30:36.277 11133-11285 CameraManagerGlobal     com.example.aquascope                I  postSingleUpdate device: camera id 0 status STATUS_PRESENT
2026-10-07 12:30:36.278 11133-11285 CameraManagerGlobal     com.example.aquascope                I  postSingleUpdate device: camera id 1 status STATUS_PRESENT
2026-10-07 12:30:36.278 11133-11285 CameraManagerGlobal     com.example.aquascope                I  postSingleUpdate device: camera id 2 status STATUS_PRESENT
2026-10-07 12:30:36.278 11133-11285 CameraRepository        com.example.aquascope                D  Added camera: 2
        2026-10-07 12:30:36.278 11133-11285 Camera2CameraInfo       com.example.aquascope                I  Device Level: INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED
2026-10-07 12:30:36.279 11133-11285 CameraManager           com.example.aquascope                I  registerAvailabilityCallback: Is device callback = false
        2026-10-07 12:30:36.279 11133-11285 CameraManagerGlobal     com.example.aquascope                I  postSingleUpdate device: camera id 0 status STATUS_PRESENT
2026-10-07 12:30:36.279 11133-11285 CameraManagerGlobal     com.example.aquascope                I  postSingleUpdate device: camera id 1 status STATUS_PRESENT
2026-10-07 12:30:36.279 11133-11285 CameraManagerGlobal     com.example.aquascope                I  postSingleUpdate device: camera id 2 status STATUS_PRESENT
2026-10-07 12:30:36.280 11133-11285 CameraValidator         com.example.aquascope                D  Verifying camera lens facing on a34x, lensFacingInteger: null
        2026-10-07 12:30:36.435 11133-11133 ample.aquascope         com.example.aquascope                I  Waiting for a blocking GC NativeAlloc
2026-10-07 12:30:36.453 11133-11133 ample.aquascope         com.example.aquascope                I  WaitForGcToComplete blocked NativeAlloc on NativeAlloc for 17.574ms
2026-10-07 12:30:36.793 11133-11133 log                     com.example.aquascope                E  number of valid carriers 60 60 [1000,3950]
        2026-10-07 12:30:36.793 11133-11133 Kumiho-Kumiho           com.example.aquascope                I  getPackageName: com.example.aquascope
2026-10-07 12:30:36.793 11133-11133 skia                    com.example.aquascope                D  SkJpegCodec::onGetPixels + (1920, 1080)
        2026-10-07 12:30:36.804 11133-11133 skia                    com.example.aquascope                D  SkJpegCodec::onGetPixels -
        2026-10-07 12:30:36.804 11133-11133 Kumiho-Kumiho           com.example.aquascope                I  getPackageName: com.example.aquascope
2026-10-07 12:30:36.815 11133-11133 log                     com.example.aquascope                D  oncreate end
2026-10-07 12:30:36.820 11133-11133 log                     com.example.aquascope                D  onresume
2026-10-07 12:30:36.825 11133-11133 SensorManager           com.example.aquascope                D  registerListener :: 1936484718, Samsung Linear Acceleration Sensor, 20000, 0,
        2026-10-07 12:30:36.825 11133-11133 SensorManager           com.example.aquascope                D  registerListener :: 4, LSM6DSOTR Gyroscope, 20000, 0, com.example.root.ffttest2.MainActivity@cc693d4
2026-10-07 12:30:36.825 11133-11133 log                     com.example.aquascope                E  start startmethod from onResume
2026-10-07 12:30:36.826 11133-11133 log                     com.example.aquascope                E  Bob is the receiver; starting the acoustic listen loop
2026-10-07 12:30:36.835 11133-11221 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/ Bob-AdaptParams-0.txt false
        2026-10-07 12:30:36.838 11133-11221 log                     com.example.aquascope                E  appendtofile Bob-Feedback-log.txt false
        2026-10-07 12:30:36.839 11133-11133 ViewRootImpl            com.example.aquascope                I  dVRR is disabled
2026-10-07 12:30:36.840 11133-11133 HardwareRenderer        com.example.aquascope                D  onDisplayChanged. displayId=0 current wxh=1080x2340 mLargest wxh=0x0
        2026-10-07 12:30:36.840 11133-11133 HWUI                    com.example.aquascope                W  Unknown dataspace 0
        2026-10-07 12:30:36.840 11133-11133 HardwareRenderer        com.example.aquascope                D  Set largestWidth and largestHeight as logical resolution. (1080x2340)
        2026-10-07 12:30:36.840 11133-11220 HWUI                    com.example.aquascope                D  setMaxSurfaceArea requested wxh=(1080,2340) requestedSurfaceArea(2527200) mMaxSurfaceArea(2073600)
        2026-10-07 12:30:36.840 11133-11221 log                     com.example.aquascope                D  work 0
        2026-10-07 12:30:36.840 11133-11221 log                     com.example.aquascope                D  Bob-DataRx-0-0
        2026-10-07 12:30:36.841 11133-11220 NativeCust...ncyManager com.example.aquascope                D  [NativeCFMS] BpCustomFrequencyManager::BpCustomFrequencyManager()
2026-10-07 12:30:36.842 11133-11221 AudioSystem             com.example.aquascope                D  onNewServiceWithAdapter: media.audio_flinger service obtained 0xb4000077da0f1520
        2026-10-07 12:30:36.843 11133-11221 AudioSystem             com.example.aquascope                D  getService: IAudioFlingerService retrieved: 0xb4000078ba17fcf0  IAudioFlinger cached: 0xb4000077da0f1520
        2026-10-07 12:30:36.843 11133-11221 AudioSystem             com.example.aquascope                D  getClient: checking for service: 0xb4000077da0f1520
        2026-10-07 12:30:36.851 11133-11133 InsetsController        com.example.aquascope                D  Setting requestedVisibleTypes to 496 (was 503)
        2026-10-07 12:30:36.851 11133-11221 recorder                com.example.aquascope                E  minbuffersize 7680
        2026-10-07 12:30:36.851 11133-11133 InsetsController        com.example.aquascope                I  setRequestedVisibleTypes: visible=false, mask=statusBars navigationBars captionBar, host=com.example.aquascope/com.example.root.ffttest2.MainActivity, from=android.view.InsetsController.controlAnimationUnchecked:1646 android.view.InsetsController.applyAnimation:2278 android.view.InsetsController.applyAnimation:2207 android.view.InsetsController.hide:1581 android.view.InsetsController.hide:1521 android.view.ViewRootImpl.controlInsetsForCompatibility:4199 android.view.ViewRootImpl.setView:1992 android.view.WindowManagerGlobal.addView:606 android.view.WindowManagerImpl.addView:167 android.app.ActivityThread.handleResumeActivity:6758
        2026-10-07 12:30:36.857 11133-11133 InputTransport          com.example.aquascope                D  Input channel constructed: 'ebc66ae', fd=125
        2026-10-07 12:30:36.858 11133-11133 InsetsController        com.example.aquascope                I  onStateChanged: host=com.example.aquascope/com.example.root.ffttest2.MainActivity, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {3 mType=ime mFrame=[0,0][0,0] mAttachedInsets=null mVisible=false mFlags=INVALID mSideHint=NONE mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:30:36.860 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  synced displayState. AttachInfo displayState=2
2026-10-07 12:30:36.860 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  setView = com.android.internal.policy.DecorView@75f69ab IsHRR=false mFlingFrameRateChange=0 TM=true
        2026-10-07 12:30:36.861 11133-11133 IDS_TAG                 com.example.aquascope                I  Starting IDS observe window
2026-10-07 12:30:36.861 11133-11133 IDS_TAG                 com.example.aquascope                I  Getting Shared Preference for android.app.Application@54e7ad1 uid = 10501
2026-10-07 12:30:36.864 11133-11133 IDS_TAG                 com.example.aquascope                I  App android.app.Application@54e7ad1 has not finished training
2026-10-07 12:30:36.866 11133-11133 IDS_TAG                 com.example.aquascope                I  Closing IDS observe window
2026-10-07 12:30:36.866 11133-11133 IDS_TAG                 com.example.aquascope                I  Getting Shared Preference for android.app.Application@54e7ad1 uid = 10501
2026-10-07 12:30:36.866 11133-11133 IDS_TAG                 com.example.aquascope                I  IDS count updated to 2 for android.app.Application@54e7ad1
2026-10-07 12:30:36.866 11133-11133 Choreographer           com.example.aquascope                I  Skipped 594 frames!  The application may be doing too much work on its main thread.
        2026-10-07 12:30:36.883 11133-11300 recorder                com.example.aquascope                E  start 1
        2026-10-07 12:30:36.886 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                D  WindowInsets changed: 1080x2265 navigationBars:[0,0,0,135] mandatorySystemGestures:[0,34,0,135]
        2026-10-07 12:30:36.886 11133-11133 WindowOnBackDispatcher  com.example.aquascope                D  setTopOnBackInvokedCallback (unwrapped): android.view.ViewRootImpl$$ExternalSyntheticLambda16@d6dc338
2026-10-07 12:30:36.891 11133-11133 HWUI                    com.example.aquascope                I  Using FreeType backend (prop=Auto)
2026-10-07 12:30:36.897 11133-11221 log                     com.example.aquascope                E  sig length 960000,DataRx
2026-10-07 12:30:36.984 11133-11133 BufferQueueConsumer     com.example.aquascope                D  [](id:2b7d00000000,api:0,p:-1,c:11133) connect: controlledByApp=false
        2026-10-07 12:30:36.986 11133-11133 BLASTBufferQueue        com.example.aquascope                I  [VRI[MainActivity]@17e0af0#0](f:0,a:0,s:0) constructor()
2026-10-07 12:30:36.986 11133-11133 BLASTBufferQueue_Java   com.example.aquascope                I  new BLASTBufferQueue, mName= VRI[MainActivity]@17e0af0 mNativeObject= 0xb4000078aaf2e9f0 caller= android.view.ViewRootImpl.updateBlastSurfaceIfNeeded:3603 android.view.ViewRootImpl.relayoutWindow:11813 android.view.ViewRootImpl.performTraversals:4840 android.view.ViewRootImpl.doTraversal:3959 android.view.ViewRootImpl$TraversalRunnable.run:13034 android.view.Choreographer$CallbackRecord.run:1961 android.view.Choreographer$CallbackRecord.run:1970 android.view.Choreographer.doCallbacks:1423 android.view.Choreographer.doFrame:1348 android.view.Choreographer$FrameDisplayEventReceiver.run:1930
        2026-10-07 12:30:36.986 11133-11133 BLASTBufferQueue_Java   com.example.aquascope                I  update, w= 1080 h= 2265 mName = VRI[MainActivity]@17e0af0 mNativeObject= 0xb4000078aaf2e9f0 sc.mNativeObject= 0xb4000077ea0c6990 format= -1 caller= android.view.ViewRootImpl.updateBlastSurfaceIfNeeded:3608 android.view.ViewRootImpl.relayoutWindow:11813 android.view.ViewRootImpl.performTraversals:4840 android.view.ViewRootImpl.doTraversal:3959 android.view.ViewRootImpl$TraversalRunnable.run:13034 android.view.Choreographer$CallbackRecord.run:1961
        2026-10-07 12:30:36.987 11133-11133 BLASTBufferQueue        com.example.aquascope                I  [VRI[MainActivity]@17e0af0#0](f:0,a:0,s:0) update width=1080 height=2265 format=-1 mTransformHint=0
        2026-10-07 12:30:36.987 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  Relayout returned: old=(0,75,1080,2340) new=(0,75,1080,2340) relayoutAsync=false req=(1080,2265)0 dur=10 res=0x3 s={true 0xb40000787a100ef0} ch=true seqId=0
        2026-10-07 12:30:36.988 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  performConfigurationChange setNightDimText nightDimLevel=0
        2026-10-07 12:30:36.989 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                D  WindowInsets changed: navigationBars:null
        2026-10-07 12:30:36.989 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                D  mThreadedRenderer.initialize() mSurface={isValid=true 0xb40000787a100ef0} hwInitialized=true
        2026-10-07 12:30:37.030 11133-11140 ample.aquascope         com.example.aquascope                I  Compiler allocated 5313KB to compile void android.view.ViewRootImpl.performTraversals()
2026-10-07 12:30:37.047 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                D  reportNextDraw android.view.ViewRootImpl.performTraversals:5507 android.view.ViewRootImpl.doTraversal:3959 android.view.ViewRootImpl$TraversalRunnable.run:13034 android.view.Choreographer$CallbackRecord.run:1961 android.view.Choreographer$CallbackRecord.run:1970
        2026-10-07 12:30:37.059 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                D  Setup new sync=wmsSync-VRI[MainActivity]@17e0af0#0
        2026-10-07 12:30:37.060 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  Creating new active sync group VRI[MainActivity]@17e0af0#1
        2026-10-07 12:30:37.061 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                D  registerCallbacksForSync syncBuffer=false
2026-10-07 12:30:37.112 11133-11294 VRI[MainAc...y]@17e0af0 com.example.aquascope                D  Received frameDrawingCallback syncResult=0 frameNum=1.
        2026-10-07 12:30:37.112 11133-11220 ample.aquascope         com.example.aquascope                D  Can't load libmbrainSDK
        2026-10-07 12:30:37.113 11133-11220 ample.aquascope         com.example.aquascope                D  initMbrain failed
2026-10-07 12:30:37.113 11133-11294 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  mWNT: t=0xb4000076fa1a4140 mBlastBufferQueue=0xb4000078aaf2e9f0 fn= 1 HdrRenderState mRenderHdrSdrRatio=1.0 caller= android.view.ViewRootImpl$12.onFrameDraw:15561 android.view.ThreadedRenderer$1.onFrameDraw:751 <bottom of call stack>
        2026-10-07 12:30:37.113 11133-11294 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  Setting up sync and frameCommitCallback
2026-10-07 12:30:37.116 11133-11220 GrallocExtra            com.example.aquascope                I  gralloc_extra_query:is_SW3D 0
        2026-10-07 12:30:37.133 11133-11220 BLASTBufferQueue        com.example.aquascope                I  [VRI[MainActivity]@17e0af0#0](f:0,a:0,s:0) onFrameAvailable the first frame is available
2026-10-07 12:30:37.134 11133-11220 GrallocExtra            com.example.aquascope                I  gralloc_extra_query:is_SW3D 0
        2026-10-07 12:30:37.134 11133-11220 BLASTBufferQueue        com.example.aquascope                I  [VRI[MainActivity]@17e0af0#0](f:0,a:1,s:0) acquireNextBufferLocked size=1080x2265 mFrameNumber=1 applyTransaction=true mTimestamp=222311164521634(auto) mPendingTransactions.size=0 graphicBufferId=47815870906373 transform=0
        2026-10-07 12:30:37.134 11133-11220 ample.aquascope         com.example.aquascope                D  Can't load libmbrainSDK
        2026-10-07 12:30:37.134 11133-11220 ample.aquascope         com.example.aquascope                D  initMbrain failed
2026-10-07 12:30:37.135 11133-11220 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  Received frameCommittedCallback lastAttemptedDrawFrameNum=1 didProduceBuffer=true
        2026-10-07 12:30:37.136 11133-11220 HWUI                    com.example.aquascope                D  CFMS:: SetUp Pid : 11133    Tid : 11220
        2026-10-07 12:30:37.136 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                D  reportDrawFinished seqId=0
2026-10-07 12:30:37.146 11133-11133 log                     com.example.aquascope                D  onpause
2026-10-07 12:30:37.147 11133-11153 HWUI                    com.example.aquascope                I  Davey! duration=5216ms; Flags=1, FrameTimelineVsyncId=4781794, IntendedVsync=222305950429037, Vsync=222310897837811, InputEventId=0, HandleInputStart=222310899034942, AnimationStart=222310899041403, PerformTraversalsStart=222310899249326, DrawStart=222311093329634, FrameDeadline=222305958762370, FrameStartTime=222310898623788, FrameInterval=8328971, WorkloadTarget=8333333, SyncQueued=222311116530942, SyncStart=222311116618557, IssueDrawCommandsStart=222311144150557, SwapBuffers=222311164514249, FrameCompleted=222311166649711, DequeueBufferDuration=21153, QueueBufferDuration=1396923, GpuCompleted=222311165659249, SwapBuffersCompleted=222311166649711, DisplayPresentTime=0, CommandSubmissionCompleted=222311164514249,
        2026-10-07 12:30:37.149 11133-11133 SensorManager           com.example.aquascope                D  unregisterListener
2026-10-07 12:30:37.150 11133-11133 log                     com.example.aquascope                D  onstop
2026-10-07 12:30:37.150 11133-11133 thread                  com.example.aquascope                E  halting......
        2026-10-07 12:30:37.150 11133-11221 System.err              com.example.aquascope                W  java.lang.InterruptedException
2026-10-07 12:30:37.150 11133-11221 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Native Method)
2026-10-07 12:30:37.150 11133-11221 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep0(Thread.java:689)
2026-10-07 12:30:37.150 11133-11221 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Thread.java:667)
2026-10-07 12:30:37.150 11133-11221 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Thread.java:580)
2026-10-07 12:30:37.150 11133-11221 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.OfflineRecorder.get_FIFO(OfflineRecorder.java:134)
2026-10-07 12:30:37.150 11133-11221 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.Utils.waitForData(Utils.java:2158)
2026-10-07 12:30:37.150 11133-11221 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.work(SendChirpAsyncTask.java:672)
2026-10-07 12:30:37.150 11133-11221 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.doInBackground(SendChirpAsyncTask.java:163)
2026-10-07 12:30:37.151 11133-11221 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.doInBackground(SendChirpAsyncTask.java:34)
2026-10-07 12:30:37.151 11133-11221 System.err              com.example.aquascope                W  	at android.os.AsyncTask$3.call(AsyncTask.java:396)
2026-10-07 12:30:37.151 11133-11221 System.err              com.example.aquascope                W  	at java.util.concurrent.FutureTask.run(FutureTask.java:328)
2026-10-07 12:30:37.152 11133-11221 System.err              com.example.aquascope                W  	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1100)
2026-10-07 12:30:37.152 11133-11221 System.err              com.example.aquascope                W  	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
2026-10-07 12:30:37.152 11133-11221 System.err              com.example.aquascope                W  	at java.lang.Thread.run(Thread.java:1572)
2026-10-07 12:30:37.155 11133-11133 log                     com.example.aquascope                D  onresume
2026-10-07 12:30:37.158 11133-11133 SensorManager           com.example.aquascope                D  registerListener :: 1936484718, Samsung Linear Acceleration Sensor, 20000, 0,
        2026-10-07 12:30:37.158 11133-11133 SensorManager           com.example.aquascope                D  registerListener :: 4, LSM6DSOTR Gyroscope, 20000, 0, com.example.root.ffttest2.MainActivity@cc693d4
2026-10-07 12:30:37.158 11133-11133 log                     com.example.aquascope                E  start startmethod from onResume
2026-10-07 12:30:37.158 11133-11133 log                     com.example.aquascope                E  Bob is the receiver; starting the acoustic listen loop
2026-10-07 12:30:37.159 11133-11301 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/ Bob-AdaptParams-0.txt false
        2026-10-07 12:30:37.161 11133-11301 log                     com.example.aquascope                E  appendtofile Bob-Feedback-log.txt false
        2026-10-07 12:30:37.162 11133-11301 log                     com.example.aquascope                D  work 0
        2026-10-07 12:30:37.162 11133-11301 log                     com.example.aquascope                D  Bob-DataRx-0-0
        2026-10-07 12:30:37.163 11133-11301 recorder                com.example.aquascope                E  minbuffersize 7680
        2026-10-07 12:30:37.173 11133-11312 recorder                com.example.aquascope                E  start 1
        2026-10-07 12:30:37.175 11133-11133 BufferQueueConsumer     com.example.aquascope                D  [](id:2b7d00000001,api:0,p:-1,c:11133) connect: controlledByApp=true
        2026-10-07 12:30:37.176 11133-11133 DeferrableSurface       com.example.aquascope                D  Surface created[total_surfaces=1, used_surfaces=0](androidx.camera.core.impl.ImmediateSurface@1e3a8d4}
        2026-10-07 12:30:37.178 11133-11133 log                     com.example.aquascope                D  camera bound with 1 use case(s)
        2026-10-07 12:30:37.178 11133-11220 HWUI                    com.example.aquascope                D  HWUI - treat SMPTE_170M as sRGB
2026-10-07 12:30:37.179 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Use case androidx.camera.core.ImageCapture-084a7ef5-2098-4b16-a00e-b9f8ce95eb54188434373 ACTIVE
2026-10-07 12:30:37.179 11133-11289 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [] for camera: 0
        2026-10-07 12:30:37.180 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Use case androidx.camera.core.ImageCapture-084a7ef5-2098-4b16-a00e-b9f8ce95eb54188434373 ACTIVE
2026-10-07 12:30:37.180 11133-11289 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [] for camera: 0
        2026-10-07 12:30:37.181 11133-11289 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [] for camera: 0
        2026-10-07 12:30:37.181 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Use cases [androidx.camera.core.ImageCapture-084a7ef5-2098-4b16-a00e-b9f8ce95eb54188434373] now ATTACHED
2026-10-07 12:30:37.182 11133-11289 UseCaseAttachState      com.example.aquascope                D  All use case: [androidx.camera.core.ImageCapture-084a7ef5-2098-4b16-a00e-b9f8ce95eb54188434373] for camera: 0
        2026-10-07 12:30:37.182 11133-11289 BufferQueueConsumer     com.example.aquascope                D  [](id:2b7d00000002,api:0,p:-1,c:11133) connect: controlledByApp=true
        2026-10-07 12:30:37.182 11133-11289 libMEOW                 com.example.aquascope                D  meow new tls: 0xb40000780a23f1a0
        2026-10-07 12:30:37.183 11133-11289 libMEOW                 com.example.aquascope                D  meow reload base cfg path: na
2026-10-07 12:30:37.183 11133-11289 libMEOW                 com.example.aquascope                D  meow reload overlay cfg path: na
2026-10-07 12:30:37.183 11133-11289 QT                      com.example.aquascope                W  qt_process_init() called
2026-10-07 12:30:37.183 11133-11289 QT                      com.example.aquascope                E  [QT]file does not exist
2026-10-07 12:30:37.183 11133-11289 QT                      com.example.aquascope                W  Support!!
        2026-10-07 12:30:37.183 11133-11289 QT                      com.example.aquascope                E  [QT]file does not exist
2026-10-07 12:30:37.183 11133-11301 log                     com.example.aquascope                E  sig length 960000,DataRx
2026-10-07 12:30:37.184 11133-11289 libMEOW                 com.example.aquascope                D  applied 1 plugins for [com.example.aquascope]:
        2026-10-07 12:30:37.184 11133-11289 libMEOW                 com.example.aquascope                D    plugin 1: [libMEOW_gift.so]:
        2026-10-07 12:30:37.184 11133-11133 Choreographer           com.example.aquascope                I  Skipped 37 frames!  The application may be doing too much work on its main thread.
        2026-10-07 12:30:37.184 11133-11289 MeteringRepeating       com.example.aquascope                D  MeteringSession SurfaceTexture size: 640x480
2026-10-07 12:30:37.185 11133-11289 DeferrableSurface       com.example.aquascope                D  Surface created[total_surfaces=2, used_surfaces=0](androidx.camera.core.impl.ImmediateSurface@9d6ac35}
        2026-10-07 12:30:37.186 11133-11289 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [androidx.camera.core.ImageCapture-084a7ef5-2098-4b16-a00e-b9f8ce95eb54188434373, MeteringRepeating147068339] for camera: 0
        2026-10-07 12:30:37.187 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Resetting Capture Session
2026-10-07 12:30:37.187 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Releasing session in state INITIALIZED
2026-10-07 12:30:37.188 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Attempting to force open the camera.
        2026-10-07 12:30:37.188 11133-11133 Choreographer           com.example.aquascope                I  Skipped 38 frames!  The application may be doing too much work on its main thread.
        2026-10-07 12:30:37.191 11133-11289 CameraStateRegistry     com.example.aquascope                D  tryOpenCamera(Camera@74ef98a[id=0]) [Available Cameras: 1, Already Open: false (Previous state: null)] --> SUCCESS
2026-10-07 12:30:37.193 11133-11133 InsetsController        com.example.aquascope                I  onStateChanged: host=com.example.aquascope/com.example.root.ffttest2.MainActivity, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {3 mType=ime mFrame=[0,0][0,0] mAttachedInsets=null mVisible=false mFlags=INVALID mSideHint=NONE mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:30:37.201 11133-11289 CameraStateRegistry     com.example.aquascope                D  Recalculating open cameras:
Camera                                       State
                                                                                                    -------------------------------------------------------------------
Camera@f74dcd7[id=1]                         UNKNOWN
Camera@74ef98a[id=0]                         OPENING
Camera@ad6ae30[id=2]                         UNKNOWN
                                                                                                    -------------------------------------------------------------------
Open count: 1 (Max allowed: 1)
        2026-10-07 12:30:37.202 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Opening camera.
        2026-10-07 12:30:37.202 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Transitioning camera internal state: INITIALIZED --> OPENING
2026-10-07 12:30:37.202 11133-11289 CameraStateMachine      com.example.aquascope                D  New public camera state CameraState{type=OPENING, error=null} from OPENING and null
        2026-10-07 12:30:37.202 11133-11289 CameraStateMachine      com.example.aquascope                D  Publishing new public camera state CameraState{type=OPENING, error=null}
2026-10-07 12:30:37.204 11133-11289 UseCaseAttachState      com.example.aquascope                D  All use case: [androidx.camera.core.ImageCapture-084a7ef5-2098-4b16-a00e-b9f8ce95eb54188434373, MeteringRepeating147068339] for camera: 0
        2026-10-07 12:30:37.211 11133-11133 InsetsController        com.example.aquascope                I  onStateChanged: host=com.example.aquascope/com.example.root.ffttest2.MainActivity, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=false mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {3 mType=ime mFrame=[0,0][0,0] mAttachedInsets=null mVisible=false mFlags=INVALID mSideHint=NONE mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=false mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:30:37.212 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[0,75][1080,2340] display=[0,75][1080,2340] parentFrame=[0,0][0,0]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:30:37.212 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  handleResized mSyncSeqId = 0
2026-10-07 12:30:37.212 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                D  reportNextDraw android.view.ViewRootImpl.handleResized:2995 android.view.ViewRootImpl.-$$Nest$mhandleResized:0 android.view.ViewRootImpl$W.resized:14200 android.app.servertransaction.WindowStateResizeItem.execute:81 android.app.servertransaction.WindowStateTransactionItem.execute:62
        2026-10-07 12:30:37.215 11133-11133 log                     com.example.aquascope                D  onstop
2026-10-07 12:30:37.215 11133-11133 thread                  com.example.aquascope                E  halting......
        2026-10-07 12:30:37.215 11133-11301 System.err              com.example.aquascope                W  java.lang.InterruptedException
2026-10-07 12:30:37.215 11133-11301 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Native Method)
2026-10-07 12:30:37.215 11133-11301 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep0(Thread.java:689)
2026-10-07 12:30:37.216 11133-11301 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Thread.java:667)
2026-10-07 12:30:37.216 11133-11301 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Thread.java:580)
2026-10-07 12:30:37.216 11133-11301 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.OfflineRecorder.get_FIFO(OfflineRecorder.java:134)
2026-10-07 12:30:37.216 11133-11301 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.Utils.waitForData(Utils.java:2158)
2026-10-07 12:30:37.216 11133-11301 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.work(SendChirpAsyncTask.java:672)
2026-10-07 12:30:37.216 11133-11301 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.doInBackground(SendChirpAsyncTask.java:163)
2026-10-07 12:30:37.217 11133-11301 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.doInBackground(SendChirpAsyncTask.java:34)
2026-10-07 12:30:37.217 11133-11301 System.err              com.example.aquascope                W  	at android.os.AsyncTask$3.call(AsyncTask.java:396)
2026-10-07 12:30:37.217 11133-11301 System.err              com.example.aquascope                W  	at java.util.concurrent.FutureTask.run(FutureTask.java:328)
2026-10-07 12:30:37.217 11133-11301 System.err              com.example.aquascope                W  	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1100)
2026-10-07 12:30:37.217 11133-11301 System.err              com.example.aquascope                W  	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
2026-10-07 12:30:37.217 11133-11301 System.err              com.example.aquascope                W  	at java.lang.Thread.run(Thread.java:1572)
2026-10-07 12:30:37.237 11133-11153 CameraManagerGlobal     com.example.aquascope                I  Camera 0 facing CAMERA_FACING_BACK state now CAMERA_STATE_OPENING for client com.example.aquascope API Level 2 User Id 0Device Id 0
        2026-10-07 12:30:37.239 11133-11300 thread                  com.example.aquascope                E  recording thread end elegantly......
        2026-10-07 12:30:37.242 11133-11153 CameraManagerGlobal     com.example.aquascope                I  postSingleUpdate device: camera id 0 status STATUS_NOT_AVAILABLE
2026-10-07 12:30:37.242 11133-11153 CameraManagerGlobal     com.example.aquascope                I  postSingleUpdate device: camera id 0 status STATUS_NOT_AVAILABLE
2026-10-07 12:30:37.242 11133-11153 CameraManagerGlobal     com.example.aquascope                I  postSingleUpdate device: camera id 0 status STATUS_NOT_AVAILABLE
2026-10-07 12:30:37.275 11133-11286 CameraManagerGlobal     com.example.aquascope                I  Camera 0 facing CAMERA_FACING_BACK state now CAMERA_STATE_OPEN for client com.example.aquascope API Level 2 User Id 0Device Id 0
        2026-10-07 12:30:37.279 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Use case androidx.camera.core.ImageCapture-084a7ef5-2098-4b16-a00e-b9f8ce95eb54188434373 ACTIVE
2026-10-07 12:30:37.280 11133-11289 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [androidx.camera.core.ImageCapture-084a7ef5-2098-4b16-a00e-b9f8ce95eb54188434373, MeteringRepeating147068339] for camera: 0
        2026-10-07 12:30:37.281 11133-11289 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [androidx.camera.core.ImageCapture-084a7ef5-2098-4b16-a00e-b9f8ce95eb54188434373, MeteringRepeating147068339] for camera: 0
        2026-10-07 12:30:37.282 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} CameraDevice.onOpened()
2026-10-07 12:30:37.282 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Transitioning camera internal state: OPENING --> OPENED
2026-10-07 12:30:37.282 11133-11289 CameraStateRegistry     com.example.aquascope                D  Recalculating open cameras:
Camera                                       State
                                                                                                    -------------------------------------------------------------------
Camera@f74dcd7[id=1]                         UNKNOWN
Camera@74ef98a[id=0]                         OPEN
Camera@ad6ae30[id=2]                         UNKNOWN
                                                                                                    -------------------------------------------------------------------
Open count: 1 (Max allowed: 1)
        2026-10-07 12:30:37.282 11133-11289 CameraStateMachine      com.example.aquascope                D  New public camera state CameraState{type=OPEN, error=null} from OPEN and null
        2026-10-07 12:30:37.282 11133-11289 CameraStateMachine      com.example.aquascope                D  Publishing new public camera state CameraState{type=OPEN, error=null}
2026-10-07 12:30:37.282 11133-11289 UseCaseAttachState      com.example.aquascope                D  All use case: [androidx.camera.core.ImageCapture-084a7ef5-2098-4b16-a00e-b9f8ce95eb54188434373, MeteringRepeating147068339] for camera: 0
        2026-10-07 12:30:37.284 11133-11289 SyncCaptureSessionBase  com.example.aquascope                D  [androidx.camera.camera2.internal.SynchronizedCaptureSessionBaseImpl@41ca0] getSurface...done
2026-10-07 12:30:37.284 11133-11289 CaptureSession          com.example.aquascope                D  Opening capture session.
2026-10-07 12:30:37.289 11133-11289 DeferrableSurface       com.example.aquascope                D  New surface in use[total_surfaces=2, used_surfaces=1](androidx.camera.core.impl.ImmediateSurface@1e3a8d4}
        2026-10-07 12:30:37.289 11133-11289 DeferrableSurface       com.example.aquascope                D  use count+1, useCount=1 androidx.camera.core.impl.ImmediateSurface@1e3a8d4
2026-10-07 12:30:37.289 11133-11289 DeferrableSurface       com.example.aquascope                D  New surface in use[total_surfaces=2, used_surfaces=2](androidx.camera.core.impl.ImmediateSurface@9d6ac35}
        2026-10-07 12:30:37.289 11133-11289 DeferrableSurface       com.example.aquascope                D  use count+1, useCount=1 androidx.camera.core.impl.ImmediateSurface@9d6ac35
2026-10-07 12:30:37.345 11133-11289 CaptureSession          com.example.aquascope                D  Attempting to send capture request onConfigured
2026-10-07 12:30:37.345 11133-11289 CaptureSession          com.example.aquascope                D  Issuing request for session.
2026-10-07 12:30:37.352 11133-11289 CaptureSession          com.example.aquascope                D  CameraCaptureSession.onConfigured() mState=OPENED
2026-10-07 12:30:37.353 11133-11289 CaptureSession          com.example.aquascope                D  CameraCaptureSession.onReady() OPENED
2026-10-07 12:30:37.358 11133-11153 CameraManagerGlobal     com.example.aquascope                I  Camera 0 facing CAMERA_FACING_BACK state now CAMERA_STATE_ACTIVE for client com.example.aquascope API Level 2 User Id 0Device Id 0
        2026-10-07 12:30:37.399 11133-11312 thread                  com.example.aquascope                E  recording thread end elegantly......
        2026-10-07 12:30:37.515 11133-11133 log                     com.example.aquascope                E  Bob is the receiver; starting the acoustic listen loop
2026-10-07 12:30:37.517 11133-11299 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/ Bob-AdaptParams-0.txt false
        2026-10-07 12:30:37.519 11133-11299 log                     com.example.aquascope                E  appendtofile Bob-Feedback-log.txt false
        2026-10-07 12:30:37.519 11133-11299 log                     com.example.aquascope                D  work 0
        2026-10-07 12:30:37.519 11133-11299 log                     com.example.aquascope                D  Bob-DataRx-0-0
        2026-10-07 12:30:37.519 11133-11299 recorder                com.example.aquascope                E  minbuffersize 7680
        2026-10-07 12:30:37.548 11133-11404 recorder                com.example.aquascope                E  start 1
        2026-10-07 12:30:37.552 11133-11299 log                     com.example.aquascope                E  sig length 960000,DataRx
2026-10-07 12:30:37.587 11133-11313 InteractionJankMonitor  com.example.aquascope                W  Initializing without READ_DEVICE_CONFIG permission. enabled=false, interval=1, missedFrameThreshold=3, frameTimeThreshold=64, package=com.example.aquascope
2026-10-07 12:30:37.610 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 2212KB AllocSpace bytes, 5(148KB) LOS objects, 62% free, 28MB/76MB, paused 395us,6.285ms total 58ms
2026-10-07 12:30:37.819 11133-11133 log                     com.example.aquascope                D  onstop
2026-10-07 12:30:37.819 11133-11299 System.err              com.example.aquascope                W  java.lang.InterruptedException
2026-10-07 12:30:37.820 11133-11133 thread                  com.example.aquascope                E  halting......
        2026-10-07 12:30:37.820 11133-11299 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Native Method)
2026-10-07 12:30:37.820 11133-11299 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep0(Thread.java:689)
2026-10-07 12:30:37.821 11133-11299 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Thread.java:667)
2026-10-07 12:30:37.821 11133-11299 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Thread.java:580)
2026-10-07 12:30:37.821 11133-11299 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.OfflineRecorder.get_FIFO(OfflineRecorder.java:134)
2026-10-07 12:30:37.821 11133-11299 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.Utils.waitForData(Utils.java:2158)
2026-10-07 12:30:37.822 11133-11299 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.work(SendChirpAsyncTask.java:672)
2026-10-07 12:30:37.822 11133-11299 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.doInBackground(SendChirpAsyncTask.java:163)
2026-10-07 12:30:37.822 11133-11299 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.doInBackground(SendChirpAsyncTask.java:34)
2026-10-07 12:30:37.822 11133-11299 System.err              com.example.aquascope                W  	at android.os.AsyncTask$3.call(AsyncTask.java:396)
2026-10-07 12:30:37.822 11133-11299 System.err              com.example.aquascope                W  	at java.util.concurrent.FutureTask.run(FutureTask.java:328)
2026-10-07 12:30:37.822 11133-11299 System.err              com.example.aquascope                W  	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1100)
2026-10-07 12:30:37.822 11133-11299 System.err              com.example.aquascope                W  	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
2026-10-07 12:30:37.822 11133-11299 System.err              com.example.aquascope                W  	at java.lang.Thread.run(Thread.java:1572)
2026-10-07 12:30:37.969 11133-11404 thread                  com.example.aquascope                E  recording thread end elegantly......
        2026-10-07 12:30:38.120 11133-11133 log                     com.example.aquascope                E  Bob is the receiver; starting the acoustic listen loop
2026-10-07 12:30:38.122 11133-11414 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/ Bob-AdaptParams-0.txt false
        2026-10-07 12:30:38.123 11133-11414 log                     com.example.aquascope                E  appendtofile Bob-Feedback-log.txt false
        2026-10-07 12:30:38.124 11133-11414 log                     com.example.aquascope                D  work 0
        2026-10-07 12:30:38.124 11133-11414 log                     com.example.aquascope                D  Bob-DataRx-0-0
        2026-10-07 12:30:38.124 11133-11414 recorder                com.example.aquascope                E  minbuffersize 7680
        2026-10-07 12:30:38.150 11133-11420 recorder                com.example.aquascope                E  start 1
        2026-10-07 12:30:38.157 11133-11414 log                     com.example.aquascope                E  sig length 960000,DataRx
2026-10-07 12:30:38.441 11133-11414 System.err              com.example.aquascope                W  java.lang.InterruptedException
2026-10-07 12:30:38.441 11133-11133 log                     com.example.aquascope                D  onstop
2026-10-07 12:30:38.441 11133-11133 thread                  com.example.aquascope                E  halting......
        2026-10-07 12:30:38.441 11133-11414 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Native Method)
2026-10-07 12:30:38.441 11133-11414 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep0(Thread.java:689)
2026-10-07 12:30:38.441 11133-11414 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Thread.java:667)
2026-10-07 12:30:38.441 11133-11414 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Thread.java:580)
2026-10-07 12:30:38.441 11133-11414 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.OfflineRecorder.get_FIFO(OfflineRecorder.java:134)
2026-10-07 12:30:38.441 11133-11414 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.Utils.waitForData(Utils.java:2158)
2026-10-07 12:30:38.441 11133-11414 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.work(SendChirpAsyncTask.java:672)
2026-10-07 12:30:38.441 11133-11414 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.doInBackground(SendChirpAsyncTask.java:163)
2026-10-07 12:30:38.441 11133-11414 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.doInBackground(SendChirpAsyncTask.java:34)
2026-10-07 12:30:38.441 11133-11414 System.err              com.example.aquascope                W  	at android.os.AsyncTask$3.call(AsyncTask.java:396)
2026-10-07 12:30:38.441 11133-11414 System.err              com.example.aquascope                W  	at java.util.concurrent.FutureTask.run(FutureTask.java:328)
2026-10-07 12:30:38.441 11133-11414 System.err              com.example.aquascope                W  	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1100)
2026-10-07 12:30:38.441 11133-11414 System.err              com.example.aquascope                W  	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
2026-10-07 12:30:38.442 11133-11414 System.err              com.example.aquascope                W  	at java.lang.Thread.run(Thread.java:1572)
2026-10-07 12:30:38.610 11133-11420 thread                  com.example.aquascope                E  recording thread end elegantly......
        2026-10-07 12:30:38.742 11133-11133 log                     com.example.aquascope                E  Bob is the receiver; starting the acoustic listen loop
2026-10-07 12:30:38.744 11133-11433 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/ Bob-AdaptParams-0.txt false
        2026-10-07 12:30:38.745 11133-11433 log                     com.example.aquascope                E  appendtofile Bob-Feedback-log.txt false
        2026-10-07 12:30:38.747 11133-11433 log                     com.example.aquascope                D  work 0
        2026-10-07 12:30:38.747 11133-11433 log                     com.example.aquascope                D  Bob-DataRx-0-0
        2026-10-07 12:30:38.747 11133-11433 recorder                com.example.aquascope                E  minbuffersize 7680
        2026-10-07 12:30:38.769 11133-11437 recorder                com.example.aquascope                E  start 1
        2026-10-07 12:30:38.772 11133-11433 log                     com.example.aquascope                E  sig length 960000,DataRx
2026-10-07 12:30:39.044 11133-11133 log                     com.example.aquascope                D  Current Expmode: testExp
2026-10-07 12:30:39.049 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                D  mThreadedRenderer.initializeIfNeeded()#2 mSurface={isValid=true 0xb40000787a100ef0}
        2026-10-07 12:30:39.054 11133-11133 InputMethodManagerUtils com.example.aquascope                D  startInputInner - Id : 0
        2026-10-07 12:30:39.054 11133-11133 InputMethodManager      com.example.aquascope                I  startInputInner - IInputMethodManagerGlobalInvoker.startInputOrWindowGainedFocus
2026-10-07 12:30:39.058 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:30:39.062 11133-11153 InputTransport          com.example.aquascope                D  Input channel constructed: 'ClientS', fd=189
        2026-10-07 12:30:39.062 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=high hint, reason=touch, vri=VRI[MainActivity]@17e0af0
2026-10-07 12:30:39.066 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:30:39.069 11133-11133 log                     com.example.aquascope                D  Codebook Size is set to 4096
        2026-10-07 12:30:39.071 11133-11133 ImageSegmentation       com.example.aquascope                D  lite_optimized_seg_240p.ptl
2026-10-07 12:30:39.075 11133-11133 DeferrableSurface       com.example.aquascope                D  surface closed,  useCount=1 closed=true androidx.camera.core.impl.ImmediateSurface@1e3a8d4
2026-10-07 12:30:39.076 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Use cases [androidx.camera.core.ImageCapture-084a7ef5-2098-4b16-a00e-b9f8ce95eb54188434373] now DETACHED for camera
2026-10-07 12:30:39.076 11133-11289 UseCaseAttachState      com.example.aquascope                D  All use case: [MeteringRepeating147068339] for camera: 0
        2026-10-07 12:30:39.077 11133-11289 MeteringRepeating       com.example.aquascope                D  MeteringRepeating clear!
        2026-10-07 12:30:39.077 11133-11289 DeferrableSurface       com.example.aquascope                D  surface closed,  useCount=1 closed=true androidx.camera.core.impl.ImmediateSurface@9d6ac35
2026-10-07 12:30:39.077 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Resetting Capture Session
2026-10-07 12:30:39.078 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Releasing session in state OPENED
2026-10-07 12:30:39.079 11133-11289 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [] for camera: 0
        2026-10-07 12:30:39.079 11133-11289 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [] for camera: 0
        2026-10-07 12:30:39.079 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Closing camera.
        2026-10-07 12:30:39.079 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Transitioning camera internal state: OPENED --> CLOSING
2026-10-07 12:30:39.080 11133-11289 CameraStateRegistry     com.example.aquascope                D  Recalculating open cameras:
Camera                                       State
                                                                                                    -------------------------------------------------------------------
Camera@f74dcd7[id=1]                         UNKNOWN
Camera@74ef98a[id=0]                         CLOSING
Camera@ad6ae30[id=2]                         UNKNOWN
                                                                                                    -------------------------------------------------------------------
Open count: 1 (Max allowed: 1)
        2026-10-07 12:30:39.080 11133-11289 CameraStateMachine      com.example.aquascope                D  New public camera state CameraState{type=CLOSING, error=null} from CLOSING and null
        2026-10-07 12:30:39.080 11133-11289 CameraStateMachine      com.example.aquascope                D  Publishing new public camera state CameraState{type=CLOSING, error=null}
2026-10-07 12:30:39.080 11133-11133 BufferQueueConsumer     com.example.aquascope                D  [](id:2b7d00000003,api:0,p:-1,c:11133) connect: controlledByApp=true
        2026-10-07 12:30:39.080 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Resetting Capture Session
2026-10-07 12:30:39.080 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Releasing session in state CLOSING
2026-10-07 12:30:39.080 11133-11133 DeferrableSurface       com.example.aquascope                D  Surface created[total_surfaces=3, used_surfaces=2](androidx.camera.core.impl.ImmediateSurface@f7a9c3d}
        2026-10-07 12:30:39.080 11133-11289 CaptureSession          com.example.aquascope                D  onSessionFinished()
2026-10-07 12:30:39.081 11133-11133 log                     com.example.aquascope                D  camera bound with 1 use case(s)
        2026-10-07 12:30:39.082 11133-11133 Choreographer           com.example.aquascope                I  Skipped 226 frames!  The application may be doing too much work on its main thread.
        2026-10-07 12:30:39.083 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                D  Setup new sync=wmsSync-VRI[MainActivity]@17e0af0#2
        2026-10-07 12:30:39.083 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  Creating new active sync group VRI[MainActivity]@17e0af0#3
        2026-10-07 12:30:39.083 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                D  registerCallbacksForSync syncBuffer=false
2026-10-07 12:30:39.086 11133-11293 VRI[MainAc...y]@17e0af0 com.example.aquascope                D  Received frameDrawingCallback syncResult=0 frameNum=2.
        2026-10-07 12:30:39.086 11133-11293 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  Setting up sync and frameCommitCallback
2026-10-07 12:30:39.094 11133-11220 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  Received frameCommittedCallback lastAttemptedDrawFrameNum=2 didProduceBuffer=true
        2026-10-07 12:30:39.094 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                D  reportDrawFinished seqId=0
2026-10-07 12:30:39.097 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[0,75][1080,2340] display=[0,75][1080,2340] parentFrame=[0,0][0,0]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:30:39.097 11133-11133 InsetsController        com.example.aquascope                D  Ignore notifyFinished, because the animation has already been cancelled.
        2026-10-07 12:30:39.098 11133-11133 InsetsController        com.example.aquascope                D  hide(ime())
        2026-10-07 12:30:39.098 11133-11133 ImeTracker              com.example.aquascope                I  com.example.aquascope:e56fe4a2: onCancelled at PHASE_CLIENT_ALREADY_HIDDEN
2026-10-07 12:30:39.106 11133-11146 HWUI                    com.example.aquascope                I  Davey! duration=1900ms; Flags=0, FrameTimelineVsyncId=4782313, IntendedVsync=222311224860982, Vsync=222313107912644, InputEventId=53551153, HandleInputStart=222313114547865, AnimationStart=222313114549096, PerformTraversalsStart=222313114768096, DrawStart=222313115213942, FrameDeadline=222311233194315, FrameStartTime=222313114203557, FrameInterval=8332087, WorkloadTarget=8333333, SyncQueued=222313117170865, SyncStart=222313117256480, IssueDrawCommandsStart=222313117860788, SwapBuffers=222313124523173, FrameCompleted=222313125587173, DequeueBufferDuration=22154, QueueBufferDuration=729770, GpuCompleted=222313125587173, SwapBuffersCompleted=222313125560173, DisplayPresentTime=0, CommandSubmissionCompleted=222313124523173,
        2026-10-07 12:30:39.123 11133-11145 CameraManagerGlobal     com.example.aquascope                I  Camera 0 facing CAMERA_FACING_BACK state now CAMERA_STATE_IDLE for client com.example.aquascope API Level 2 User Id 0Device Id 0
        2026-10-07 12:30:39.130 11133-11133 ample.aquascope         com.example.aquascope                I  hiddenapi: Accessing hidden field Landroid/widget/AbsListView;->mIsChildViewEnabled:Z (runtime_flags=0, domain=platform, api=unsupported) from Landroidx/appcompat/widget/DropDownListView; (domain=app, TargetSdkVersion=31) using reflection: allowed
2026-10-07 12:30:39.135 11133-11133 WindowManager           com.example.aquascope                I  WindowManagerGlobal#addView, ty=1002, view=android.widget.PopupWindow$PopupDecorView{a2f5963 V.E...... R.....I. 0,0-0,0}, caller=android.view.WindowManagerImpl.addView:167 android.widget.PopupWindow.invokePopup:1686 android.widget.PopupWindow.showAsDropDown:1493
        2026-10-07 12:30:39.135 11133-11133 ViewRootImpl            com.example.aquascope                I  dVRR is disabled
2026-10-07 12:30:39.136 11133-11220 NativeCust...ncyManager com.example.aquascope                D  [NativeCFMS] BpCustomFrequencyManager::BpCustomFrequencyManager()
2026-10-07 12:30:39.143 11133-11133 InputTransport          com.example.aquascope                D  Input channel constructed: 'a9bfbcd', fd=195
        2026-10-07 12:30:39.144 11133-11133 InsetsController        com.example.aquascope                I  onStateChanged: host=PopupWindow:abecb92, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=false mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=false mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:30:39.145 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                I  synced displayState. AttachInfo displayState=2
2026-10-07 12:30:39.145 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                I  setView = android.widget.PopupWindow$PopupDecorView@a2f5963 IsHRR=false mFlingFrameRateChange=0 TM=true
        2026-10-07 12:30:39.146 11133-11133 WindowOnBackDispatcher  com.example.aquascope                D  setTopOnBackInvokedCallback (unwrapped): android.view.ViewRootImpl$$ExternalSyntheticLambda16@38c91db
2026-10-07 12:30:39.151 11133-11133 BufferQueueConsumer     com.example.aquascope                D  [](id:2b7d00000004,api:0,p:-1,c:11133) connect: controlledByApp=false
        2026-10-07 12:30:39.151 11133-11133 BLASTBufferQueue        com.example.aquascope                I  [VRI[PopupWindow:abecb92]@8f76960#1](f:0,a:0,s:0) constructor()
2026-10-07 12:30:39.152 11133-11133 BLASTBufferQueue_Java   com.example.aquascope                I  new BLASTBufferQueue, mName= VRI[PopupWindow:abecb92]@8f76960 mNativeObject= 0xb4000078aaf27d90 caller= android.view.ViewRootImpl.updateBlastSurfaceIfNeeded:3603 android.view.ViewRootImpl.relayoutWindow:11813 android.view.ViewRootImpl.performTraversals:4840 android.view.ViewRootImpl.doTraversal:3959 android.view.ViewRootImpl$TraversalRunnable.run:13034 android.view.Choreographer$CallbackRecord.run:1961 android.view.Choreographer$CallbackRecord.run:1970 android.view.Choreographer.doCallbacks:1423 android.view.Choreographer.doFrame:1348 android.view.Choreographer$FrameDisplayEventReceiver.run:1930
        2026-10-07 12:30:39.152 11133-11133 BLASTBufferQueue_Java   com.example.aquascope                I  update, w= 714 h= 810 mName = VRI[PopupWindow:abecb92]@8f76960 mNativeObject= 0xb4000078aaf27d90 sc.mNativeObject= 0xb4000077ea0c65d0 format= -3 caller= android.view.ViewRootImpl.updateBlastSurfaceIfNeeded:3608 android.view.ViewRootImpl.relayoutWindow:11813 android.view.ViewRootImpl.performTraversals:4840 android.view.ViewRootImpl.doTraversal:3959 android.view.ViewRootImpl$TraversalRunnable.run:13034 android.view.Choreographer$CallbackRecord.run:1961
        2026-10-07 12:30:39.153 11133-11133 BLASTBufferQueue        com.example.aquascope                I  [VRI[PopupWindow:abecb92]@8f76960#1](f:0,a:0,s:0) update width=714 height=810 format=-3 mTransformHint=0
        2026-10-07 12:30:39.154 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                I  Relayout returned: old=(501,1431,945,1971) new=(501,1431,945,1971) relayoutAsync=false req=(444,540)0 dur=4 res=0x3 s={true 0xb40000787a12ac70} ch=true seqId=0
        2026-10-07 12:30:39.154 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                I  performConfigurationChange setNightDimText nightDimLevel=0
        2026-10-07 12:30:39.156 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                D  mThreadedRenderer.initialize() mSurface={isValid=true 0xb40000787a12ac70} hwInitialized=true
        2026-10-07 12:30:39.172 11133-11133 AbsListView             com.example.aquascope                D   in onLayout changed
2026-10-07 12:30:39.173 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                D  reportNextDraw android.view.ViewRootImpl.performTraversals:5507 android.view.ViewRootImpl.doTraversal:3959 android.view.ViewRootImpl$TraversalRunnable.run:13034 android.view.Choreographer$CallbackRecord.run:1961 android.view.Choreographer$CallbackRecord.run:1970
        2026-10-07 12:30:39.178 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                D  Setup new sync=wmsSync-VRI[PopupWindow:abecb92]@8f76960#4
        2026-10-07 12:30:39.179 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                I  Creating new active sync group VRI[PopupWindow:abecb92]@8f76960#5
        2026-10-07 12:30:39.182 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                D  registerCallbacksForSync syncBuffer=false
2026-10-07 12:30:39.184 11133-11294 VRI[PopupW...2]@8f76960 com.example.aquascope                D  Received frameDrawingCallback syncResult=0 frameNum=1.
        2026-10-07 12:30:39.185 11133-11294 VRI[PopupW...2]@8f76960 com.example.aquascope                I  mWNT: t=0xb4000076fa0f8850 mBlastBufferQueue=0xb4000078aaf27d90 fn= 1 HdrRenderState mRenderHdrSdrRatio=1.0 caller= android.view.ViewRootImpl$12.onFrameDraw:15561 android.view.ThreadedRenderer$1.onFrameDraw:751 <bottom of call stack>
        2026-10-07 12:30:39.185 11133-11294 VRI[PopupW...2]@8f76960 com.example.aquascope                I  Setting up sync and frameCommitCallback
2026-10-07 12:30:39.205 11133-11220 BLASTBufferQueue        com.example.aquascope                I  [VRI[PopupWindow:abecb92]@8f76960#1](f:0,a:0,s:0) onFrameAvailable the first frame is available
2026-10-07 12:30:39.205 11133-11220 BLASTBufferQueue        com.example.aquascope                I  [VRI[PopupWindow:abecb92]@8f76960#1](f:0,a:1,s:0) acquireNextBufferLocked size=714x810 mFrameNumber=1 applyTransaction=true mTimestamp=222313237007480(auto) mPendingTransactions.size=0 graphicBufferId=47815870906387 transform=0
        2026-10-07 12:30:39.206 11133-11220 VRI[PopupW...2]@8f76960 com.example.aquascope                I  Received frameCommittedCallback lastAttemptedDrawFrameNum=1 didProduceBuffer=true
        2026-10-07 12:30:39.208 11133-11220 HWUI                    com.example.aquascope                D  CFMS:: SetUp Pid : 11133    Tid : 11220
        2026-10-07 12:30:39.209 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                D  reportDrawFinished seqId=0
2026-10-07 12:30:39.212 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[0,75][1080,2340] display=[0,75][1080,2340] parentFrame=[0,0][0,0]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:30:39.213 11133-11220 HWUI                    com.example.aquascope                D  HWUI - treat SMPTE_170M as sRGB
2026-10-07 12:30:39.216 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[501,1431][945,1971] display=[-100000,-100000][100000,100000] parentFrame=[0,0][0,0] attachedFrame=[0,75][1080,2340]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:30:39.216 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                I  handleResized mSyncSeqId = 0
2026-10-07 12:30:39.217 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                D  reportNextDraw android.view.ViewRootImpl.handleResized:2995 android.view.ViewRootImpl.-$$Nest$mhandleResized:0 android.view.ViewRootImpl$W.resized:14200 android.app.servertransaction.WindowStateResizeItem.execute:81 android.app.servertransaction.WindowStateTransactionItem.execute:62
        2026-10-07 12:30:39.219 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                D  Setup new sync=wmsSync-VRI[PopupWindow:abecb92]@8f76960#6
        2026-10-07 12:30:39.219 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                I  Creating new active sync group VRI[PopupWindow:abecb92]@8f76960#7
        2026-10-07 12:30:39.222 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                D  registerCallbacksForSync syncBuffer=false
2026-10-07 12:30:39.223 11133-11293 VRI[PopupW...2]@8f76960 com.example.aquascope                D  Received frameDrawingCallback syncResult=0 frameNum=2.
        2026-10-07 12:30:39.223 11133-11293 VRI[PopupW...2]@8f76960 com.example.aquascope                I  Setting up sync and frameCommitCallback
2026-10-07 12:30:39.223 11133-11220 VRI[PopupW...2]@8f76960 com.example.aquascope                I  Received frameCommittedCallback lastAttemptedDrawFrameNum=2 didProduceBuffer=false
        2026-10-07 12:30:39.223 11133-11220 BLASTBufferQueue_Java   com.example.aquascope                I  gatherPendingTransactions, mName= VRI[PopupWindow:abecb92]@8f76960 mNativeObject= 0xb4000078aaf27d90 frameNumber= 2 caller= android.view.ViewRootImpl$12.lambda$onFrameDraw$3:15645 android.view.ViewRootImpl$12.$r8$lambda$CTo3ExVBk8akdVTGlqHPAoYLRVI:0 android.view.ViewRootImpl$12$$ExternalSyntheticLambda1.onFrameCommit:0 android.view.ThreadedRenderer$1.lambda$onFrameDraw$0:763 android.view.ThreadedRenderer$1$$ExternalSyntheticLambda0.onFrameCommit:0 <bottom of call stack>
        2026-10-07 12:30:39.225 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                D  reportDrawFinished seqId=0
2026-10-07 12:30:39.235 11133-11133 ImeFocusController      com.example.aquascope                I  onPreWindowFocus: skipped hasWindowFocus=false mHasImeFocus=true
        2026-10-07 12:30:39.239 11133-11133 ImeFocusController      com.example.aquascope                I  onPostWindowFocus: skipped hasWindowFocus=false mHasImeFocus=true
        2026-10-07 12:30:39.242 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                D  mThreadedRenderer.initializeIfNeeded()#2 mSurface={isValid=true 0xb40000787a12ac70}
        2026-10-07 12:30:39.242 11133-11133 ImeFocusController      com.example.aquascope                I  onPreWindowFocus: skipped hasWindowFocus=true mHasImeFocus=false
        2026-10-07 12:30:39.243 11133-11133 ImeFocusController      com.example.aquascope                I  onPostWindowFocus: skipped hasWindowFocus=true mHasImeFocus=false
        2026-10-07 12:30:39.314 11133-11146 CameraManagerGlobal     com.example.aquascope                I  postSingleUpdate device: camera id 0 status STATUS_PRESENT
2026-10-07 12:30:39.314 11133-11146 CameraManagerGlobal     com.example.aquascope                I  postSingleUpdate device: camera id 0 status STATUS_PRESENT
2026-10-07 12:30:39.314 11133-11146 CameraManagerGlobal     com.example.aquascope                I  postSingleUpdate device: camera id 0 status STATUS_PRESENT
2026-10-07 12:30:39.314 11133-11146 CameraManagerGlobal     com.example.aquascope                I  Camera 0 facing CAMERA_FACING_BACK state now CAMERA_STATE_CLOSED for client com.example.aquascope API Level 2 User Id 0Device Id 0
        2026-10-07 12:30:39.319 11133-11289 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [] for camera: 0
        2026-10-07 12:30:39.320 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Use case androidx.camera.core.ImageCapture-6100bd1d-40c0-40d7-b8d8-09e64841a773111114251 ACTIVE
2026-10-07 12:30:39.321 11133-11289 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [] for camera: 0
        2026-10-07 12:30:39.322 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Use case androidx.camera.core.ImageCapture-6100bd1d-40c0-40d7-b8d8-09e64841a773111114251 ACTIVE
2026-10-07 12:30:39.322 11133-11289 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [] for camera: 0
        2026-10-07 12:30:39.324 11133-11289 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [] for camera: 0
        2026-10-07 12:30:39.326 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Use cases [androidx.camera.core.ImageCapture-6100bd1d-40c0-40d7-b8d8-09e64841a773111114251] now ATTACHED
2026-10-07 12:30:39.327 11133-11289 UseCaseAttachState      com.example.aquascope                D  All use case: [androidx.camera.core.ImageCapture-6100bd1d-40c0-40d7-b8d8-09e64841a773111114251] for camera: 0
        2026-10-07 12:30:39.327 11133-11289 BufferQueueConsumer     com.example.aquascope                D  [](id:2b7d00000005,api:0,p:-1,c:11133) connect: controlledByApp=true
        2026-10-07 12:30:39.328 11133-11289 MeteringRepeating       com.example.aquascope                D  MeteringSession SurfaceTexture size: 640x480
2026-10-07 12:30:39.330 11133-11289 DeferrableSurface       com.example.aquascope                D  Surface created[total_surfaces=4, used_surfaces=2](androidx.camera.core.impl.ImmediateSurface@5ba543e}
        2026-10-07 12:30:39.332 11133-11289 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [androidx.camera.core.ImageCapture-6100bd1d-40c0-40d7-b8d8-09e64841a773111114251, MeteringRepeating199573429] for camera: 0
        2026-10-07 12:30:39.335 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Resetting Capture Session
2026-10-07 12:30:39.336 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Releasing session in state CLOSING
2026-10-07 12:30:39.336 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Transitioning camera internal state: CLOSING --> REOPENING
2026-10-07 12:30:39.336 11133-11289 CameraStateRegistry     com.example.aquascope                D  Recalculating open cameras:
Camera                                       State
                                                                                                    -------------------------------------------------------------------
Camera@f74dcd7[id=1]                         UNKNOWN
Camera@74ef98a[id=0]                         OPENING
Camera@ad6ae30[id=2]                         UNKNOWN
                                                                                                    -------------------------------------------------------------------
Open count: 1 (Max allowed: 1)
        2026-10-07 12:30:39.337 11133-11289 CameraStateMachine      com.example.aquascope                D  New public camera state CameraState{type=OPENING, error=null} from OPENING and null
        2026-10-07 12:30:39.337 11133-11289 CameraStateMachine      com.example.aquascope                D  Publishing new public camera state CameraState{type=OPENING, error=null}
2026-10-07 12:30:39.337 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Use case androidx.camera.core.ImageCapture-6100bd1d-40c0-40d7-b8d8-09e64841a773111114251 ACTIVE
2026-10-07 12:30:39.338 11133-11289 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [androidx.camera.core.ImageCapture-6100bd1d-40c0-40d7-b8d8-09e64841a773111114251, MeteringRepeating199573429] for camera: 0
        2026-10-07 12:30:39.341 11133-11289 DeferrableSurface       com.example.aquascope                D  use count-1,  useCount=0 closed=true androidx.camera.core.impl.ImmediateSurface@1e3a8d4
2026-10-07 12:30:39.341 11133-11289 DeferrableSurface       com.example.aquascope                D  Surface no longer in use[total_surfaces=4, used_surfaces=1](androidx.camera.core.impl.ImmediateSurface@1e3a8d4}
        2026-10-07 12:30:39.342 11133-11289 DeferrableSurface       com.example.aquascope                D  Surface terminated[total_surfaces=3, used_surfaces=1](androidx.camera.core.impl.ImmediateSurface@1e3a8d4}
        2026-10-07 12:30:39.342 11133-11289 DeferrableSurface       com.example.aquascope                D  use count-1,  useCount=0 closed=true androidx.camera.core.impl.ImmediateSurface@9d6ac35
2026-10-07 12:30:39.342 11133-11289 DeferrableSurface       com.example.aquascope                D  Surface no longer in use[total_surfaces=3, used_surfaces=0](androidx.camera.core.impl.ImmediateSurface@9d6ac35}
        2026-10-07 12:30:39.342 11133-11289 DeferrableSurface       com.example.aquascope                D  Surface terminated[total_surfaces=2, used_surfaces=0](androidx.camera.core.impl.ImmediateSurface@9d6ac35}
        2026-10-07 12:30:39.342 11133-11289 BufferQueueConsumer     com.example.aquascope                D  [SurfaceTexture-0-11133-0](id:2b7d00000002,api:0,p:-1,c:11133) disconnect
2026-10-07 12:30:39.343 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} CameraDevice.onClosed()
2026-10-07 12:30:39.343 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Attempting to open the camera.
        2026-10-07 12:30:39.344 11133-11289 CameraStateRegistry     com.example.aquascope                D  tryOpenCamera(Camera@74ef98a[id=0]) [Available Cameras: 0, Already Open: true (Previous state: OPENING)] --> SUCCESS
        2026-10-07 12:30:39.344 11133-11289 CameraStateRegistry     com.example.aquascope                D  Recalculating open cameras:
        Camera                                       State
        -------------------------------------------------------------------
        Camera@f74dcd7[id=1]                         UNKNOWN
        Camera@74ef98a[id=0]                         OPENING
        Camera@ad6ae30[id=2]                         UNKNOWN
        -------------------------------------------------------------------
        Open count: 1 (Max allowed: 1)
        2026-10-07 12:30:39.344 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Opening camera.
        2026-10-07 12:30:39.345 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Transitioning camera internal state: REOPENING --> OPENING
        2026-10-07 12:30:39.345 11133-11289 CameraStateMachine      com.example.aquascope                D  New public camera state CameraState{type=OPENING, error=null} from OPENING and null
        2026-10-07 12:30:39.345 11133-11289 CameraStateMachine      com.example.aquascope                D  Publishing new public camera state CameraState{type=OPENING, error=null}
        2026-10-07 12:30:39.346 11133-11289 UseCaseAttachState      com.example.aquascope                D  All use case: [androidx.camera.core.ImageCapture-6100bd1d-40c0-40d7-b8d8-09e64841a773111114251, MeteringRepeating199573429] for camera: 0
        2026-10-07 12:30:39.353 11133-11133 BLASTBufferQueue_Java   com.example.aquascope                I  update, w= 714 h= 810 mName = VRI[PopupWindow:abecb92]@8f76960 mNativeObject= 0xb4000078aaf27d90 sc.mNativeObject= 0xb4000077ea0c65d0 format= -3 caller= android.view.ViewRootImpl.updateBlastSurfaceIfNeeded:3592 android.view.ViewRootImpl.relayoutWindow:11813 android.view.ViewRootImpl.performTraversals:4840 android.view.ViewRootImpl.doTraversal:3959 android.view.ViewRootImpl$TraversalRunnable.run:13034 android.view.Choreographer$CallbackRecord.run:1961
        2026-10-07 12:30:39.353 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                I  Relayout returned: old=(501,1431,945,1971) new=(501,1431,945,1971) relayoutAsync=true req=(444,540)0 dur=1 res=0x0 s={true 0xb40000787a12ac70} ch=false seqId=0
        2026-10-07 12:30:39.354 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                I  registerCallbackForPendingTransactions
        2026-10-07 12:30:39.355 11133-11294 VRI[PopupW...2]@8f76960 com.example.aquascope                I  mWNT: t=0xb4000076fa10f360 mBlastBufferQueue=0xb4000078aaf27d90 fn= 10 HdrRenderState mRenderHdrSdrRatio=1.0 caller= android.view.ViewRootImpl$10.onFrameDraw:6615 android.view.ViewRootImpl$4.onFrameDraw:2485 android.view.ThreadedRenderer$1.onFrameDraw:751
        2026-10-07 12:30:39.357 11133-11133 BufferQueueConsumer     com.example.aquascope                D  [ImageReader-1920x1440f21m2-11133-0](id:2b7d00000001,api:0,p:-1,c:11133) disconnect
        2026-10-07 12:30:39.358 11133-11153 CameraManagerGlobal     com.example.aquascope                I  Camera 0 facing CAMERA_FACING_BACK state now CAMERA_STATE_OPENING for client com.example.aquascope API Level 2 User Id 0Device Id 0
        2026-10-07 12:30:39.359 11133-11153 CameraManagerGlobal     com.example.aquascope                I  postSingleUpdate device: camera id 0 status STATUS_NOT_AVAILABLE
        2026-10-07 12:30:39.360 11133-11153 CameraManagerGlobal     com.example.aquascope                I  postSingleUpdate device: camera id 0 status STATUS_NOT_AVAILABLE
        2026-10-07 12:30:39.360 11133-11153 CameraManagerGlobal     com.example.aquascope                I  postSingleUpdate device: camera id 0 status STATUS_NOT_AVAILABLE
        2026-10-07 12:30:39.381 11133-11153 CameraManagerGlobal     com.example.aquascope                I  Camera 0 facing CAMERA_FACING_BACK state now CAMERA_STATE_OPEN for client com.example.aquascope API Level 2 User Id 0Device Id 0
        2026-10-07 12:30:39.386 11133-11289 UseCaseAttachState      com.example.aquascope                D  Active and attached use case: [androidx.camera.core.ImageCapture-6100bd1d-40c0-40d7-b8d8-09e64841a773111114251, MeteringRepeating199573429] for camera: 0
        2026-10-07 12:30:39.393 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} CameraDevice.onOpened()
        2026-10-07 12:30:39.393 11133-11289 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Transitioning camera internal state: OPENING --> OPENED
        2026-10-07 12:30:39.394 11133-11289 CameraStateRegistry     com.example.aquascope                D  Recalculating open cameras:
        Camera                                       State
        -------------------------------------------------------------------
        Camera@f74dcd7[id=1]                         UNKNOWN
        Camera@74ef98a[id=0]                         OPEN
        Camera@ad6ae30[id=2]                         UNKNOWN
        -------------------------------------------------------------------
        Open count: 1 (Max allowed: 1)
        2026-10-07 12:30:39.394 11133-11289 CameraStateMachine      com.example.aquascope                D  New public camera state CameraState{type=OPEN, error=null} from OPEN and null
        2026-10-07 12:30:39.394 11133-11289 CameraStateMachine      com.example.aquascope                D  Publishing new public camera state CameraState{type=OPEN, error=null}
        2026-10-07 12:30:39.396 11133-11289 UseCaseAttachState      com.example.aquascope                D  All use case: [androidx.camera.core.ImageCapture-6100bd1d-40c0-40d7-b8d8-09e64841a773111114251, MeteringRepeating199573429] for camera: 0
        2026-10-07 12:30:39.398 11133-11289 SyncCaptureSessionBase  com.example.aquascope                D  [androidx.camera.camera2.internal.SynchronizedCaptureSessionBaseImpl@c8e05fa] getSurface...done
        2026-10-07 12:30:39.398 11133-11289 CaptureSession          com.example.aquascope                D  Opening capture session.
        2026-10-07 12:30:39.403 11133-11289 DeferrableSurface       com.example.aquascope                D  New surface in use[total_surfaces=2, used_surfaces=1](androidx.camera.core.impl.ImmediateSurface@f7a9c3d}
        2026-10-07 12:30:39.403 11133-11289 DeferrableSurface       com.example.aquascope                D  use count+1, useCount=1 androidx.camera.core.impl.ImmediateSurface@f7a9c3d
        2026-10-07 12:30:39.403 11133-11289 DeferrableSurface       com.example.aquascope                D  New surface in use[total_surfaces=2, used_surfaces=2](androidx.camera.core.impl.ImmediateSurface@5ba543e}
        2026-10-07 12:30:39.403 11133-11289 DeferrableSurface       com.example.aquascope                D  use count+1, useCount=1 androidx.camera.core.impl.ImmediateSurface@5ba543e
        2026-10-07 12:30:39.445 11133-11289 CaptureSession          com.example.aquascope                D  Attempting to send capture request onConfigured
        2026-10-07 12:30:39.445 11133-11289 CaptureSession          com.example.aquascope                D  Issuing request for session.
        2026-10-07 12:30:39.451 11133-11289 CaptureSession          com.example.aquascope                D  CameraCaptureSession.onConfigured() mState=OPENED
        2026-10-07 12:30:39.452 11133-11289 CaptureSession          com.example.aquascope                D  CameraCaptureSession.onReady() OPENED
        2026-10-07 12:30:39.455 11133-11443 CameraManagerGlobal     com.example.aquascope                I  Camera 0 facing CAMERA_FACING_BACK state now CAMERA_STATE_ACTIVE for client com.example.aquascope API Level 2 User Id 0Device Id 0
        2026-10-07 12:30:39.938 11133-11133 InsetsController        com.example.aquascope                I  onStateChanged: host=com.example.aquascope/com.example.root.ffttest2.MainActivity, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {3 mType=ime mFrame=[0,0][0,0] mAttachedInsets=null mVisible=false mFlags=INVALID mSideHint=NONE mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=false mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:30:39.942 11133-11133 InsetsController        com.example.aquascope                I  onStateChanged: host=com.example.aquascope/com.example.root.ffttest2.MainActivity, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=false mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {3 mType=ime mFrame=[0,0][0,0] mAttachedInsets=null mVisible=false mFlags=INVALID mSideHint=NONE mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=false mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:30:39.943 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[0,75][1080,2340] display=[0,75][1080,2340] parentFrame=[0,0][0,0]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:30:39.943 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[501,1431][945,1971] display=[-100000,-100000][100000,100000] parentFrame=[0,0][0,0] attachedFrame=[0,75][1080,2340]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:30:40.307 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:30:40.312 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                I  call setFrameRateCategory for touch hint category=high hint, reason=touch, vri=VRI[PopupWindow:abecb92]@8f76960
        2026-10-07 12:30:40.362 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:30:40.362 11133-11133 AbsListView             com.example.aquascope                D  onTouchUp() mTouchMode : 0
        2026-10-07 12:30:40.438 11133-11133 BLASTBufferQueue_Java   com.example.aquascope                I  update, w= 714 h= 810 mName = VRI[PopupWindow:abecb92]@8f76960 mNativeObject= 0xb4000078aaf27d90 sc.mNativeObject= 0xb4000077ea0c65d0 format= -3 caller= android.view.ViewRootImpl.updateBlastSurfaceIfNeeded:3592 android.view.ViewRootImpl.relayoutWindow:11813 android.view.ViewRootImpl.performTraversals:4840 android.view.ViewRootImpl.doTraversal:3959 android.view.ViewRootImpl$TraversalRunnable.run:13034 android.view.Choreographer$CallbackRecord.run:1961
        2026-10-07 12:30:40.438 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                I  Relayout returned: old=(501,1431,945,1971) new=(501,1431,945,1971) relayoutAsync=true req=(444,540)0 dur=0 res=0x0 s={true 0xb40000787a12ac70} ch=false seqId=0
        2026-10-07 12:30:40.438 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                I  registerCallbackForPendingTransactions
        2026-10-07 12:30:40.443 11133-11293 VRI[PopupW...2]@8f76960 com.example.aquascope                I  mWNT: t=0xb4000076fa115dc0 mBlastBufferQueue=0xb4000078aaf27d90 fn= 27 HdrRenderState mRenderHdrSdrRatio=1.0 caller= android.view.ViewRootImpl$10.onFrameDraw:6615 android.view.ViewRootImpl$4.onFrameDraw:2485 android.view.ThreadedRenderer$1.onFrameDraw:751
        2026-10-07 12:30:40.479 11133-11133 log                     com.example.aquascope                D  onstop
        2026-10-07 12:30:40.479 11133-11133 thread                  com.example.aquascope                E  halting......
        2026-10-07 12:30:40.524 11133-11433 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:40.524 11133-11433 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:40.525 11133-11433 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:40.525 11133-11433 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:40.531 11133-11433 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:40.536 11133-11433 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:40.536 11133-11433 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:40.537 11133-11433 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:40.537 11133-11433 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:40.537 11133-11433 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:40.537 11133-11433 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:40.537 11133-11433 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:40.537 11133-11433 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:40.537 11133-11433 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:40.538 11133-11433 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:40.539 11133-11433 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:40.545 11133-11433 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:40.545 11133-11433 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:40.545 11133-11433 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:40.545 11133-11433 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:40.545 11133-11433 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:40.545 11133-11433 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:40.571 11133-11433 log                     com.example.aquascope                E  xcorr_out-1.0 0.0
        2026-10-07 12:30:40.571 11133-11433 log                     com.example.aquascope                E  Listening........ (0.00, 2)
        2026-10-07 12:30:40.571 11133-11433 System.err              com.example.aquascope                W  java.lang.InterruptedException
        2026-10-07 12:30:40.571 11133-11433 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Native Method)
        2026-10-07 12:30:40.571 11133-11433 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep0(Thread.java:689)
        2026-10-07 12:30:40.571 11133-11433 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Thread.java:667)
        2026-10-07 12:30:40.571 11133-11433 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Thread.java:580)
        2026-10-07 12:30:40.571 11133-11433 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.OfflineRecorder.get_FIFO(OfflineRecorder.java:134)
        2026-10-07 12:30:40.571 11133-11433 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.Utils.waitForData(Utils.java:2158)
        2026-10-07 12:30:40.571 11133-11433 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.work(SendChirpAsyncTask.java:672)
        2026-10-07 12:30:40.572 11133-11433 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.doInBackground(SendChirpAsyncTask.java:163)
        2026-10-07 12:30:40.572 11133-11433 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.doInBackground(SendChirpAsyncTask.java:34)
        2026-10-07 12:30:40.572 11133-11433 System.err              com.example.aquascope                W  	at android.os.AsyncTask$3.call(AsyncTask.java:396)
        2026-10-07 12:30:40.572 11133-11433 System.err              com.example.aquascope                W  	at java.util.concurrent.FutureTask.run(FutureTask.java:328)
        2026-10-07 12:30:40.572 11133-11433 System.err              com.example.aquascope                W  	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1100)
        2026-10-07 12:30:40.572 11133-11433 System.err              com.example.aquascope                W  	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
        2026-10-07 12:30:40.572 11133-11433 System.err              com.example.aquascope                W  	at java.lang.Thread.run(Thread.java:1572)
        2026-10-07 12:30:40.670 11133-11437 thread                  com.example.aquascope                E  recording thread end elegantly......
        2026-10-07 12:30:40.779 11133-11133 log                     com.example.aquascope                E  Bob is the receiver; starting the acoustic listen loop
        2026-10-07 12:30:40.781 11133-11553 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/ Bob-AdaptParams-0.txt false
        2026-10-07 12:30:40.782 11133-11553 log                     com.example.aquascope                E  appendtofile Bob-Feedback-log.txt false
        2026-10-07 12:30:40.783 11133-11553 log                     com.example.aquascope                D  Bob-DataRx-0-0
        2026-10-07 12:30:40.783 11133-11553 recorder                com.example.aquascope                E  minbuffersize 7680
        2026-10-07 12:30:40.807 11133-11558 recorder                com.example.aquascope                E  start 1
        2026-10-07 12:30:40.814 11133-11553 log                     com.example.aquascope                E  sig length 960000,DataRx
        2026-10-07 12:30:41.080 11133-11133 log                     com.example.aquascope                D  Current Expmode: end2endCam
        2026-10-07 12:30:41.084 11133-11133 ImeFocusController      com.example.aquascope                I  onPreWindowFocus: skipped hasWindowFocus=false mHasImeFocus=false
        2026-10-07 12:30:41.084 11133-11133 ImeFocusController      com.example.aquascope                I  onPostWindowFocus: skipped hasWindowFocus=false mHasImeFocus=false
        2026-10-07 12:30:41.085 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                D  mThreadedRenderer.initializeIfNeeded()#2 mSurface={isValid=true 0xb40000787a100ef0}
        2026-10-07 12:30:41.087 11133-11133 InputMethodManagerUtils com.example.aquascope                D  startInputInner - Id : 0
        2026-10-07 12:30:41.087 11133-11133 InputMethodManager      com.example.aquascope                I  startInputInner - IInputMethodManagerGlobalInvoker.startInputOrWindowGainedFocus
        2026-10-07 12:30:41.090 11133-11443 InputTransport          com.example.aquascope                D  Input channel constructed: 'ClientS', fd=243
        2026-10-07 12:30:41.097 11133-11133 Choreographer           com.example.aquascope                I  Skipped 36 frames!  The application may be doing too much work on its main thread.
        2026-10-07 12:30:41.100 11133-11133 WindowManager           com.example.aquascope                I  WindowManagerGlobal#removeView, ty=1002, view=android.widget.PopupWindow$PopupDecorView{a2f5963 V.E...... R.....ID 0,0-444,540}, caller=android.view.WindowManagerGlobal.removeView:654 android.view.WindowManagerImpl.removeViewImmediate:254 android.widget.PopupWindow.dismissImmediate:2380
        2026-10-07 12:30:41.100 11133-11133 WindowOnBackDispatcher  com.example.aquascope                D  setTopOnBackInvokedCallback (unwrapped): null
        2026-10-07 12:30:41.103 11133-11133 VRI[PopupW...2]@8f76960 com.example.aquascope                I  dispatchDetachedFromWindow
        2026-10-07 12:30:41.103 11133-11133 BLASTBufferQueue        com.example.aquascope                I  [VRI[PopupWindow:abecb92]@8f76960#1](f:0,a:3,s:0) destructor()
        2026-10-07 12:30:41.103 11133-11133 BufferQueueConsumer     com.example.aquascope                D  [VRI[PopupWindow:abecb92]@8f76960#1(BLAST Consumer)1](id:2b7d00000004,api:0,p:-1,c:11133) disconnect
        2026-10-07 12:30:41.106 11133-11133 InputTransport          com.example.aquascope                D  Input channel destroyed: 'a9bfbcd', fd=195
        2026-10-07 12:30:41.113 11133-11133 InsetsController        com.example.aquascope                D  hide(ime())
        2026-10-07 12:30:41.113 11133-11133 ImeTracker              com.example.aquascope                I  com.example.aquascope:4824ece: onCancelled at PHASE_CLIENT_ALREADY_HIDDEN
        2026-10-07 12:30:41.649 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:30:41.819 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:30:42.540 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:42.540 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:42.542 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:42.542 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:42.544 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:42.550 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:42.550 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:42.550 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:42.550 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:42.551 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:42.551 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:42.551 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:42.551 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:42.551 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:42.552 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:42.553 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:42.559 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:42.559 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:42.559 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:42.571 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:42.571 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:42.571 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:42.629 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 11MB AllocSpace bytes, 35(5180KB) LOS objects, 0% free, 110MB/110MB, paused 893us,13.941ms total 142.812ms
        2026-10-07 12:30:42.636 11133-11142 InputTransport          com.example.aquascope                D  Input channel destroyed: 'ClientS', fd=243
        2026-10-07 12:30:42.677 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 18796.0
        2026-10-07 12:30:42.677 11133-11553 log                     com.example.aquascope                E  Listening........ (0.07, 2)
        2026-10-07 12:30:42.825 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 66MB AllocSpace bytes, 549(42MB) LOS objects, 43% free, 61MB/109MB, paused 3.771ms,10.593ms total 194.854ms
        2026-10-07 12:30:43.003 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:43.003 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:43.004 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:43.004 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:43.006 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:43.012 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:43.012 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:43.012 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:43.012 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:43.013 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:43.013 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:43.013 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:43.013 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:43.014 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:43.014 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:43.016 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:43.021 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:43.021 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:43.022 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:43.022 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:43.022 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:43.022 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:43.031 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 424KB AllocSpace bytes, 1(48KB) LOS objects, 39% free, 66MB/109MB, paused 566us,12.871ms total 68.656ms
        2026-10-07 12:30:43.123 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 20688.0
        2026-10-07 12:30:43.123 11133-11553 log                     com.example.aquascope                E  Listening........ (0.21, 3)
        2026-10-07 12:30:43.165 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 61MB AllocSpace bytes, 515(41MB) LOS objects, 40% free, 70MB/118MB, paused 924us,3.373ms total 100.846ms
        2026-10-07 12:30:43.229 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 5244KB AllocSpace bytes, 38(2888KB) LOS objects, 43% free, 62MB/110MB, paused 510us,11.865ms total 62.335ms
        2026-10-07 12:30:43.581 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:43.581 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:43.582 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:43.582 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:43.584 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:43.586 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:30:43.590 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:43.590 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:43.590 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:43.591 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:43.591 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:43.591 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:43.591 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:43.591 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:43.592 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:43.592 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:43.596 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:43.603 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:43.603 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:43.604 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:43.604 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:43.605 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:43.605 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:43.645 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:30:43.655 11133-11133 log                     com.example.aquascope                E  camera capture
        2026-10-07 12:30:43.666 11133-11133 CameraOrientationUtil   com.example.aquascope                D  getRelativeImageRotation: destRotationDegrees=0, sourceRotationDegrees=90, isOppositeFacing=true, result=90
        2026-10-07 12:30:43.667 11133-11133 ImageCapture            com.example.aquascope                D  Send image capture request [current, pending] = [0, 1]
        2026-10-07 12:30:43.667 11133-11133 ImageCapture            com.example.aquascope                D  issueTakePicture
        2026-10-07 12:30:43.679 11133-11285 Camera2CameraImpl       com.example.aquascope                D  {Camera@74ef98a[id=0]} Issue capture request
        2026-10-07 12:30:43.680 11133-11285 CaptureSession          com.example.aquascope                D  Issuing capture request.
        2026-10-07 12:30:43.725 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 27368.0
        2026-10-07 12:30:43.725 11133-11553 log                     com.example.aquascope                E  Listening........ (0.18, 4)
        2026-10-07 12:30:44.024 11133-11133 DMABUFHEAPS             com.example.aquascope                I  Using DMA-BUF heap named: mtk_mm
        2026-10-07 12:30:44.024 11133-11133 DMABUFHEAPS             com.example.aquascope                I  Using DMA-BUF heap named: mtk_mm-uncached
        2026-10-07 12:30:44.024 11133-11133 DMABUFHEAPS             com.example.aquascope                I  Using DMA-BUF heap named: mtk_svp_region
        2026-10-07 12:30:44.024 11133-11133 DMABUFHEAPS             com.example.aquascope                I  Using DMA-BUF heap named: mtk_prot_region
        2026-10-07 12:30:44.024 11133-11133 DMABUFHEAPS             com.example.aquascope                I  Using DMA-BUF heap named: mtk_wfd_region-aligned
        2026-10-07 12:30:44.041 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:44.041 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:44.042 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:44.042 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:44.044 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:44.044 11133-11570 Kumiho-Kumiho           com.example.aquascope                I  getPackageName: com.example.aquascope
        2026-10-07 12:30:44.047 11133-11570 skia                    com.example.aquascope                D  SkJpegCodec::onGetPixels + (1920, 1440)
        2026-10-07 12:30:44.050 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:44.050 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:44.050 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:44.050 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:44.050 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:44.050 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:44.051 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:44.051 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:44.051 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:44.051 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:44.053 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:44.059 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:44.059 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:44.059 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:44.059 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:44.059 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:44.059 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:44.075 11133-11570 skia                    com.example.aquascope                D  SkJpegCodec::onGetPixels -
        2026-10-07 12:30:44.075 11133-11570 Kumiho-Kumiho           com.example.aquascope                I  getPackageName: com.example.aquascope
        2026-10-07 12:30:44.076 11133-11570 log                     com.example.aquascope                E  bitmap size 1440 1920
        2026-10-07 12:30:44.118 11133-11570 log                     com.example.aquascope                D  Turbidity: variance below floor (0.053475063 < 2.0), frame has no usable detail - check camera or recalibrate baseline
        2026-10-07 12:30:44.118 11133-11570 log                     com.example.aquascope                D  Turbidity: variance=0.053475063 meanLum=0.46048388 turbidity=0.9997861
        2026-10-07 12:30:44.119 11133-11570 log                     com.example.aquascope                D  ContextVector: turbidity=0.9997861 noise=0.0 luminance=0.46048388 laplacian=0.053475063
        2026-10-07 12:30:44.183 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 32288.0
        2026-10-07 12:30:44.183 11133-11553 log                     com.example.aquascope                E  Listening........ (0.08, 5)
        2026-10-07 12:30:44.192 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 34MB AllocSpace bytes, 290(25MB) LOS objects, 0% free, 118MB/118MB, paused 694us,9.253ms total 85.858ms
        2026-10-07 12:30:44.540 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:44.540 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:44.541 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:44.541 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:44.543 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:44.549 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:44.550 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:44.550 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:44.550 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:44.551 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:44.551 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:44.551 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:44.551 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:44.551 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:44.551 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:44.553 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:44.558 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:44.558 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:44.559 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:44.559 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:44.559 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:44.559 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:44.727 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 8288.0
        2026-10-07 12:30:44.727 11133-11553 log                     com.example.aquascope                E  Listening........ (0.08, 6)
        2026-10-07 12:30:44.740 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 39MB AllocSpace bytes, 327(26MB) LOS objects, 0% free, 161MB/161MB, paused 817us,15.792ms total 134.131ms
        2026-10-07 12:30:44.979 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 61MB AllocSpace bytes, 499(38MB) LOS objects, 42% free, 63MB/111MB, paused 793us,15.303ms total 237.583ms
        2026-10-07 12:30:44.996 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:44.996 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:44.997 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:44.997 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:44.999 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:45.005 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:45.005 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:45.005 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:45.005 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:45.005 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:45.006 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:45.006 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:45.006 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:45.006 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:45.006 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:45.008 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:45.013 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:45.013 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:45.014 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:45.014 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:45.015 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:45.015 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:45.170 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:30:45.187 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 15980.0
        2026-10-07 12:30:45.187 11133-11553 log                     com.example.aquascope                E  Listening........ (0.10, 7)
        2026-10-07 12:30:45.206 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 34MB AllocSpace bytes, 275(20MB) LOS objects, 0% free, 113MB/113MB, paused 767us,13.847ms total 88.322ms
        2026-10-07 12:30:45.336 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:30:45.551 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:45.551 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:45.552 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:45.552 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:45.555 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:45.560 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:45.560 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:45.560 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:45.560 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:45.561 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:45.561 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:45.561 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:45.561 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:45.561 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:45.562 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:45.563 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:45.569 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:45.569 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:45.570 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:45.570 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:45.570 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:45.570 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:45.702 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 2650.0
        2026-10-07 12:30:45.705 11133-11553 log                     com.example.aquascope                E  Listening........ (0.10, 8)
        2026-10-07 12:30:46.054 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:46.054 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:46.054 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:46.055 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:46.057 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:46.063 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:46.063 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:46.063 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:46.063 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:46.064 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:46.064 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:46.064 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:46.064 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:46.064 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:46.065 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:46.066 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:46.074 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:46.074 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:46.074 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:46.074 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:46.075 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:46.075 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:46.182 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 13988.0
        2026-10-07 12:30:46.182 11133-11553 log                     com.example.aquascope                E  Listening........ (0.06, 9)
        2026-10-07 12:30:46.425 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 30MB AllocSpace bytes, 242(18MB) LOS objects, 43% free, 61MB/109MB, paused 796us,15.067ms total 209.034ms
        2026-10-07 12:30:46.541 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:46.541 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:46.542 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:46.542 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:46.544 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:46.550 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:46.550 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:46.550 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:46.550 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:46.551 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:46.551 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:46.551 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:46.551 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:46.551 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:46.552 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:46.553 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:46.559 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:46.559 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:46.559 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:46.559 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:46.560 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:46.560 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:46.622 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 19830.0
        2026-10-07 12:30:46.622 11133-11553 log                     com.example.aquascope                E  Listening........ (0.04, 10)
        2026-10-07 12:30:47.024 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:47.024 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:47.024 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:47.025 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:47.026 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:47.032 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:47.032 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:47.032 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:47.032 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:47.033 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:47.033 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:47.033 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:47.033 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:47.033 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:47.034 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:47.035 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:47.043 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:47.043 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:47.044 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:47.044 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:47.044 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:47.045 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:47.157 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 32108.0
        2026-10-07 12:30:47.158 11133-11553 log                     com.example.aquascope                E  Listening........ (0.20, 11)
        2026-10-07 12:30:47.257 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 41MB AllocSpace bytes, 327(25MB) LOS objects, 43% free, 63MB/111MB, paused 266us,3.665ms total 106.962ms
        2026-10-07 12:30:47.579 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:47.579 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:47.580 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:47.580 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:47.582 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:47.588 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:47.588 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:47.589 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:47.589 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:47.589 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:47.589 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:47.589 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:47.589 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:47.590 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:47.590 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:47.591 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:47.597 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:47.597 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:47.597 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:47.597 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:47.598 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:47.598 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:47.695 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 30MB AllocSpace bytes, 250(20MB) LOS objects, 8% free, 101MB/111MB, paused 814us,14.334ms total 58.864ms
        2026-10-07 12:30:47.715 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 8108.0
        2026-10-07 12:30:47.715 11133-11553 log                     com.example.aquascope                E  Listening........ (0.20, 12)
        2026-10-07 12:30:48.042 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:48.042 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:48.043 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:48.043 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:48.045 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:48.052 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:48.052 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:48.053 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:48.053 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:48.053 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:48.053 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:48.053 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:48.053 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:48.054 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:48.054 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:48.056 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:48.061 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:48.061 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:48.062 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:48.062 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:48.062 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:48.062 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:48.072 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 0.0
        2026-10-07 12:30:48.072 11133-11553 log                     com.example.aquascope                E  Listening........ (0.00, 13)
        2026-10-07 12:30:48.337 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=no preference, reason=boost timeout, vri=VRI[MainActivity]@17e0af0
        2026-10-07 12:30:48.540 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:48.540 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:48.541 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:48.541 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:48.543 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:48.550 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:48.550 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:48.551 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:48.551 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:48.551 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:48.551 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:48.551 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:48.551 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:48.552 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:48.552 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:48.553 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:48.558 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:48.559 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:48.559 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:48.559 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:48.559 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:48.559 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:48.668 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 11320.0
        2026-10-07 12:30:48.669 11133-11553 log                     com.example.aquascope                E  Listening........ (0.13, 14)
        2026-10-07 12:30:48.861 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 39MB AllocSpace bytes, 314(25MB) LOS objects, 43% free, 61MB/109MB, paused 839us,12.080ms total 184.248ms
        2026-10-07 12:30:49.015 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:49.015 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:49.016 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:49.016 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:49.017 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:49.023 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:49.023 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:49.023 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:49.023 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:49.024 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:49.024 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:49.024 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:49.024 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:49.025 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:49.025 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:49.026 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:49.033 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:49.033 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:49.034 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:49.034 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:49.034 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:49.034 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:49.195 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 35930.0
        2026-10-07 12:30:49.196 11133-11553 log                     com.example.aquascope                E  Listening........ (0.14, 15)
        2026-10-07 12:30:49.362 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 62MB AllocSpace bytes, 500(37MB) LOS objects, 43% free, 63MB/111MB, paused 1.044ms,11.020ms total 195.560ms
        2026-10-07 12:30:49.582 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:49.582 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:49.582 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:49.583 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:49.587 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:49.593 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:49.593 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:49.594 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:49.594 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:49.595 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:49.595 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:49.595 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:49.595 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:49.595 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:49.595 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:49.597 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:49.603 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:49.603 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:49.603 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:49.603 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:49.604 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:49.604 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:49.727 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 25MB AllocSpace bytes, 212(17MB) LOS objects, 0% free, 130MB/130MB, paused 1.601ms,13.283ms total 94.066ms
        2026-10-07 12:30:49.728 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 24688.0
        2026-10-07 12:30:49.729 11133-11553 log                     com.example.aquascope                E  Listening........ (0.05, 16)
        2026-10-07 12:30:50.050 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:50.050 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:50.051 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:50.051 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:50.053 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:50.059 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:50.059 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:50.060 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:50.060 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:50.060 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:50.060 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:50.061 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:50.061 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:50.061 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:50.061 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:50.063 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:50.069 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:50.069 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:50.069 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:50.069 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:50.070 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:50.070 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:50.123 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 31822.0
        2026-10-07 12:30:50.124 11133-11553 log                     com.example.aquascope                E  Listening........ (0.17, 17)
        2026-10-07 12:30:50.531 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:50.531 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:50.531 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:50.531 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:50.533 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:50.540 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:50.540 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:50.541 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:50.541 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:50.541 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:50.541 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:50.541 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:50.541 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:50.542 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:50.542 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:50.544 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:50.552 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:50.552 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:50.552 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:50.552 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:50.553 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:50.553 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:50.698 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 22422.0
        2026-10-07 12:30:50.699 11133-11553 log                     com.example.aquascope                E  Listening........ (0.20, 18)
        2026-10-07 12:30:50.783 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 68MB AllocSpace bytes, 556(43MB) LOS objects, 42% free, 64MB/112MB, paused 964us,2.910ms total 128.731ms
        2026-10-07 12:30:51.019 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:51.019 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:51.019 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:51.019 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:51.021 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:51.027 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:51.027 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:51.027 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:51.027 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:51.028 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:51.028 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:51.028 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:51.028 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:51.028 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:51.029 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:51.030 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:51.036 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:51.036 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:51.036 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:51.036 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:51.037 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:51.037 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:51.097 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 14618.0
        2026-10-07 12:30:51.097 11133-11553 log                     com.example.aquascope                E  Listening........ (0.08, 19)
        2026-10-07 12:30:51.289 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 25MB AllocSpace bytes, 201(15MB) LOS objects, 43% free, 63MB/111MB, paused 1.236ms,10.834ms total 192.985ms
        2026-10-07 12:30:51.567 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:51.567 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:51.568 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:51.569 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:51.570 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:51.576 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:51.576 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:51.576 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:51.576 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:51.577 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:51.577 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:51.577 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:51.577 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:51.578 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:51.578 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:51.579 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:51.586 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:51.586 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:51.587 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:51.587 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:51.587 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:51.587 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:51.699 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 1616.0
        2026-10-07 12:30:51.700 11133-11553 log                     com.example.aquascope                E  Listening........ (0.06, 20)
        2026-10-07 12:30:51.772 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 38MB AllocSpace bytes, 317(25MB) LOS objects, 0% free, 111MB/111MB, paused 611us,14.235ms total 140.641ms
        2026-10-07 12:30:51.964 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 29MB AllocSpace bytes, 244(20MB) LOS objects, 43% free, 62MB/110MB, paused 928us,11.065ms total 191.875ms
        2026-10-07 12:30:52.049 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:30:52.050 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:52.050 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:52.051 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:52.051 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=high hint, reason=touch, vri=VRI[MainActivity]@17e0af0
        2026-10-07 12:30:52.051 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:52.054 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:52.064 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:52.064 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:52.064 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:52.064 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:52.065 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:52.065 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:52.065 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:52.065 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:52.065 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:52.066 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:52.067 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:52.073 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:52.073 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:52.073 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:52.073 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:52.074 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:52.074 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:52.123 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 36166.0
        2026-10-07 12:30:52.124 11133-11553 log                     com.example.aquascope                E  Listening........ (0.11, 21)
        2026-10-07 12:30:52.209 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 33MB AllocSpace bytes, 278(22MB) LOS objects, 41% free, 64MB/110MB, paused 442us,8.593ms total 94.349ms
        2026-10-07 12:30:52.461 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:30:52.516 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:52.516 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:52.516 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:52.516 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:52.518 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:52.524 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:52.524 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:52.525 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:52.525 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:52.525 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:52.525 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:52.525 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:52.525 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:52.526 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:52.526 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:52.528 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:52.533 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:52.533 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:52.534 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:52.534 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:52.534 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:52.534 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:52.622 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 19428.0
        2026-10-07 12:30:52.622 11133-11553 log                     com.example.aquascope                E  Listening........ (0.15, 22)
        2026-10-07 12:30:52.764 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 68MB AllocSpace bytes, 555(44MB) LOS objects, 42% free, 63MB/111MB, paused 584us,8.532ms total 203.698ms
        2026-10-07 12:30:52.822 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 304KB AllocSpace bytes, 0(0B) LOS objects, 42% free, 63MB/111MB, paused 301us,13.014ms total 58.352ms
        2026-10-07 12:30:53.007 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:53.007 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:53.008 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:53.008 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:53.010 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:53.015 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:53.015 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:53.016 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:53.016 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:53.016 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:53.016 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:53.017 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:53.017 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:53.017 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:53.018 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:53.019 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:53.026 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:53.026 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:53.026 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:53.026 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:53.027 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:53.027 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:53.165 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 7408.0
        2026-10-07 12:30:53.165 11133-11553 log                     com.example.aquascope                E  Listening........ (0.05, 23)
        2026-10-07 12:30:53.329 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 95MB AllocSpace bytes, 783(61MB) LOS objects, 39% free, 72MB/120MB, paused 965us,11.480ms total 259.788ms
        2026-10-07 12:30:53.401 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 5752KB AllocSpace bytes, 42(3192KB) LOS objects, 42% free, 64MB/112MB, paused 1.049ms,9.973ms total 71.580ms
        2026-10-07 12:30:53.557 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:53.557 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:53.557 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:53.557 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:53.559 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:53.564 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:53.565 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:53.565 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:53.565 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:53.565 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:53.565 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:53.565 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:53.565 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:53.566 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:53.566 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:53.567 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:53.573 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:53.573 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:53.573 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:53.573 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:53.574 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:53.574 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:53.675 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 33536.0
        2026-10-07 12:30:53.675 11133-11553 log                     com.example.aquascope                E  Listening........ (0.10, 24)
        2026-10-07 12:30:53.841 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 68MB AllocSpace bytes, 558(44MB) LOS objects, 43% free, 63MB/111MB, paused 838us,11.883ms total 222.087ms
        2026-10-07 12:30:53.932 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:30:53.985 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:30:54.017 11133-11133 WindowManager           com.example.aquascope                I  WindowManagerGlobal#addView, ty=1002, view=android.widget.PopupWindow$PopupDecorView{30e42e0 V.E...... R.....I. 0,0-0,0}, caller=android.view.WindowManagerImpl.addView:167 android.widget.PopupWindow.invokePopup:1686 android.widget.PopupWindow.showAsDropDown:1493
        2026-10-07 12:30:54.017 11133-11133 ViewRootImpl            com.example.aquascope                I  dVRR is disabled
        2026-10-07 12:30:54.018 11133-11220 NativeCust...ncyManager com.example.aquascope                D  [NativeCFMS] BpCustomFrequencyManager::BpCustomFrequencyManager()
        2026-10-07 12:30:54.023 11133-11133 InputTransport          com.example.aquascope                D  Input channel constructed: '618527f', fd=204
        2026-10-07 12:30:54.024 11133-11133 InsetsController        com.example.aquascope                I  onStateChanged: host=PopupWindow:c5ba8e3, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=false mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=false mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:30:54.024 11133-11133 VRI[PopupW...3]@eb2e999 com.example.aquascope                I  synced displayState. AttachInfo displayState=2
        2026-10-07 12:30:54.025 11133-11133 VRI[PopupW...3]@eb2e999 com.example.aquascope                I  setView = android.widget.PopupWindow$PopupDecorView@30e42e0 IsHRR=false mFlingFrameRateChange=0 TM=true
        2026-10-07 12:30:54.028 11133-11133 WindowOnBackDispatcher  com.example.aquascope                D  setTopOnBackInvokedCallback (unwrapped): android.view.ViewRootImpl$$ExternalSyntheticLambda16@9bf8655
        2026-10-07 12:30:54.032 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:54.032 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:54.033 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:54.033 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:54.034 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:54.035 11133-11133 BufferQueueConsumer     com.example.aquascope                D  [](id:2b7d00000006,api:0,p:-1,c:11133) connect: controlledByApp=false
        2026-10-07 12:30:54.035 11133-11133 BLASTBufferQueue        com.example.aquascope                I  [VRI[PopupWindow:c5ba8e3]@eb2e999#2](f:0,a:0,s:0) constructor()
        2026-10-07 12:30:54.036 11133-11133 BLASTBufferQueue_Java   com.example.aquascope                I  new BLASTBufferQueue, mName= VRI[PopupWindow:c5ba8e3]@eb2e999 mNativeObject= 0xb4000078aaf27d90 caller= android.view.ViewRootImpl.updateBlastSurfaceIfNeeded:3603 android.view.ViewRootImpl.relayoutWindow:11813 android.view.ViewRootImpl.performTraversals:4840 android.view.ViewRootImpl.doTraversal:3959 android.view.ViewRootImpl$TraversalRunnable.run:13034 android.view.Choreographer$CallbackRecord.run:1961 android.view.Choreographer$CallbackRecord.run:1970 android.view.Choreographer.doCallbacks:1423 android.view.Choreographer.doFrame:1348 android.view.Choreographer$FrameDisplayEventReceiver.run:1930
        2026-10-07 12:30:54.036 11133-11133 BLASTBufferQueue_Java   com.example.aquascope                I  update, w= 709 h= 945 mName = VRI[PopupWindow:c5ba8e3]@eb2e999 mNativeObject= 0xb4000078aaf27d90 sc.mNativeObject= 0xb4000077ea0d1190 format= -3 caller= android.view.ViewRootImpl.updateBlastSurfaceIfNeeded:3608 android.view.ViewRootImpl.relayoutWindow:11813 android.view.ViewRootImpl.performTraversals:4840 android.view.ViewRootImpl.doTraversal:3959 android.view.ViewRootImpl$TraversalRunnable.run:13034 android.view.Choreographer$CallbackRecord.run:1961
        2026-10-07 12:30:54.036 11133-11133 BLASTBufferQueue        com.example.aquascope                I  [VRI[PopupWindow:c5ba8e3]@eb2e999#2](f:0,a:0,s:0) update width=709 height=945 format=-3 mTransformHint=0
        2026-10-07 12:30:54.036 11133-11133 VRI[PopupW...3]@eb2e999 com.example.aquascope                I  Relayout returned: old=(506,936,945,1611) new=(506,936,945,1611) relayoutAsync=false req=(439,675)0 dur=6 res=0x3 s={true 0xb40000787a132d30} ch=true seqId=0
        2026-10-07 12:30:54.038 11133-11133 VRI[PopupW...3]@eb2e999 com.example.aquascope                I  performConfigurationChange setNightDimText nightDimLevel=0
        2026-10-07 12:30:54.038 11133-11133 VRI[PopupW...3]@eb2e999 com.example.aquascope                D  mThreadedRenderer.initialize() mSurface={isValid=true 0xb40000787a132d30} hwInitialized=true
        2026-10-07 12:30:54.040 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:54.040 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:54.041 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:54.041 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:54.041 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:54.041 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:54.041 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:54.041 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:54.041 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:54.042 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:54.043 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:54.047 11133-11133 AbsListView             com.example.aquascope                D   in onLayout changed
        2026-10-07 12:30:54.048 11133-11133 VRI[PopupW...3]@eb2e999 com.example.aquascope                D  reportNextDraw android.view.ViewRootImpl.performTraversals:5507 android.view.ViewRootImpl.doTraversal:3959 android.view.ViewRootImpl$TraversalRunnable.run:13034 android.view.Choreographer$CallbackRecord.run:1961 android.view.Choreographer$CallbackRecord.run:1970
        2026-10-07 12:30:54.050 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:54.050 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:54.050 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:54.050 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:54.051 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:54.051 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:54.060 11133-11133 VRI[PopupW...3]@eb2e999 com.example.aquascope                D  Setup new sync=wmsSync-VRI[PopupWindow:c5ba8e3]@eb2e999#8
        2026-10-07 12:30:54.060 11133-11133 VRI[PopupW...3]@eb2e999 com.example.aquascope                I  Creating new active sync group VRI[PopupWindow:c5ba8e3]@eb2e999#9
        2026-10-07 12:30:54.061 11133-11133 VRI[PopupW...3]@eb2e999 com.example.aquascope                D  registerCallbacksForSync syncBuffer=false
        2026-10-07 12:30:54.063 11133-11294 VRI[PopupW...3]@eb2e999 com.example.aquascope                D  Received frameDrawingCallback syncResult=0 frameNum=1.
        2026-10-07 12:30:54.064 11133-11294 VRI[PopupW...3]@eb2e999 com.example.aquascope                I  mWNT: t=0xb4000076fa18ed50 mBlastBufferQueue=0xb4000078aaf27d90 fn= 1 HdrRenderState mRenderHdrSdrRatio=1.0 caller= android.view.ViewRootImpl$12.onFrameDraw:15561 android.view.ThreadedRenderer$1.onFrameDraw:751 <bottom of call stack>
        2026-10-07 12:30:54.064 11133-11294 VRI[PopupW...3]@eb2e999 com.example.aquascope                I  Setting up sync and frameCommitCallback
        2026-10-07 12:30:54.070 11133-11220 BLASTBufferQueue        com.example.aquascope                I  [VRI[PopupWindow:c5ba8e3]@eb2e999#2](f:0,a:0,s:0) onFrameAvailable the first frame is available
        2026-10-07 12:30:54.070 11133-11220 BLASTBufferQueue        com.example.aquascope                I  [VRI[PopupWindow:c5ba8e3]@eb2e999#2](f:0,a:1,s:0) acquireNextBufferLocked size=709x945 mFrameNumber=1 applyTransaction=true mTimestamp=222328101481635(auto) mPendingTransactions.size=0 graphicBufferId=47815870906402 transform=0
        2026-10-07 12:30:54.071 11133-11220 VRI[PopupW...3]@eb2e999 com.example.aquascope                I  Received frameCommittedCallback lastAttemptedDrawFrameNum=1 didProduceBuffer=true
        2026-10-07 12:30:54.073 11133-11220 HWUI                    com.example.aquascope                D  CFMS:: SetUp Pid : 11133    Tid : 11220
        2026-10-07 12:30:54.073 11133-11133 VRI[PopupW...3]@eb2e999 com.example.aquascope                D  reportDrawFinished seqId=0
        2026-10-07 12:30:54.078 11133-11220 HWUI                    com.example.aquascope                D  HWUI - treat SMPTE_170M as sRGB
        2026-10-07 12:30:54.079 11133-11133 VRI[PopupW...3]@eb2e999 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[506,936][945,1611] display=[-100000,-100000][100000,100000] parentFrame=[0,0][0,0] attachedFrame=[0,75][1080,2340]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:30:54.079 11133-11133 VRI[PopupW...3]@eb2e999 com.example.aquascope                I  handleResized mSyncSeqId = 0
        2026-10-07 12:30:54.079 11133-11133 VRI[PopupW...3]@eb2e999 com.example.aquascope                D  reportNextDraw android.view.ViewRootImpl.handleResized:2995 android.view.ViewRootImpl.-$$Nest$mhandleResized:0 android.view.ViewRootImpl$W.resized:14200 android.app.servertransaction.WindowStateResizeItem.execute:81 android.app.servertransaction.WindowStateTransactionItem.execute:62
        2026-10-07 12:30:54.080 11133-11133 ImeFocusController      com.example.aquascope                I  onPreWindowFocus: skipped hasWindowFocus=false mHasImeFocus=true
        2026-10-07 12:30:54.080 11133-11133 ImeFocusController      com.example.aquascope                I  onPostWindowFocus: skipped hasWindowFocus=false mHasImeFocus=true
        2026-10-07 12:30:54.084 11133-11133 VRI[PopupW...3]@eb2e999 com.example.aquascope                D  Setup new sync=wmsSync-VRI[PopupWindow:c5ba8e3]@eb2e999#10
        2026-10-07 12:30:54.084 11133-11133 VRI[PopupW...3]@eb2e999 com.example.aquascope                I  Creating new active sync group VRI[PopupWindow:c5ba8e3]@eb2e999#11
        2026-10-07 12:30:54.084 11133-11133 VRI[PopupW...3]@eb2e999 com.example.aquascope                D  registerCallbacksForSync syncBuffer=false
        2026-10-07 12:30:54.084 11133-11293 VRI[PopupW...3]@eb2e999 com.example.aquascope                D  Received frameDrawingCallback syncResult=0 frameNum=2.
        2026-10-07 12:30:54.084 11133-11293 VRI[PopupW...3]@eb2e999 com.example.aquascope                I  Setting up sync and frameCommitCallback
        2026-10-07 12:30:54.086 11133-11220 VRI[PopupW...3]@eb2e999 com.example.aquascope                I  Received frameCommittedCallback lastAttemptedDrawFrameNum=2 didProduceBuffer=true
        2026-10-07 12:30:54.087 11133-11133 VRI[PopupW...3]@eb2e999 com.example.aquascope                D  reportDrawFinished seqId=0
        2026-10-07 12:30:54.110 11133-11133 VRI[PopupW...3]@eb2e999 com.example.aquascope                D  mThreadedRenderer.initializeIfNeeded()#2 mSurface={isValid=true 0xb40000787a132d30}
        2026-10-07 12:30:54.110 11133-11133 ImeFocusController      com.example.aquascope                I  onPreWindowFocus: skipped hasWindowFocus=true mHasImeFocus=false
        2026-10-07 12:30:54.110 11133-11133 ImeFocusController      com.example.aquascope                I  onPostWindowFocus: skipped hasWindowFocus=true mHasImeFocus=false
        2026-10-07 12:30:54.276 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 33810.0
        2026-10-07 12:30:54.276 11133-11553 log                     com.example.aquascope                E  Listening........ (0.07, 25)
        2026-10-07 12:30:54.295 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 46MB AllocSpace bytes, 375(27MB) LOS objects, 36% free, 82MB/130MB, paused 267us,13.342ms total 52.107ms
        2026-10-07 12:30:54.507 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:54.507 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:54.508 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:54.508 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:54.509 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:54.514 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:54.514 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:54.515 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:54.515 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:54.516 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:54.516 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:54.516 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:54.516 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:54.517 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:54.517 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:54.518 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:54.524 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:54.524 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:54.524 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:54.524 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:54.525 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:54.525 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:54.638 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 36904.0
        2026-10-07 12:30:54.638 11133-11553 log                     com.example.aquascope                E  Listening........ (0.06, 26)
        2026-10-07 12:30:54.682 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 35MB AllocSpace bytes, 295(24MB) LOS objects, 0% free, 134MB/134MB, paused 596us,14.047ms total 128.916ms
        2026-10-07 12:30:54.907 11133-11133 VRI[PopupW...3]@eb2e999 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:30:54.908 11133-11133 VRI[PopupW...3]@eb2e999 com.example.aquascope                I  call setFrameRateCategory for touch hint category=high hint, reason=touch, vri=VRI[PopupWindow:c5ba8e3]@eb2e999
        2026-10-07 12:30:54.915 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 43MB AllocSpace bytes, 362(28MB) LOS objects, 43% free, 62MB/110MB, paused 1.007ms,17.906ms total 232.293ms
        2026-10-07 12:30:54.962 11133-11133 VRI[PopupW...3]@eb2e999 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:30:54.962 11133-11133 AbsListView             com.example.aquascope                D  onTouchUp() mTouchMode : 0
        2026-10-07 12:30:54.998 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:54.998 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:54.998 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:54.998 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:55.000 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:55.005 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:55.005 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:55.006 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:55.006 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:55.006 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:55.006 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:55.006 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:55.006 11133-11553 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:55.007 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:55.007 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:55.009 11133-11553 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:55.014 11133-11553 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:55.014 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:55.015 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:55.015 11133-11553 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:55.015 11133-11553 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:55.015 11133-11553 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:55.034 11133-11133 BLASTBufferQueue_Java   com.example.aquascope                I  update, w= 709 h= 945 mName = VRI[PopupWindow:c5ba8e3]@eb2e999 mNativeObject= 0xb4000078aaf27d90 sc.mNativeObject= 0xb4000077ea0d1190 format= -3 caller= android.view.ViewRootImpl.updateBlastSurfaceIfNeeded:3592 android.view.ViewRootImpl.relayoutWindow:11813 android.view.ViewRootImpl.performTraversals:4840 android.view.ViewRootImpl.doTraversal:3959 android.view.ViewRootImpl$TraversalRunnable.run:13034 android.view.Choreographer$CallbackRecord.run:1961
        2026-10-07 12:30:55.034 11133-11133 VRI[PopupW...3]@eb2e999 com.example.aquascope                I  Relayout returned: old=(506,936,945,1611) new=(506,936,945,1611) relayoutAsync=true req=(439,675)0 dur=0 res=0x0 s={true 0xb40000787a132d30} ch=false seqId=0
        2026-10-07 12:30:55.034 11133-11133 VRI[PopupW...3]@eb2e999 com.example.aquascope                I  registerCallbackForPendingTransactions
        2026-10-07 12:30:55.036 11133-11294 VRI[PopupW...3]@eb2e999 com.example.aquascope                I  mWNT: t=0xb4000076fa186290 mBlastBufferQueue=0xb4000078aaf27d90 fn= 21 HdrRenderState mRenderHdrSdrRatio=1.0 caller= android.view.ViewRootImpl$10.onFrameDraw:6615 android.view.ViewRootImpl$4.onFrameDraw:2485 android.view.ThreadedRenderer$1.onFrameDraw:751
        2026-10-07 12:30:55.064 11133-11133 log                     com.example.aquascope                D  onstop
        2026-10-07 12:30:55.064 11133-11133 thread                  com.example.aquascope                E  halting......
        2026-10-07 12:30:55.169 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 26MB AllocSpace bytes, 212(15MB) LOS objects, 8% free, 100MB/110MB, paused 273us,13.253ms total 56.648ms
        2026-10-07 12:30:55.185 11133-11558 thread                  com.example.aquascope                E  recording thread end elegantly......
        2026-10-07 12:30:55.208 11133-11553 log                     com.example.aquascope                E  xcorr_out-1.0 22942.0
        2026-10-07 12:30:55.209 11133-11553 log                     com.example.aquascope                E  Listening........ (0.06, 27)
        2026-10-07 12:30:55.210 11133-11553 System.err              com.example.aquascope                W  java.lang.InterruptedException
        2026-10-07 12:30:55.210 11133-11553 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Native Method)
        2026-10-07 12:30:55.210 11133-11553 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep0(Thread.java:689)
        2026-10-07 12:30:55.210 11133-11553 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Thread.java:667)
        2026-10-07 12:30:55.210 11133-11553 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Thread.java:580)
        2026-10-07 12:30:55.210 11133-11553 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.OfflineRecorder.get_FIFO(OfflineRecorder.java:134)
        2026-10-07 12:30:55.210 11133-11553 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.Utils.waitForData(Utils.java:2158)
        2026-10-07 12:30:55.210 11133-11553 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.work(SendChirpAsyncTask.java:672)
        2026-10-07 12:30:55.210 11133-11553 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.doInBackground(SendChirpAsyncTask.java:230)
        2026-10-07 12:30:55.210 11133-11553 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.doInBackground(SendChirpAsyncTask.java:34)
        2026-10-07 12:30:55.210 11133-11553 System.err              com.example.aquascope                W  	at android.os.AsyncTask$3.call(AsyncTask.java:396)
        2026-10-07 12:30:55.210 11133-11553 System.err              com.example.aquascope                W  	at java.util.concurrent.FutureTask.run(FutureTask.java:328)
        2026-10-07 12:30:55.210 11133-11553 System.err              com.example.aquascope                W  	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1100)
        2026-10-07 12:30:55.210 11133-11553 System.err              com.example.aquascope                W  	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
        2026-10-07 12:30:55.210 11133-11553 System.err              com.example.aquascope                W  	at java.lang.Thread.run(Thread.java:1572)
        2026-10-07 12:30:55.292 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 43MB AllocSpace bytes, 360(26MB) LOS objects, 42% free, 64MB/112MB, paused 156us,2.142ms total 123.062ms
        2026-10-07 12:30:55.365 11133-11133 log                     com.example.aquascope                E  Bob is the receiver; starting the acoustic listen loop
        2026-10-07 12:30:55.368 11133-11584 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/ Bob-AdaptParams-0.txt false
        2026-10-07 12:30:55.369 11133-11584 log                     com.example.aquascope                E  appendtofile Bob-Feedback-log.txt false
        2026-10-07 12:30:55.370 11133-11584 fifo                    com.example.aquascope                E  Bob-Sounding-0-0
        2026-10-07 12:30:55.371 11133-11584 recorder                com.example.aquascope                E  minbuffersize 7680
        2026-10-07 12:30:55.397 11133-11584 len                     com.example.aquascope                E  sig length 72000,Sounding
        2026-10-07 12:30:55.397 11133-11589 recorder                com.example.aquascope                E  start 1
        2026-10-07 12:30:55.673 11133-11133 Choreographer           com.example.aquascope                I  Skipped 36 frames!  The application may be doing too much work on its main thread.
        2026-10-07 12:30:55.674 11133-11133 WindowManager           com.example.aquascope                I  WindowManagerGlobal#removeView, ty=1002, view=android.widget.PopupWindow$PopupDecorView{30e42e0 V.E...... R.....ID 0,0-439,675}, caller=android.view.WindowManagerGlobal.removeView:654 android.view.WindowManagerImpl.removeViewImmediate:254 android.widget.PopupWindow.dismissImmediate:2380
        2026-10-07 12:30:55.675 11133-11133 WindowOnBackDispatcher  com.example.aquascope                D  setTopOnBackInvokedCallback (unwrapped): null
        2026-10-07 12:30:55.679 11133-11133 VRI[PopupW...3]@eb2e999 com.example.aquascope                I  dispatchDetachedFromWindow
        2026-10-07 12:30:55.679 11133-11133 BLASTBufferQueue        com.example.aquascope                I  [VRI[PopupWindow:c5ba8e3]@eb2e999#2](f:0,a:3,s:0) destructor()
        2026-10-07 12:30:55.679 11133-11133 BufferQueueConsumer     com.example.aquascope                D  [VRI[PopupWindow:c5ba8e3]@eb2e999#2(BLAST Consumer)2](id:2b7d00000006,api:0,p:-1,c:11133) disconnect
        2026-10-07 12:30:55.683 11133-11133 InputTransport          com.example.aquascope                D  Input channel destroyed: '618527f', fd=204
        2026-10-07 12:30:55.693 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                D  mThreadedRenderer.initializeIfNeeded()#2 mSurface={isValid=true 0xb40000787a100ef0}
        2026-10-07 12:30:55.695 11133-11133 InputMethodManagerUtils com.example.aquascope                D  startInputInner - Id : 0
        2026-10-07 12:30:55.695 11133-11133 InputMethodManager      com.example.aquascope                I  startInputInner - IInputMethodManagerGlobalInvoker.startInputOrWindowGainedFocus
        2026-10-07 12:30:55.702 11133-11133 InsetsController        com.example.aquascope                D  hide(ime())
        2026-10-07 12:30:55.702 11133-11286 InputTransport          com.example.aquascope                D  Input channel constructed: 'ClientS', fd=170
        2026-10-07 12:30:55.702 11133-11133 ImeTracker              com.example.aquascope                I  com.example.aquascope:e9314c18: onCancelled at PHASE_CLIENT_ALREADY_HIDDEN
        2026-10-07 12:30:55.856 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:30:55.958 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[0,75][1080,2340] display=[0,75][1080,2340] parentFrame=[0,0][0,0]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:30:56.069 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:30:56.928 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:30:57.124 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:57.124 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:57.125 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:57.125 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:57.127 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:57.134 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:57.134 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:57.135 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:57.135 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:57.136 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:57.136 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:57.136 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:57.136 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:57.137 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:57.137 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:57.139 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:57.147 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:57.147 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:57.147 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:57.147 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:57.147 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:57.147 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:57.178 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:30:57.291 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 38MB AllocSpace bytes, 301(24MB) LOS objects, 0% free, 159MB/159MB, paused 2.129ms,11.955ms total 117.968ms
        2026-10-07 12:30:57.292 11133-11142 InputTransport          com.example.aquascope                D  Input channel destroyed: 'ClientS', fd=170
        2026-10-07 12:30:57.308 11133-11584 log                     com.example.aquascope                E  Listening... (0.16)
        2026-10-07 12:30:57.546 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 64MB AllocSpace bytes, 531(40MB) LOS objects, 40% free, 69MB/117MB, paused 1.651ms,10.164ms total 254.673ms
        2026-10-07 12:30:57.579 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:57.579 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:57.579 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:57.579 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:57.581 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:57.586 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:57.587 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:57.587 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:57.587 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:57.587 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:57.587 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:57.587 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:57.587 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:57.588 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:57.588 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:57.590 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:57.595 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:57.595 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:57.596 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:57.596 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:57.596 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:57.596 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:57.705 11133-11584 log                     com.example.aquascope                E  Listening... (0.16)
        2026-10-07 12:30:57.726 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 34MB AllocSpace bytes, 283(23MB) LOS objects, 0% free, 124MB/124MB, paused 813us,13.142ms total 91.627ms
        2026-10-07 12:30:57.905 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 33MB AllocSpace bytes, 280(23MB) LOS objects, 41% free, 68MB/116MB, paused 548us,9.496ms total 177.880ms
        2026-10-07 12:30:58.149 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:58.149 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:58.150 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:58.150 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:58.152 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:58.157 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:58.157 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:58.158 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:58.158 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:58.158 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:58.158 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:58.158 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:58.158 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:58.159 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:58.159 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:58.161 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:58.166 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:58.166 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:58.167 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:58.167 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:58.167 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:58.167 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:58.273 11133-11584 log                     com.example.aquascope                E  Listening... (0.09)
        2026-10-07 12:30:58.358 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 41MB AllocSpace bytes, 342(27MB) LOS objects, 4% free, 111MB/116MB, paused 631us,11.090ms total 146.316ms
        2026-10-07 12:30:58.471 11133-11133 InsetsController        com.example.aquascope                I  onStateChanged: host=com.example.aquascope/com.example.root.ffttest2.MainActivity, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {3 mType=ime mFrame=[0,0][0,0] mAttachedInsets=null mVisible=false mFlags=INVALID mSideHint=NONE mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=false mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:30:58.478 11133-11133 InsetsController        com.example.aquascope                I  onStateChanged: host=com.example.aquascope/com.example.root.ffttest2.MainActivity, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=false mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {3 mType=ime mFrame=[0,0][0,0] mAttachedInsets=null mVisible=false mFlags=INVALID mSideHint=NONE mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=false mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:30:58.478 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[0,75][1080,2340] display=[0,75][1080,2340] parentFrame=[0,0][0,0]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:30:58.603 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 26MB AllocSpace bytes, 213(16MB) LOS objects, 41% free, 68MB/116MB, paused 1.851ms,10.684ms total 243.655ms
        2026-10-07 12:30:58.655 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:58.655 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:58.655 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:58.656 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:58.657 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:58.663 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:58.663 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:58.663 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:58.663 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:58.664 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:58.664 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:58.664 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:58.664 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:58.664 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:58.664 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:58.666 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:58.676 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:58.676 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:58.676 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:58.676 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:58.676 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:58.677 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:58.790 11133-11584 log                     com.example.aquascope                E  Listening... (0.06)
        2026-10-07 12:30:58.853 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 39MB AllocSpace bytes, 325(26MB) LOS objects, 1% free, 114MB/116MB, paused 1.235ms,13.455ms total 132.911ms
        2026-10-07 12:30:58.939 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:30:59.097 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 28MB AllocSpace bytes, 230(18MB) LOS objects, 40% free, 70MB/118MB, paused 590us,9.611ms total 243.377ms
        2026-10-07 12:30:59.124 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:59.124 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:59.124 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:59.124 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:59.126 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:59.131 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:59.131 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:59.132 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:59.132 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:59.132 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:59.132 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:59.132 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:59.132 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:59.133 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:59.133 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:59.136 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:59.146 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:59.146 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:59.146 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:59.146 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:59.147 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:59.147 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:59.195 11133-11584 log                     com.example.aquascope                E  Listening... (0.10)
        2026-10-07 12:30:59.267 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:30:59.290 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 33MB AllocSpace bytes, 278(22MB) LOS objects, 40% free, 70MB/118MB, paused 2.007ms,10.493ms total 101.697ms
        2026-10-07 12:30:59.596 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:59.596 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:59.596 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:59.596 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:59.598 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:59.603 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:59.603 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:59.603 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:59.603 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:59.604 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:59.604 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:59.604 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:30:59.604 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:30:59.605 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:30:59.605 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:30:59.608 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:30:59.615 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:30:59.615 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:30:59.615 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:30:59.615 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:30:59.616 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:30:59.616 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:30:59.763 11133-11584 log                     com.example.aquascope                E  Listening... (0.13)
        2026-10-07 12:30:59.924 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 69MB AllocSpace bytes, 566(45MB) LOS objects, 28% free, 121MB/169MB, paused 1.113ms,11.223ms total 282.853ms
        2026-10-07 12:31:00.032 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 31MB AllocSpace bytes, 256(19MB) LOS objects, 40% free, 71MB/119MB, paused 828us,12.097ms total 107.406ms
        2026-10-07 12:31:00.152 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:00.152 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:00.153 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:00.153 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:00.155 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:00.160 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:00.161 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:00.161 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:00.161 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:00.162 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:00.162 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:00.162 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:00.162 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:00.162 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:00.163 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:00.164 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:00.170 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:00.170 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:00.170 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:00.171 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:00.171 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:00.171 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:00.270 11133-11584 log                     com.example.aquascope                E  Listening... (0.05)
        2026-10-07 12:31:00.446 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 67MB AllocSpace bytes, 561(45MB) LOS objects, 40% free, 70MB/118MB, paused 958us,10.183ms total 227.269ms
        2026-10-07 12:31:00.671 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:00.672 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:00.672 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:00.672 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:00.674 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:00.681 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:00.681 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:00.682 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:00.682 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:00.682 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:00.682 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:00.682 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:00.682 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:00.683 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:00.683 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:00.686 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:00.691 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:00.692 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:00.692 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:00.692 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:00.693 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:00.693 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:00.859 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 21MB AllocSpace bytes, 182(15MB) LOS objects, 0% free, 186MB/186MB, paused 702us,19.215ms total 142.686ms
        2026-10-07 12:31:00.870 11133-11584 log                     com.example.aquascope                E  Listening... (0.16)
        2026-10-07 12:31:00.984 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 80MB AllocSpace bytes, 644(48MB) LOS objects, 40% free, 69MB/117MB, paused 261us,3.158ms total 125.036ms
        2026-10-07 12:31:01.125 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:01.125 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:01.126 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:01.126 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:01.129 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:01.135 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:01.135 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:01.136 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:01.136 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:01.136 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:01.137 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:01.137 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:01.137 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:01.138 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:01.138 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:01.140 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:01.149 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:01.149 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:01.150 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:01.150 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:01.150 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:01.150 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:01.266 11133-11584 log                     com.example.aquascope                E  Listening... (0.16)
        2026-10-07 12:31:01.335 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 34MB AllocSpace bytes, 285(23MB) LOS objects, 0% free, 124MB/124MB, paused 1.140ms,14.854ms total 142.485ms
        2026-10-07 12:31:01.590 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 33MB AllocSpace bytes, 276(22MB) LOS objects, 40% free, 70MB/118MB, paused 740us,12.010ms total 254.433ms
        2026-10-07 12:31:01.621 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:01.621 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:01.622 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:01.622 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:01.624 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:01.629 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:01.629 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:01.630 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:01.630 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:01.630 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:01.630 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:01.630 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:01.630 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:01.631 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:01.632 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:01.634 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:01.640 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:01.640 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:01.641 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:01.641 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:01.642 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:01.642 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:01.776 11133-11584 log                     com.example.aquascope                E  Listening... (0.15)
        2026-10-07 12:31:01.801 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 41MB AllocSpace bytes, 341(27MB) LOS objects, 5% free, 111MB/118MB, paused 2.096ms,15.411ms total 106.558ms
        2026-10-07 12:31:02.046 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 26MB AllocSpace bytes, 216(17MB) LOS objects, 41% free, 68MB/116MB, paused 800us,11.937ms total 244.523ms
        2026-10-07 12:31:02.161 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:02.161 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:02.162 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:02.162 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:02.164 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:02.170 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:02.170 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:02.171 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:02.171 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:02.171 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:02.171 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:02.171 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:02.171 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:02.172 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:02.172 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:02.173 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:02.180 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:02.180 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:02.180 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:02.180 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:02.181 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:02.181 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:02.268 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=no preference, reason=boost timeout, vri=VRI[MainActivity]@17e0af0
        2026-10-07 12:31:02.340 11133-11584 log                     com.example.aquascope                E  Listening... (0.22)
        2026-10-07 12:31:02.385 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 39MB AllocSpace bytes, 322(26MB) LOS objects, 0% free, 168MB/168MB, paused 851us,16.714ms total 155.831ms
        2026-10-07 12:31:02.656 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 62MB AllocSpace bytes, 504(38MB) LOS objects, 40% free, 70MB/118MB, paused 838us,12.920ms total 269.988ms
        2026-10-07 12:31:02.658 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:02.658 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:02.659 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:02.659 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:02.661 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:02.668 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:02.668 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:02.668 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:02.668 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:02.668 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:02.668 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:02.668 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:02.668 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:02.669 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:02.669 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:02.671 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:02.677 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:02.677 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:02.677 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:02.677 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:02.678 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:02.678 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:02.732 11133-11584 log                     com.example.aquascope                E  Listening... (0.05)
        2026-10-07 12:31:03.115 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:03.115 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:03.115 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:03.115 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:03.117 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:03.124 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:03.124 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:03.124 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:03.124 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:03.125 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:03.125 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:03.125 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:03.125 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:03.125 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:03.126 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:03.127 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:03.133 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:03.133 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:03.133 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:03.133 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:03.134 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:03.134 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:03.301 11133-11584 log                     com.example.aquascope                E  Listening... (0.26)
        2026-10-07 12:31:03.361 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 71MB AllocSpace bytes, 577(43MB) LOS objects, 39% free, 73MB/121MB, paused 305us,3.691ms total 119.195ms
        2026-10-07 12:31:03.611 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:03.611 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:03.611 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:03.611 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:03.613 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:03.620 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:03.620 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:03.621 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:03.621 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:03.622 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:03.622 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:03.622 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:03.622 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:03.623 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:03.623 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:03.625 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:03.631 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:03.631 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:03.631 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:03.631 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:03.632 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:03.632 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:03.748 11133-11584 log                     com.example.aquascope                E  Listening... (0.16)
        2026-10-07 12:31:03.835 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 44MB AllocSpace bytes, 366(28MB) LOS objects, 34% free, 92MB/140MB, paused 288us,2.751ms total 135.544ms
        2026-10-07 12:31:04.171 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:04.171 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:04.172 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:04.172 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:04.174 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:04.179 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:04.179 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:04.180 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:04.180 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:04.181 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:04.181 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:04.181 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:04.181 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:04.181 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:04.182 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:04.183 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:04.191 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:04.191 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:04.191 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:04.192 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:04.192 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:04.192 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:04.278 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 29MB AllocSpace bytes, 242(20MB) LOS objects, 21% free, 109MB/140MB, paused 357us,16.835ms total 62.754ms
        2026-10-07 12:31:04.382 11133-11584 log                     com.example.aquascope                E  Listening... (0.23)
        2026-10-07 12:31:04.450 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 28MB AllocSpace bytes, 227(16MB) LOS objects, 13% free, 121MB/140MB, paused 551us,13.361ms total 129.682ms
        2026-10-07 12:31:04.656 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 31MB AllocSpace bytes, 262(21MB) LOS objects, 40% free, 70MB/118MB, paused 1ms,19.488ms total 204.049ms
        2026-10-07 12:31:04.661 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:04.662 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:04.662 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:04.662 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:04.664 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:04.670 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:04.670 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:04.670 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:04.670 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:04.671 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:04.671 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:04.671 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:04.671 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:04.672 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:04.672 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:04.673 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:04.679 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:04.679 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:04.680 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:04.680 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:04.680 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:04.680 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:04.807 11133-11584 log                     com.example.aquascope                E  Listening... (0.11)
        2026-10-07 12:31:04.842 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 40MB AllocSpace bytes, 334(26MB) LOS objects, 4% free, 112MB/118MB, paused 3.374ms,14.760ms total 109.767ms
        2026-10-07 12:31:05.110 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 27MB AllocSpace bytes, 221(17MB) LOS objects, 40% free, 70MB/118MB, paused 2.224ms,12.911ms total 266.836ms
        2026-10-07 12:31:05.115 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:05.115 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:05.116 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:05.116 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:05.118 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:05.124 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:05.124 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:05.125 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:05.125 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:05.126 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:05.126 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:05.126 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:05.126 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:05.126 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:05.127 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:05.128 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:05.134 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:05.134 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:05.134 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:05.134 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:05.135 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:05.135 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:05.262 11133-11584 log                     com.example.aquascope                E  Listening... (0.08)
        2026-10-07 12:31:05.282 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 38MB AllocSpace bytes, 318(25MB) LOS objects, 1% free, 115MB/118MB, paused 640us,13.772ms total 97.229ms
        2026-10-07 12:31:05.517 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 29MB AllocSpace bytes, 237(18MB) LOS objects, 41% free, 68MB/116MB, paused 3.746ms,14.942ms total 233.466ms
        2026-10-07 12:31:05.598 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:05.598 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:05.598 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:05.598 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:05.600 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:05.606 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:05.606 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:05.606 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:05.606 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:05.607 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:05.607 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:05.607 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:05.607 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:05.607 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:05.608 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:05.609 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:05.615 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:05.615 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:05.615 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:05.616 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:05.616 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:05.616 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:05.733 11133-11584 log                     com.example.aquascope                E  Listening... (0.17)
        2026-10-07 12:31:05.874 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 30MB AllocSpace bytes, 244(19MB) LOS objects, 41% free, 68MB/116MB, paused 180us,2.500ms total 137.652ms
        2026-10-07 12:31:05.938 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:31:05.939 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=high hint, reason=touch, vri=VRI[MainActivity]@17e0af0
        2026-10-07 12:31:05.975 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:31:06.196 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:06.196 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:06.196 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:06.196 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:06.198 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:06.205 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:06.205 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:06.205 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:06.206 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:06.206 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:06.206 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:06.206 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:06.206 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:06.206 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:06.207 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:06.208 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:06.214 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:06.214 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:06.214 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:06.214 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:06.215 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:06.215 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:06.323 11133-11584 log                     com.example.aquascope                E  Listening... (0.17)
        2026-10-07 12:31:06.397 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 43MB AllocSpace bytes, 354(28MB) LOS objects, 6% free, 109MB/116MB, paused 657us,9.313ms total 137.023ms
        2026-10-07 12:31:06.631 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 25MB AllocSpace bytes, 201(15MB) LOS objects, 40% free, 70MB/118MB, paused 652us,10.012ms total 233.693ms
        2026-10-07 12:31:06.670 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:06.670 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:06.671 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:06.671 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:06.672 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:06.679 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:06.679 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:06.679 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:06.679 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:06.680 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:06.680 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:06.680 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:06.680 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:06.680 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:06.681 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:06.682 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:06.688 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:06.688 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:06.689 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:06.689 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:06.690 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:06.690 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:06.805 11133-11584 log                     com.example.aquascope                E  Listening... (0.16)
        2026-10-07 12:31:07.165 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:07.165 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:07.166 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:07.166 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:07.168 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:07.174 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:07.174 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:07.174 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:07.174 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:07.175 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:07.175 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:07.175 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:07.175 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:07.176 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:07.176 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:07.178 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:07.183 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:07.183 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:07.184 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:07.184 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:07.184 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:07.184 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:07.310 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 29MB AllocSpace bytes, 241(20MB) LOS objects, 0% free, 128MB/128MB, paused 235us,13.662ms total 81.149ms
        2026-10-07 12:31:07.358 11133-11584 log                     com.example.aquascope                E  Listening... (0.17)
        2026-10-07 12:31:07.513 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 66MB AllocSpace bytes, 536(40MB) LOS objects, 37% free, 80MB/128MB, paused 420us,11ms total 202.726ms
        2026-10-07 12:31:07.610 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:07.610 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:07.611 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:07.611 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:07.612 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:07.618 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:07.619 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:07.619 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:07.619 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:07.620 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:07.620 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:07.620 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:07.620 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:07.620 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:07.620 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:07.622 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:07.630 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:07.630 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:07.630 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:07.630 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:07.631 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:07.631 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:07.770 11133-11584 log                     com.example.aquascope                E  Listening... (0.05)
        2026-10-07 12:31:07.784 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 23MB AllocSpace bytes, 201(17MB) LOS objects, 0% free, 151MB/151MB, paused 605us,15.498ms total 129.932ms
        2026-10-07 12:31:08.091 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 50MB AllocSpace bytes, 415(32MB) LOS objects, 41% free, 68MB/116MB, paused 4.016ms,18.280ms total 305.395ms
        2026-10-07 12:31:08.179 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:08.179 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:08.180 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:08.180 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:08.181 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:08.187 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:08.187 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:08.188 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:08.188 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:08.188 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:08.188 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:08.188 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:08.188 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:08.189 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:08.189 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:08.191 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:08.197 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:08.197 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:08.198 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:08.198 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:08.198 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:08.198 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:08.253 11133-11584 log                     com.example.aquascope                E  Listening... (0.13)
        2026-10-07 12:31:08.352 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 33MB AllocSpace bytes, 278(22MB) LOS objects, 39% free, 70MB/116MB, paused 565us,12.709ms total 106.614ms
        2026-10-07 12:31:08.671 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:08.671 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:08.672 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:08.672 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:08.674 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:08.679 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:08.680 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:08.680 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:08.681 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:08.681 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:08.681 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:08.681 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:08.681 11133-11584 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:08.682 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:08.682 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:08.683 11133-11584 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:08.689 11133-11584 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:08.689 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:08.690 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:08.690 11133-11584 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:08.691 11133-11584 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:08.691 11133-11584 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:08.826 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 49MB AllocSpace bytes, 401(32MB) LOS objects, 27% free, 127MB/175MB, paused 391us,2.888ms total 105.886ms
        2026-10-07 12:31:08.860 11133-11584 log                     com.example.aquascope                E  Listening... (0.20)
        2026-10-07 12:31:08.976 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=no preference, reason=boost timeout, vri=VRI[MainActivity]@17e0af0
        2026-10-07 12:31:08.981 11133-11133 log                     com.example.aquascope                D  onstop
        2026-10-07 12:31:08.981 11133-11584 System.err              com.example.aquascope                W  java.lang.InterruptedException
        2026-10-07 12:31:08.981 11133-11133 thread                  com.example.aquascope                E  halting......
        2026-10-07 12:31:08.981 11133-11133 log                     com.example.aquascope                E  start startmethod from startWrapper
        2026-10-07 12:31:08.981 11133-11584 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Native Method)
        2026-10-07 12:31:08.981 11133-11584 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep0(Thread.java:689)
        2026-10-07 12:31:08.981 11133-11584 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Thread.java:667)
        2026-10-07 12:31:08.981 11133-11584 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Thread.java:580)
        2026-10-07 12:31:08.981 11133-11584 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.OfflineRecorder.get_FIFO(OfflineRecorder.java:134)
        2026-10-07 12:31:08.981 11133-11584 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.Utils.waitForChirp(Utils.java:1759)
        2026-10-07 12:31:08.981 11133-11584 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.work(SendChirpAsyncTask.java:599)
        2026-10-07 12:31:08.981 11133-11584 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.doInBackground(SendChirpAsyncTask.java:230)
        2026-10-07 12:31:08.981 11133-11584 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.doInBackground(SendChirpAsyncTask.java:34)
        2026-10-07 12:31:08.981 11133-11584 System.err              com.example.aquascope                W  	at android.os.AsyncTask$3.call(AsyncTask.java:396)
        2026-10-07 12:31:08.981 11133-11584 System.err              com.example.aquascope                W  	at java.util.concurrent.FutureTask.run(FutureTask.java:328)
        2026-10-07 12:31:08.981 11133-11584 System.err              com.example.aquascope                W  	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1100)
        2026-10-07 12:31:08.981 11133-11584 System.err              com.example.aquascope                W  	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
        2026-10-07 12:31:08.981 11133-11584 System.err              com.example.aquascope                W  	at java.lang.Thread.run(Thread.java:1572)
        2026-10-07 12:31:08.985 11133-11133 log                     com.example.aquascope                E  startMethod: manual veto selected ACOUSTIC; sending on the acoustic channel only
        2026-10-07 12:31:08.990 11133-11599 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/5_1791356468986 Alice-AdaptParams-0.txt false
        2026-10-07 12:31:08.992 11133-11599 log                     com.example.aquascope                E  appendtofile Alice-Sounding-log.txt false
        2026-10-07 12:31:08.994 11133-11599 log                     com.example.aquascope                E  appendtofile Alice-Data-log.txt false
        2026-10-07 12:31:08.997 11133-11599 log                     com.example.aquascope                E  appendtofile Alice-Battery_Level-log.txt false
        2026-10-07 12:31:09.172 11133-11589 thread                  com.example.aquascope                E  recording thread end elegantly......
        2026-10-07 12:31:11.539 11133-11599 log                     com.example.aquascope                D  use codebook 4096 do nothing
        2026-10-07 12:31:11.539 11133-11599 log                     com.example.aquascope                E  Sender_Latency_Str: 2026-10-07 12:31:08.988
        OFDM_freq_adapt
        sender encode image (ms): 2489
        2026-10-07 12:31:11.539 11133-11599 log                     com.example.aquascope                E  send embedding: [168, 3276, 3765, 2485, 933, 2553, 2972, 2543, 1666, 1209, 1497, 554, 533, 4085, 4089, 3718, 702, 3483, 1958, 3027, 2371, 3751, 3302, 2371, 338, 222, 3914, 1195, 3957, 2044, 956, 821, 552, 2415, 880, 2953, 2082, 712, 385, 3082, 2140, 3844, 3635, 989, 1363, 2513, 3335, 2918, 3977, 256, 2866, 1576, 3522, 557, 2195, 2997, 371, 3375, 1809, 3275, 2021, 3361, 359, 2241]
        2026-10-07 12:31:11.539 11133-11599 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/5_1791356468986 Alice-Send_Embedding_Sequence-0.txt false
        2026-10-07 12:31:11.540 11133-11599 log                     com.example.aquascope                D  use codebook 4096 do nothing
        2026-10-07 12:31:16.637 11133-11141 ample.aquascope         com.example.aquascope                I  NativeAlloc concurrent mark compact GC freed 15MB AllocSpace bytes, 134(11MB) LOS objects, 41% free, 68MB/116MB, paused 840us,4.189ms total 213.170ms
        2026-10-07 12:31:16.860 11133-11599 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/5_1791356468986,Alice-Sounding-0.txt false
        2026-10-07 12:31:16.885 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:16.891 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:16.892 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:16.893 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:16.919 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:16.922 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:16.926 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:16.931 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:16.933 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:16.964 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:16.978 11133-11599 AudioSystem             com.example.aquascope                D  onNewService: media.audio_policy service obtained 0xb4000078ba105cc0
        2026-10-07 12:31:16.979 11133-11599 AudioSystem             com.example.aquascope                D  getService: IAudioPolicyService retrieved: 0xb4000078ba105cc0  cached: 0xb4000078ba105cc0
        2026-10-07 12:31:16.997 11133-11599 AudioTrack              com.example.aquascope                W  Use of stream types is deprecated for operations other than volume control
        2026-10-07 12:31:16.997 11133-11599 AudioTrack              com.example.aquascope                W  See the documentation of AudioTrack() for what to use instead with android.media.AudioAttributes to qualify your playback use case
        2026-10-07 12:31:16.999 11133-11599 log                     com.example.aquascope                E  appendtofile Alice-Sounding-log.txt false
        2026-10-07 12:31:17.000 11133-11599 AudioTrack              com.example.aquascope                D  setVolume(0.600000, 0.600000) pid : 11133
        2026-10-07 12:31:17.544 11133-11599 fifo                    com.example.aquascope                E  Alice-Feedback-0-0
        2026-10-07 12:31:17.544 11133-11599 recorder                com.example.aquascope                E  minbuffersize 7680
        2026-10-07 12:31:17.565 11133-11599 len                     com.example.aquascope                E  sig length 72000,Feedback
        2026-10-07 12:31:17.565 11133-11613 recorder                com.example.aquascope                E  start 1
        2026-10-07 12:31:18.120 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[0,75][1080,2340] display=[0,75][1080,2340] parentFrame=[0,0][0,0]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:31:19.289 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:19.289 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:19.290 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:19.290 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:19.292 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:19.299 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:19.299 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:19.299 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:19.300 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:19.300 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:19.300 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:19.300 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:19.300 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:19.301 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:19.301 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:19.302 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:19.308 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:19.308 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:19.309 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:19.309 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:19.310 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:19.310 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:19.435 11133-11599 log                     com.example.aquascope                E  Listening... (0.09)
        2026-10-07 12:31:19.541 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 46MB AllocSpace bytes, 355(28MB) LOS objects, 1% free, 114MB/116MB, paused 621us,14.520ms total 187.676ms
        2026-10-07 12:31:19.753 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:19.753 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:19.754 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:19.754 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:19.756 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:19.765 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:19.765 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:19.766 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:19.766 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:19.766 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:19.766 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:19.766 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:19.766 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:19.767 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:19.767 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:19.769 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:19.774 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:19.774 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:19.775 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:19.775 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:19.776 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:19.776 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:19.803 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 24MB AllocSpace bytes, 208(16MB) LOS objects, 35% free, 88MB/136MB, paused 1.950ms,12.696ms total 259.829ms
        2026-10-07 12:31:19.964 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 40MB AllocSpace bytes, 335(27MB) LOS objects, 0% free, 146MB/146MB, paused 1.981ms,13.524ms total 120.229ms
        2026-10-07 12:31:19.987 11133-11599 log                     com.example.aquascope                E  Listening... (0.15)
        2026-10-07 12:31:20.217 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 59MB AllocSpace bytes, 487(36MB) LOS objects, 38% free, 75MB/123MB, paused 3.644ms,11.290ms total 252.889ms
        2026-10-07 12:31:20.346 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:20.346 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:20.346 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:20.347 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:20.349 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:20.355 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:20.355 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:20.355 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:20.356 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:20.356 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:20.356 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:20.356 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:20.356 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:20.357 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:20.357 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:20.359 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:20.366 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:20.366 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:20.367 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:20.367 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:20.367 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:20.367 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:20.433 11133-11599 log                     com.example.aquascope                E  Listening... (0.09)
        2026-10-07 12:31:20.521 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 29MB AllocSpace bytes, 246(20MB) LOS objects, 32% free, 83MB/123MB, paused 810us,11.109ms total 111.528ms
        2026-10-07 12:31:20.616 11133-11133 InsetsController        com.example.aquascope                I  onStateChanged: host=com.example.aquascope/com.example.root.ffttest2.MainActivity, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {3 mType=ime mFrame=[0,0][0,0] mAttachedInsets=null mVisible=false mFlags=INVALID mSideHint=NONE mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=false mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:31:20.619 11133-11133 InsetsController        com.example.aquascope                I  onStateChanged: host=com.example.aquascope/com.example.root.ffttest2.MainActivity, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=false mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {3 mType=ime mFrame=[0,0][0,0] mAttachedInsets=null mVisible=false mFlags=INVALID mSideHint=NONE mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=false mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:31:20.619 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[0,75][1080,2340] display=[0,75][1080,2340] parentFrame=[0,0][0,0]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:31:20.818 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 5268KB AllocSpace bytes, 44(5400KB) LOS objects, 38% free, 75MB/123MB, paused 1.194ms,12.104ms total 296.727ms
        2026-10-07 12:31:20.831 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:20.831 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:20.832 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:20.832 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:20.834 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:20.841 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:20.841 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:20.841 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:20.841 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:20.842 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:20.842 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:20.842 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:20.842 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:20.843 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:20.843 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:20.845 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:20.851 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:20.851 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:20.852 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:20.852 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:20.852 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:20.852 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:20.985 11133-11599 log                     com.example.aquascope                E  Listening... (0.06)
        2026-10-07 12:31:21.081 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 38MB AllocSpace bytes, 319(25MB) LOS objects, 1% free, 121MB/123MB, paused 1.036ms,12.340ms total 173.372ms
        2026-10-07 12:31:21.318 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:21.318 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:21.319 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:21.319 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:21.321 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:21.329 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:21.329 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:21.329 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:21.329 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:21.330 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:21.330 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:21.330 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:21.330 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:21.331 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:21.331 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:21.333 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:21.342 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:21.342 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:21.343 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:21.343 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:21.343 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:21.343 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:21.401 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 29MB AllocSpace bytes, 236(18MB) LOS objects, 28% free, 119MB/167MB, paused 1.812ms,16.812ms total 320.040ms
        2026-10-07 12:31:21.482 11133-11599 log                     com.example.aquascope                E  Listening... (0.11)
        2026-10-07 12:31:21.557 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 48MB AllocSpace bytes, 401(31MB) LOS objects, 31% free, 105MB/153MB, paused 836us,13.412ms total 128.182ms
        2026-10-07 12:31:21.773 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:21.773 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:21.774 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:21.774 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:21.776 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:21.784 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:21.784 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:21.784 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:21.784 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:21.785 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:21.785 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:21.785 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:21.785 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:21.786 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:21.787 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:21.789 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:21.796 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:21.796 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:21.796 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:21.796 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:21.797 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:21.797 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:21.957 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 29MB AllocSpace bytes, 248(20MB) LOS objects, 0% free, 166MB/166MB, paused 963us,18.220ms total 140.295ms
        2026-10-07 12:31:21.957 11133-11599 log                     com.example.aquascope                E  Listening... (0.11)
        2026-10-07 12:31:22.277 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 56MB AllocSpace bytes, 463(36MB) LOS objects, 39% free, 74MB/122MB, paused 1.053ms,16.536ms total 319.914ms
        2026-10-07 12:31:22.313 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:22.313 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:22.314 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:22.315 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:22.317 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:22.324 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:22.324 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:22.325 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:22.325 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:22.325 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:22.325 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:22.325 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:22.326 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:22.326 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:22.327 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:22.328 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:22.336 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:22.337 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:22.338 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:22.338 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:22.338 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:22.338 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:22.471 11133-11599 log                     com.example.aquascope                E  Listening... (0.07)
        2026-10-07 12:31:22.556 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 37MB AllocSpace bytes, 308(24MB) LOS objects, 0% free, 124MB/124MB, paused 1.057ms,14.224ms total 162.112ms
        2026-10-07 12:31:22.818 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:22.818 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:22.818 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:22.819 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:22.820 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:22.827 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:22.827 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:22.828 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:22.828 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:22.829 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:22.829 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:22.829 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:22.829 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:22.830 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:22.830 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:22.832 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:22.839 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:22.839 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:22.839 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:22.839 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:22.840 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:22.840 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:22.870 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 30MB AllocSpace bytes, 247(19MB) LOS objects, 33% free, 96MB/144MB, paused 1.016ms,15.547ms total 313.816ms
        2026-10-07 12:31:22.960 11133-11599 log                     com.example.aquascope                E  Listening... (0.07)
        2026-10-07 12:31:22.960 11133-11599 thread                  com.example.aquascope                E  halting......
        2026-10-07 12:31:22.989 11133-11599 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/5_1791356468986,Alice-Sounding-0.txt false
        2026-10-07 12:31:23.021 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:23.038 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:23.040 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:23.041 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:23.052 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:23.054 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:23.055 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:23.059 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:23.066 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:23.078 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:23.089 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 46MB AllocSpace bytes, 385(30MB) LOS objects, 23% free, 111MB/144MB, paused 680us,16.683ms total 180.930ms
        2026-10-07 12:31:23.112 11133-11613 thread                  com.example.aquascope                E  recording thread end elegantly......
        2026-10-07 12:31:23.152 11133-11599 AudioTrack              com.example.aquascope                W  Use of stream types is deprecated for operations other than volume control
        2026-10-07 12:31:23.152 11133-11599 AudioTrack              com.example.aquascope                W  See the documentation of AudioTrack() for what to use instead with android.media.AudioAttributes to qualify your playback use case
        2026-10-07 12:31:23.158 11133-11599 log                     com.example.aquascope                E  appendtofile Alice-Sounding-log.txt false
        2026-10-07 12:31:23.159 11133-11599 AudioTrack              com.example.aquascope                D  setVolume(0.600000, 0.600000) pid : 11133
        2026-10-07 12:31:23.201 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 25MB AllocSpace bytes, 176(14MB) LOS objects, 40% free, 71MB/119MB, paused 1.124ms,2.648ms total 110.955ms
        2026-10-07 12:31:23.682 11133-11599 fifo                    com.example.aquascope                E  Alice-Feedback-0-1
        2026-10-07 12:31:23.683 11133-11599 recorder                com.example.aquascope                E  minbuffersize 7680
        2026-10-07 12:31:23.711 11133-11599 len                     com.example.aquascope                E  sig length 72000,Feedback
        2026-10-07 12:31:23.711 11133-11625 recorder                com.example.aquascope                E  start 1
        2026-10-07 12:31:24.270 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[0,75][1080,2340] display=[0,75][1080,2340] parentFrame=[0,0][0,0]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:31:25.460 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:25.461 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:25.462 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:25.462 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:25.465 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:25.472 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:25.472 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:25.472 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:25.472 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:25.473 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:25.473 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:25.473 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:25.473 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:25.474 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:25.474 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:25.476 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:25.483 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:25.483 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:25.483 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:25.483 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:25.484 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:25.484 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:25.497 11133-11599 log                     com.example.aquascope                E  Listening... (0.00)
        2026-10-07 12:31:25.937 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:25.937 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:25.938 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:25.938 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:25.940 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:25.948 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:25.948 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:25.949 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:25.949 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:25.949 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:25.949 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:25.949 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:25.949 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:25.950 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:25.950 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:25.952 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:25.959 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:25.960 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:25.960 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:25.960 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:25.960 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:25.960 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:26.102 11133-11599 log                     com.example.aquascope                E  Listening... (0.23)
        2026-10-07 12:31:26.327 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 43MB AllocSpace bytes, 325(24MB) LOS objects, 38% free, 75MB/123MB, paused 2.239ms,12.078ms total 240.720ms
        2026-10-07 12:31:26.503 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:26.503 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:26.503 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:26.503 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:26.505 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:26.514 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:26.514 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:26.514 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:26.514 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:26.515 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:26.515 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:26.515 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:26.515 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:26.515 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:26.516 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:26.517 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:26.524 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:26.524 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:26.525 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:26.525 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:26.525 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:26.525 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:26.692 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 33MB AllocSpace bytes, 276(22MB) LOS objects, 0% free, 159MB/159MB, paused 1.554ms,13.344ms total 115.800ms
        2026-10-07 12:31:26.719 11133-11599 log                     com.example.aquascope                E  Listening... (0.23)
        2026-10-07 12:31:26.765 11133-11133 InsetsController        com.example.aquascope                I  onStateChanged: host=com.example.aquascope/com.example.root.ffttest2.MainActivity, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {3 mType=ime mFrame=[0,0][0,0] mAttachedInsets=null mVisible=false mFlags=INVALID mSideHint=NONE mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=false mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:31:26.771 11133-11133 InsetsController        com.example.aquascope                I  onStateChanged: host=com.example.aquascope/com.example.root.ffttest2.MainActivity, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=false mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {3 mType=ime mFrame=[0,0][0,0] mAttachedInsets=null mVisible=false mFlags=INVALID mSideHint=NONE mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=false mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:31:26.772 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[0,75][1080,2340] display=[0,75][1080,2340] parentFrame=[0,0][0,0]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:31:26.886 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 66MB AllocSpace bytes, 550(41MB) LOS objects, 38% free, 76MB/124MB, paused 1.019ms,2.889ms total 194.241ms
        2026-10-07 12:31:26.961 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:26.961 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:26.961 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:26.961 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:26.963 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:26.970 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:26.970 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:26.970 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:26.970 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:26.971 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:26.971 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:26.971 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:26.971 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:26.972 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:26.972 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:26.974 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:26.980 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:26.980 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:26.981 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:26.981 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:26.982 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:26.982 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:27.182 11133-11599 log                     com.example.aquascope                E  Listening... (0.12)
        2026-10-07 12:31:27.186 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 28MB AllocSpace bytes, 236(19MB) LOS objects, 0% free, 193MB/193MB, paused 704us,12.234ms total 157.359ms
        2026-10-07 12:31:27.445 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:27.445 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:27.446 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:27.446 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:27.448 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:27.458 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:27.458 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:27.458 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:27.458 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:27.459 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:27.460 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:27.460 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:27.460 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:27.460 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:27.461 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:27.463 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:27.469 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:27.469 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:27.470 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:27.470 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:27.470 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:27.470 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:27.518 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 73MB AllocSpace bytes, 596(46MB) LOS objects, 29% free, 114MB/162MB, paused 903us,11.201ms total 330.733ms
        2026-10-07 12:31:27.539 11133-11599 log                     com.example.aquascope                E  Listening... (0.11)
        2026-10-07 12:31:27.932 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:27.933 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:27.933 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:27.933 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:27.935 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:27.942 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:27.942 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:27.943 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:27.943 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:27.943 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:27.944 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:27.944 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:27.944 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:27.944 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:27.945 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:27.946 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:27.954 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:27.954 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:27.954 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:27.954 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:27.955 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:27.955 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:28.155 11133-11599 log                     com.example.aquascope                E  Listening... (0.21)
        2026-10-07 12:31:28.184 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 52MB AllocSpace bytes, 442(38MB) LOS objects, 0% free, 208MB/208MB, paused 784us,15.547ms total 211.457ms
        2026-10-07 12:31:28.474 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:28.474 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:28.475 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:28.475 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:28.477 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:28.484 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:28.484 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:28.484 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:28.484 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:28.485 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:28.485 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:28.485 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:28.485 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:28.486 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:28.486 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:28.488 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:28.496 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:28.496 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:28.497 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:28.497 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:28.497 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:28.497 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:28.511 11133-11599 log                     com.example.aquascope                E  Listening... (0.00)
        2026-10-07 12:31:28.520 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 83MB AllocSpace bytes, 670(50MB) LOS objects, 37% free, 79MB/127MB, paused 682us,11.267ms total 334.669ms
        2026-10-07 12:31:28.985 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:28.985 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:28.986 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:28.986 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:28.988 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:28.995 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:28.995 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:28.996 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:28.996 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:28.997 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:28.997 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:28.997 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:28.997 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:28.998 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:28.998 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:29.000 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:29.007 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:29.007 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:29.008 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:29.008 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:29.008 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:29.008 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:29.073 11133-11599 log                     com.example.aquascope                E  Listening... (0.12)
        2026-10-07 12:31:29.073 11133-11599 thread                  com.example.aquascope                E  halting......
        2026-10-07 12:31:29.087 11133-11599 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/5_1791356468986,Alice-Sounding-0.txt false
        2026-10-07 12:31:29.131 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:29.154 11133-11625 thread                  com.example.aquascope                E  recording thread end elegantly......
        2026-10-07 12:31:29.173 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:29.175 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:29.176 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:29.178 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:29.180 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:29.182 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 33MB AllocSpace bytes, 291(26MB) LOS objects, 37% free, 79MB/127MB, paused 3.384ms,11.995ms total 123.280ms
        2026-10-07 12:31:29.183 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:29.187 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:29.189 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:29.221 11133-11599 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:31:29.237 11133-11599 AudioTrack              com.example.aquascope                W  Use of stream types is deprecated for operations other than volume control
        2026-10-07 12:31:29.238 11133-11599 AudioTrack              com.example.aquascope                W  See the documentation of AudioTrack() for what to use instead with android.media.AudioAttributes to qualify your playback use case
        2026-10-07 12:31:29.239 11133-11599 log                     com.example.aquascope                E  appendtofile Alice-Sounding-log.txt false
        2026-10-07 12:31:29.240 11133-11599 AudioTrack              com.example.aquascope                D  setVolume(0.600000, 0.600000) pid : 11133
        2026-10-07 12:31:29.759 11133-11599 fifo                    com.example.aquascope                E  Alice-Feedback-0-2
        2026-10-07 12:31:29.759 11133-11599 recorder                com.example.aquascope                E  minbuffersize 7680
        2026-10-07 12:31:29.784 11133-11599 len                     com.example.aquascope                E  sig length 72000,Feedback
        2026-10-07 12:31:29.784 11133-11641 recorder                com.example.aquascope                E  start 1
        2026-10-07 12:31:30.349 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[0,75][1080,2340] display=[0,75][1080,2340] parentFrame=[0,0][0,0]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:31:31.514 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:31.514 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:31.515 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:31.515 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:31.518 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:31.525 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:31.525 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:31.526 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:31.526 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:31.526 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:31.527 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:31.527 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:31.527 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:31.528 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:31.528 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:31.530 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:31.537 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:31.537 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:31.538 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:31.538 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:31.539 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:31.539 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:31.660 11133-11599 log                     com.example.aquascope                E  Listening... (0.10)
        2026-10-07 12:31:31.865 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 77MB AllocSpace bytes, 566(45MB) LOS objects, 39% free, 74MB/122MB, paused 695us,9.104ms total 309.039ms
        2026-10-07 12:31:31.974 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 336KB AllocSpace bytes, 0(0B) LOS objects, 37% free, 76MB/122MB, paused 597us,13.615ms total 108.458ms
        2026-10-07 12:31:31.997 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:31.997 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:31.998 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:31.998 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:32.000 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:32.007 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:32.007 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:32.007 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:32.007 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:32.008 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:32.008 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:32.008 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:32.008 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:32.009 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:32.010 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:32.011 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:32.018 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:32.018 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:32.019 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:32.019 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:32.020 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:32.020 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:32.086 11133-11599 log                     com.example.aquascope                E  Listening... (0.10)
        2026-10-07 12:31:32.366 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 33MB AllocSpace bytes, 286(24MB) LOS objects, 38% free, 75MB/123MB, paused 640us,14.345ms total 296.381ms
        2026-10-07 12:31:32.590 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:32.590 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:32.590 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:32.590 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:32.592 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:32.599 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:32.599 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:32.599 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:32.599 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:32.600 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:32.600 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:32.600 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:32.600 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:32.600 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:32.601 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:32.602 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:32.609 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:32.609 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:32.609 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:32.609 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:32.610 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:32.610 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:32.780 11133-11599 log                     com.example.aquascope                E  Listening... (0.13)
        2026-10-07 12:31:32.785 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 35MB AllocSpace bytes, 290(23MB) LOS objects, 0% free, 181MB/181MB, paused 555us,9.367ms total 129.217ms
        2026-10-07 12:31:32.846 11133-11133 InsetsController        com.example.aquascope                I  onStateChanged: host=com.example.aquascope/com.example.root.ffttest2.MainActivity, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {3 mType=ime mFrame=[0,0][0,0] mAttachedInsets=null mVisible=false mFlags=INVALID mSideHint=NONE mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=false mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:31:32.850 11133-11133 InsetsController        com.example.aquascope                I  onStateChanged: host=com.example.aquascope/com.example.root.ffttest2.MainActivity, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=false mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {3 mType=ime mFrame=[0,0][0,0] mAttachedInsets=null mVisible=false mFlags=INVALID mSideHint=NONE mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=false mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:31:32.851 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[0,75][1080,2340] display=[0,75][1080,2340] parentFrame=[0,0][0,0]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:31:33.068 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:33.068 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:33.069 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:33.069 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:33.071 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:33.077 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:33.077 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:33.078 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:33.078 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:33.078 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:33.078 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:33.078 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:33.078 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:33.079 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:33.079 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:33.081 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:33.088 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:33.088 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:33.088 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:33.088 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:33.089 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:33.089 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:33.102 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 65MB AllocSpace bytes, 542(42MB) LOS objects, 37% free, 81MB/129MB, paused 1.503ms,10.238ms total 316.061ms
        2026-10-07 12:31:33.267 11133-11599 log                     com.example.aquascope                E  Listening... (0.11)
        2026-10-07 12:31:33.285 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 39MB AllocSpace bytes, 321(25MB) LOS objects, 0% free, 175MB/175MB, paused 661us,11.725ms total 142.992ms
        2026-10-07 12:31:33.508 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:33.508 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:33.509 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:33.509 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:33.511 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:33.519 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:33.519 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:33.519 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:33.519 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:33.520 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:33.520 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:33.520 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:33.520 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:33.521 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:33.521 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:33.526 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:33.535 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:33.535 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:33.535 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:33.535 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:33.535 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:33.535 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:33.626 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 62MB AllocSpace bytes, 505(38MB) LOS objects, 25% free, 139MB/187MB, paused 1.055ms,13.413ms total 339.846ms
        2026-10-07 12:31:33.732 11133-11599 log                     com.example.aquascope                E  Listening... (0.10)
        2026-10-07 12:31:33.807 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 59MB AllocSpace bytes, 488(38MB) LOS objects, 24% free, 141MB/187MB, paused 672us,11.812ms total 164.632ms
        2026-10-07 12:31:34.006 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:34.006 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:34.007 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:34.007 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:34.009 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:34.018 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:34.018 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:34.019 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:34.019 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:34.020 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:34.020 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:34.020 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:34.020 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:34.021 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:34.021 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:34.024 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:34.031 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:34.031 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:34.031 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:34.031 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:34.032 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:34.032 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:34.069 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 41MB AllocSpace bytes, 338(26MB) LOS objects, 30% free, 107MB/155MB, paused 968us,12.552ms total 261.628ms
        2026-10-07 12:31:34.094 11133-11599 log                     com.example.aquascope                E  Listening... (0.10)
        2026-10-07 12:31:34.576 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:34.576 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:34.577 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:34.577 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:34.579 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:34.586 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:34.586 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:34.586 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:34.586 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:34.587 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:34.587 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:34.587 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:34.587 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:34.588 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:34.588 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:34.590 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:34.597 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:34.597 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:34.598 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:34.598 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:34.598 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:34.598 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:34.796 11133-11599 log                     com.example.aquascope                E  Listening... (0.10)
        2026-10-07 12:31:34.801 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 46MB AllocSpace bytes, 395(34MB) LOS objects, 0% free, 217MB/217MB, paused 833us,15.348ms total 189.433ms
        2026-10-07 12:31:35.033 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:35.033 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:35.034 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:35.034 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:35.036 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:35.042 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:35.043 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:35.044 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:35.044 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:35.044 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:35.045 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:35.045 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:31:35.045 11133-11599 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:31:35.045 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:31:35.046 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:31:35.047 11133-11599 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:31:35.054 11133-11599 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:31:35.054 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:31:35.055 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:31:35.055 11133-11599 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:31:35.055 11133-11599 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:31:35.055 11133-11599 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:31:35.152 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 89MB AllocSpace bytes, 717(54MB) LOS objects, 22% free, 161MB/209MB, paused 792us,13.173ms total 350.881ms
        2026-10-07 12:31:35.176 11133-11599 log                     com.example.aquascope                E  Listening... (0.05)
        2026-10-07 12:31:35.177 11133-11599 thread                  com.example.aquascope                E  halting......
        2026-10-07 12:31:35.292 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 66MB AllocSpace bytes, 548(42MB) LOS objects, 38% free, 77MB/125MB, paused 1.294ms,11.538ms total 135.100ms
        2026-10-07 12:31:35.331 11133-11641 thread                  com.example.aquascope                E  recording thread end elegantly......
        2026-10-07 12:31:38.178 11133-11133 log                     com.example.aquascope                E  Bob is the receiver; starting the acoustic listen loop
        2026-10-07 12:31:52.619 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:31:52.619 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=high hint, reason=touch, vri=VRI[MainActivity]@17e0af0
        2026-10-07 12:31:52.713 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:31:55.199 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:31:55.254 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:31:56.125 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:31:56.179 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:31:59.181 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=no preference, reason=boost timeout, vri=VRI[MainActivity]@17e0af0
        2026-10-07 12:31:59.193 11133-11133 log                     com.example.aquascope                D  onstop
        2026-10-07 12:31:59.193 11133-11133 log                     com.example.aquascope                E  start startmethod from startWrapper
        2026-10-07 12:31:59.194 11133-11133 log                     com.example.aquascope                E  startMethod: manual veto selected ACOUSTIC; sending on the acoustic channel only
        2026-10-07 12:31:59.198 11133-11656 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/5_1791356519195 Alice-AdaptParams-0.txt false
        2026-10-07 12:31:59.201 11133-11656 log                     com.example.aquascope                E  appendtofile Alice-Sounding-log.txt false
        2026-10-07 12:31:59.203 11133-11656 log                     com.example.aquascope                E  appendtofile Alice-Data-log.txt false
        2026-10-07 12:31:59.204 11133-11656 log                     com.example.aquascope                E  appendtofile Alice-Battery_Level-log.txt false
        2026-10-07 12:32:01.140 11133-11656 log                     com.example.aquascope                D  use codebook 4096 do nothing
        2026-10-07 12:32:01.140 11133-11656 log                     com.example.aquascope                E  Sender_Latency_Str: 2026-10-07 12:31:59.196
        OFDM_freq_adapt
        sender encode image (ms): 1874
        2026-10-07 12:32:01.140 11133-11656 log                     com.example.aquascope                E  send embedding: [168, 3276, 3765, 2485, 933, 2553, 2972, 2543, 1666, 1209, 1497, 554, 533, 4085, 4089, 3718, 702, 3483, 1958, 3027, 2371, 3751, 3302, 2371, 338, 222, 3914, 1195, 3957, 2044, 956, 821, 552, 2415, 880, 2953, 2082, 712, 385, 3082, 2140, 3844, 3635, 989, 1363, 2513, 3335, 2918, 3977, 256, 2866, 1576, 3522, 557, 2195, 2997, 371, 3375, 1809, 3275, 2021, 3361, 359, 2241]
        2026-10-07 12:32:01.141 11133-11656 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/5_1791356519195 Alice-Send_Embedding_Sequence-0.txt false
        2026-10-07 12:32:01.142 11133-11656 log                     com.example.aquascope                D  use codebook 4096 do nothing
        2026-10-07 12:32:04.624 11133-11656 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/5_1791356519195,Alice-Sounding-0.txt false
        2026-10-07 12:32:04.645 11133-11656 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:32:04.647 11133-11656 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:32:04.648 11133-11656 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:32:04.648 11133-11656 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:32:04.671 11133-11656 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:32:04.672 11133-11656 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:32:04.673 11133-11656 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:32:04.675 11133-11656 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:32:04.675 11133-11656 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:32:04.685 11133-11656 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:32:04.696 11133-11656 AudioTrack              com.example.aquascope                W  Use of stream types is deprecated for operations other than volume control
        2026-10-07 12:32:04.696 11133-11656 AudioTrack              com.example.aquascope                W  See the documentation of AudioTrack() for what to use instead with android.media.AudioAttributes to qualify your playback use case
        2026-10-07 12:32:04.696 11133-11656 log                     com.example.aquascope                E  appendtofile Alice-Sounding-log.txt false
        2026-10-07 12:32:04.697 11133-11656 AudioTrack              com.example.aquascope                D  setVolume(0.600000, 0.600000) pid : 11133
        2026-10-07 12:32:05.214 11133-11656 fifo                    com.example.aquascope                E  Alice-Feedback-0-0
        2026-10-07 12:32:05.215 11133-11656 recorder                com.example.aquascope                E  minbuffersize 7680
        2026-10-07 12:32:05.248 11133-11656 len                     com.example.aquascope                E  sig length 72000,Feedback
        2026-10-07 12:32:05.248 11133-11662 recorder                com.example.aquascope                E  start 1
        2026-10-07 12:32:05.815 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[0,75][1080,2340] display=[0,75][1080,2340] parentFrame=[0,0][0,0]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:32:07.002 11133-11656 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:32:07.002 11133-11656 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:32:07.003 11133-11656 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:32:07.003 11133-11656 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:32:07.005 11133-11656 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:32:07.010 11133-11656 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:32:07.010 11133-11656 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:32:07.010 11133-11656 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:32:07.010 11133-11656 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:32:07.011 11133-11656 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:32:07.011 11133-11656 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:32:07.011 11133-11656 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:32:07.011 11133-11656 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:32:07.011 11133-11656 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:32:07.011 11133-11656 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:32:07.013 11133-11656 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:32:07.017 11133-11656 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:32:07.017 11133-11656 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:32:07.018 11133-11656 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:32:07.018 11133-11656 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:32:07.018 11133-11656 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:32:07.018 11133-11656 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:32:07.061 11133-11656 log                     com.example.aquascope                E  Listening... (0.97)
        2026-10-07 12:32:07.061 11133-11656 copy                    com.example.aquascope                E  one more flag 27692.0,59612.0
        2026-10-07 12:32:07.064 11133-11656 copy                    com.example.aquascope                E  copy (27692.0,48065) to (20308)
        2026-10-07 12:32:07.157 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 37MB AllocSpace bytes, 286(23MB) LOS objects, 36% free, 74MB/116MB, paused 412us,8.567ms total 108.585ms
        2026-10-07 12:32:07.414 11133-11656 copy                    com.example.aquascope                E  another window from 20308,44308,72000
        2026-10-07 12:32:07.430 11133-11656 thread                  com.example.aquascope                E  halting......
        2026-10-07 12:32:07.488 11133-11656 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:32:07.488 11133-11656 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:32:07.489 11133-11656 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:32:07.489 11133-11656 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:32:07.489 11133-11656 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:32:07.489 11133-11656 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:32:07.489 11133-11656 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:32:07.490 11133-11656 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:32:07.490 11133-11656 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:32:07.490 11133-11656 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:32:07.490 11133-11656 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:32:07.490 11133-11656 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:32:07.490 11133-11656 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:32:07.490 11133-11656 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:32:07.490 11133-11656 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:32:07.490 11133-11656 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:32:07.491 11133-11656 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:32:07.491 11133-11656 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:32:07.491 11133-11656 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:32:07.491 11133-11656 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:32:07.491 11133-11656 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:32:07.491 11133-11656 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:32:07.493 11133-11656 extract                 com.example.aquascope                E  0,9359,72065
        2026-10-07 12:32:07.493 11133-11656 log                     com.example.aquascope                E  72065,10321,19920,9600
        2026-10-07 12:32:07.493 11133-11656 log                     com.example.aquascope                E  FeedbackSignal_parse_signal
        2026-10-07 12:32:07.502 11133-11656 log                     com.example.aquascope                E  acoustic feedback SNR: best=34.05461522340386 usable=5/5 -> MoE snr=100
        2026-10-07 12:32:07.502 11133-11656 log                     com.example.aquascope                E  MoE: acoustic ACK -> rawSnr=34.1 normalised=100 usableBins=5 threshold=13
        2026-10-07 12:32:07.502 11133-11656 log                     com.example.aquascope                E  feedback freqs 3300,3950
        2026-10-07 12:32:07.502 11133-11656 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/5_1791356519195,Alice-FeedbackFreqs-0.txt false
        2026-10-07 12:32:07.503 11133-11656 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/5_1791356519195,Alice-ExactFeedbackFreqs-0.txt false
        2026-10-07 12:32:07.504 11133-11656 log                     com.example.aquascope                E  Sender_Latency_Str: 2026-10-07 12:31:59.196
        OFDM_freq_adapt
        sender encode image (ms): 1874
        sender get valid bin (ms): 2807
        2026-10-07 12:32:07.504 11133-11656 log                     com.example.aquascope                E  appendtofile Alice-Data-log.txt false
        2026-10-07 12:32:07.505 11133-11656 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/5_1791356519195 Alice-ValidBins-0.txt false
        2026-10-07 12:32:07.507 11133-11656 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/5_1791356519195 Alice-TxBits-0.txt false
        2026-10-07 12:32:07.508 11133-11656 log                     com.example.aquascope                E  tx_bits_before_coding=>000010101000110011001100111010110101100110110101001110100101100111111001101110011100100111101111011010000010010010111001010111011001001000101010001000010101111111110101111111111001111010000110001010111110110110011011011110100110101111010011100101000011111010100111110011100110100101000011000101010010000011011110111101001010010010101011111101110101011111111100001110111100001100110101001000101000100101101111001101110000101110001001100000100010001011001000000110000001110000001010100001011100111100000100111000110011001111011101010101010011100111010001110100000111101101100110111110001001000100000000101100110010011000101000110111000010001000101101100010010011101110110101000101110011110100101111011100010001110011001011011111100101110100100001000101100111100011000001
        2026-10-07 12:32:07.508 11133-11656 log                     com.example.aquascope                E  tx_bits_after_coding =>000000001110001000101100110101111101011111010111110110010010000101001000010111110101000101001000101111011001001011111000010111110110101010011111010100011001111101100111111011110110100100011010010001010010110000001110111110111110000110011111100010000110010001011111101111101100111000100010110011101100001110001000011010101010101001001000011010101010101010011111011010010010110000110101110011100010000110101001000101000101111101010001010001101001001011110101001000011010010010111101100111111000101100001101101010010010001011110110101001111101100111110101001011111000101100001101011100111000100010111110110000001101010001101001000110100100101111100010111110111110001000100001101010100100011001001000100001101010101010100111000011011001000110100111000011010111110101001000101111101100111000101100111011111000010100011010011111010100011001110000111000011001110011101111010111000000111011001110110011100001011111101100000000110101110000000011011001110000000011100010001011000011100001100111110110100111000000111011110110011100110101111101011111011010010001100100100010001000100010111101100111110110010010110011011001001011000000110110100100010100010111110101000110101001110011101111101100111011000000000000111000010111110101111110111101011100111000101100110101000110011100001110110011101100111000010100010111001110111110111101100100011001000101001000101100111000011001111101101001001011111000011010010001100111001110110011011001111101011111100001010001101010100111111000011001001011111011000011101100111000010111110110100111001101011100000011101100000000000000
        2026-10-07 12:32:07.511 11133-11656 log                     com.example.aquascope                E  Sender_Latency_Str: 2026-10-07 12:31:59.196
        OFDM_freq_adapt
        sender encode image (ms): 1874
        sender get valid bin (ms): 2807
        sender encode signal (ms): 5
        2026-10-07 12:32:07.511 11133-11656 sym                     com.example.aquascope                E  1554,14,111,960,60
        2026-10-07 12:32:07.519 11133-11656 symbol                  com.example.aquascope                E  DataAdapt
        2026-10-07 12:32:07.519 11133-11656 symbol                  com.example.aquascope                E  # bits 1554
        2026-10-07 12:32:07.519 11133-11656 symbol                  com.example.aquascope                E  # carriers 14
        2026-10-07 12:32:07.519 11133-11656 symbol                  com.example.aquascope                E  # symbols 111
        2026-10-07 12:32:07.519 11133-11656 symbol                  com.example.aquascope                E  sym 0: 14,0
        2026-10-07 12:32:07.521 11133-11656 symbol                  com.example.aquascope                E  sym 1: 14,0
        2026-10-07 12:32:07.521 11133-11133 graph                   com.example.aquascope                E  48.282253750218885,149.22104468618483
        2026-10-07 12:32:07.522 11133-11656 symbol                  com.example.aquascope                E  sym 2: 14,0
        2026-10-07 12:32:07.523 11133-11656 symbol                  com.example.aquascope                E  sym 3: 14,0
        2026-10-07 12:32:07.524 11133-11656 symbol                  com.example.aquascope                E  sym 4: 14,0
        2026-10-07 12:32:07.526 11133-11656 symbol                  com.example.aquascope                E  sym 5: 14,0
        2026-10-07 12:32:07.527 11133-11656 symbol                  com.example.aquascope                E  sym 6: 14,0
        2026-10-07 12:32:07.528 11133-11656 symbol                  com.example.aquascope                E  sym 7: 14,0
        2026-10-07 12:32:07.529 11133-11656 symbol                  com.example.aquascope                E  sym 8: 14,0
        2026-10-07 12:32:07.531 11133-11656 symbol                  com.example.aquascope                E  sym 9: 14,0
        2026-10-07 12:32:07.532 11133-11656 symbol                  com.example.aquascope                E  sym 10: 14,0
        2026-10-07 12:32:07.533 11133-11656 symbol                  com.example.aquascope                E  sym 11: 14,0
        2026-10-07 12:32:07.535 11133-11656 symbol                  com.example.aquascope                E  sym 12: 14,0
        2026-10-07 12:32:07.536 11133-11656 symbol                  com.example.aquascope                E  sym 13: 14,0
        2026-10-07 12:32:07.538 11133-11656 symbol                  com.example.aquascope                E  sym 14: 14,0
        2026-10-07 12:32:07.539 11133-11656 symbol                  com.example.aquascope                E  sym 15: 14,0
        2026-10-07 12:32:07.541 11133-11656 symbol                  com.example.aquascope                E  sym 16: 14,0
        2026-10-07 12:32:07.542 11133-11656 symbol                  com.example.aquascope                E  sym 17: 14,0
        2026-10-07 12:32:07.543 11133-11656 symbol                  com.example.aquascope                E  sym 18: 14,0
        2026-10-07 12:32:07.545 11133-11656 symbol                  com.example.aquascope                E  sym 19: 14,0
        2026-10-07 12:32:07.546 11133-11656 symbol                  com.example.aquascope                E  sym 20: 14,0
        2026-10-07 12:32:07.547 11133-11656 symbol                  com.example.aquascope                E  sym 21: 14,0
        2026-10-07 12:32:07.548 11133-11656 symbol                  com.example.aquascope                E  sym 22: 14,0
        2026-10-07 12:32:07.550 11133-11656 symbol                  com.example.aquascope                E  sym 23: 14,0
        2026-10-07 12:32:07.551 11133-11656 symbol                  com.example.aquascope                E  sym 24: 14,0
        2026-10-07 12:32:07.553 11133-11656 symbol                  com.example.aquascope                E  sym 25: 14,0
        2026-10-07 12:32:07.554 11133-11656 symbol                  com.example.aquascope                E  sym 26: 14,0
        2026-10-07 12:32:07.556 11133-11656 symbol                  com.example.aquascope                E  sym 27: 14,0
        2026-10-07 12:32:07.557 11133-11656 symbol                  com.example.aquascope                E  sym 28: 14,0
        2026-10-07 12:32:07.558 11133-11656 symbol                  com.example.aquascope                E  sym 29: 14,0
        2026-10-07 12:32:07.560 11133-11656 symbol                  com.example.aquascope                E  sym 30: 14,0
        2026-10-07 12:32:07.561 11133-11656 symbol                  com.example.aquascope                E  sym 31: 14,0
        2026-10-07 12:32:07.563 11133-11656 symbol                  com.example.aquascope                E  sym 32: 14,0
        2026-10-07 12:32:07.564 11133-11656 symbol                  com.example.aquascope                E  sym 33: 14,0
        2026-10-07 12:32:07.567 11133-11133 graph                   com.example.aquascope                E  61.21729716458175,158.5358931527657
        2026-10-07 12:32:07.567 11133-11656 symbol                  com.example.aquascope                E  sym 34: 14,0
        2026-10-07 12:32:07.574 11133-11656 symbol                  com.example.aquascope                E  sym 35: 14,0
        2026-10-07 12:32:07.579 11133-11656 symbol                  com.example.aquascope                E  sym 36: 14,0
        2026-10-07 12:32:07.580 11133-11656 symbol                  com.example.aquascope                E  sym 37: 14,0
        2026-10-07 12:32:07.582 11133-11656 symbol                  com.example.aquascope                E  sym 38: 14,0
        2026-10-07 12:32:07.582 11133-11662 thread                  com.example.aquascope                E  recording thread end elegantly......
        2026-10-07 12:32:07.583 11133-11656 symbol                  com.example.aquascope                E  sym 39: 14,0
        2026-10-07 12:32:07.584 11133-11656 symbol                  com.example.aquascope                E  sym 40: 14,0
        2026-10-07 12:32:07.586 11133-11656 symbol                  com.example.aquascope                E  sym 41: 14,0
        2026-10-07 12:32:07.587 11133-11656 symbol                  com.example.aquascope                E  sym 42: 14,0
        2026-10-07 12:32:07.589 11133-11656 symbol                  com.example.aquascope                E  sym 43: 14,0
        2026-10-07 12:32:07.590 11133-11656 symbol                  com.example.aquascope                E  sym 44: 14,0
        2026-10-07 12:32:07.592 11133-11656 symbol                  com.example.aquascope                E  sym 45: 14,0
        2026-10-07 12:32:07.593 11133-11656 symbol                  com.example.aquascope                E  sym 46: 14,0
        2026-10-07 12:32:07.594 11133-11656 symbol                  com.example.aquascope                E  sym 47: 14,0
        2026-10-07 12:32:07.596 11133-11656 symbol                  com.example.aquascope                E  sym 48: 14,0
        2026-10-07 12:32:07.597 11133-11656 symbol                  com.example.aquascope                E  sym 49: 14,0
        2026-10-07 12:32:07.598 11133-11656 symbol                  com.example.aquascope                E  sym 50: 14,0
        2026-10-07 12:32:07.598 11133-11656 symbol                  com.example.aquascope                E  sym 51: 14,0
        2026-10-07 12:32:07.599 11133-11656 symbol                  com.example.aquascope                E  sym 52: 14,0
        2026-10-07 12:32:07.600 11133-11656 symbol                  com.example.aquascope                E  sym 53: 14,0
        2026-10-07 12:32:07.601 11133-11656 symbol                  com.example.aquascope                E  sym 54: 14,0
        2026-10-07 12:32:07.602 11133-11656 symbol                  com.example.aquascope                E  sym 55: 14,0
        2026-10-07 12:32:07.603 11133-11656 symbol                  com.example.aquascope                E  sym 56: 14,0
        2026-10-07 12:32:07.604 11133-11656 symbol                  com.example.aquascope                E  sym 57: 14,0
        2026-10-07 12:32:07.605 11133-11656 symbol                  com.example.aquascope                E  sym 58: 14,0
        2026-10-07 12:32:07.606 11133-11656 symbol                  com.example.aquascope                E  sym 59: 14,0
        2026-10-07 12:32:07.607 11133-11656 symbol                  com.example.aquascope                E  sym 60: 14,0
        2026-10-07 12:32:07.608 11133-11656 symbol                  com.example.aquascope                E  sym 61: 14,0
        2026-10-07 12:32:07.609 11133-11656 symbol                  com.example.aquascope                E  sym 62: 14,0
        2026-10-07 12:32:07.610 11133-11656 symbol                  com.example.aquascope                E  sym 63: 14,0
        2026-10-07 12:32:07.610 11133-11656 symbol                  com.example.aquascope                E  sym 64: 14,0
        2026-10-07 12:32:07.611 11133-11656 symbol                  com.example.aquascope                E  sym 65: 14,0
        2026-10-07 12:32:07.612 11133-11656 symbol                  com.example.aquascope                E  sym 66: 14,0
        2026-10-07 12:32:07.613 11133-11656 symbol                  com.example.aquascope                E  sym 67: 14,0
        2026-10-07 12:32:07.614 11133-11656 symbol                  com.example.aquascope                E  sym 68: 14,0
        2026-10-07 12:32:07.619 11133-11656 symbol                  com.example.aquascope                E  sym 69: 14,0
        2026-10-07 12:32:07.621 11133-11656 symbol                  com.example.aquascope                E  sym 70: 14,0
        2026-10-07 12:32:07.624 11133-11656 symbol                  com.example.aquascope                E  sym 71: 14,0
        2026-10-07 12:32:07.627 11133-11656 symbol                  com.example.aquascope                E  sym 72: 14,0
        2026-10-07 12:32:07.630 11133-11656 symbol                  com.example.aquascope                E  sym 73: 14,0
        2026-10-07 12:32:07.633 11133-11656 symbol                  com.example.aquascope                E  sym 74: 14,0
        2026-10-07 12:32:07.635 11133-11656 symbol                  com.example.aquascope                E  sym 75: 14,0
        2026-10-07 12:32:07.636 11133-11656 symbol                  com.example.aquascope                E  sym 76: 14,0
        2026-10-07 12:32:07.637 11133-11656 symbol                  com.example.aquascope                E  sym 77: 14,0
        2026-10-07 12:32:07.639 11133-11656 symbol                  com.example.aquascope                E  sym 78: 14,0
        2026-10-07 12:32:07.639 11133-11656 symbol                  com.example.aquascope                E  sym 79: 14,0
        2026-10-07 12:32:07.640 11133-11656 symbol                  com.example.aquascope                E  sym 80: 14,0
        2026-10-07 12:32:07.641 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 7333KB AllocSpace bytes, 27(4404KB) LOS objects, 39% free, 74MB/122MB, paused 495us,1.856ms total 147.883ms
        2026-10-07 12:32:07.641 11133-11656 symbol                  com.example.aquascope                E  sym 81: 14,0
        2026-10-07 12:32:07.642 11133-11656 symbol                  com.example.aquascope                E  sym 82: 14,0
        2026-10-07 12:32:07.642 11133-11656 symbol                  com.example.aquascope                E  sym 83: 14,0
        2026-10-07 12:32:07.643 11133-11656 symbol                  com.example.aquascope                E  sym 84: 14,0
        2026-10-07 12:32:07.643 11133-11656 symbol                  com.example.aquascope                E  sym 85: 14,0
        2026-10-07 12:32:07.644 11133-11656 symbol                  com.example.aquascope                E  sym 86: 14,0
        2026-10-07 12:32:07.644 11133-11656 symbol                  com.example.aquascope                E  sym 87: 14,0
        2026-10-07 12:32:07.645 11133-11656 symbol                  com.example.aquascope                E  sym 88: 14,0
        2026-10-07 12:32:07.646 11133-11656 symbol                  com.example.aquascope                E  sym 89: 14,0
        2026-10-07 12:32:07.646 11133-11656 symbol                  com.example.aquascope                E  sym 90: 14,0
        2026-10-07 12:32:07.647 11133-11656 symbol                  com.example.aquascope                E  sym 91: 14,0
        2026-10-07 12:32:07.647 11133-11656 symbol                  com.example.aquascope                E  sym 92: 14,0
        2026-10-07 12:32:07.648 11133-11656 symbol                  com.example.aquascope                E  sym 93: 14,0
        2026-10-07 12:32:07.648 11133-11656 symbol                  com.example.aquascope                E  sym 94: 14,0
        2026-10-07 12:32:07.649 11133-11656 symbol                  com.example.aquascope                E  sym 95: 14,0
        2026-10-07 12:32:07.649 11133-11656 symbol                  com.example.aquascope                E  sym 96: 14,0
        2026-10-07 12:32:07.650 11133-11656 symbol                  com.example.aquascope                E  sym 97: 14,0
        2026-10-07 12:32:07.650 11133-11656 symbol                  com.example.aquascope                E  sym 98: 14,0
        2026-10-07 12:32:07.651 11133-11656 symbol                  com.example.aquascope                E  sym 99: 14,0
        2026-10-07 12:32:07.652 11133-11656 symbol                  com.example.aquascope                E  sym 100: 14,0
        2026-10-07 12:32:07.652 11133-11656 symbol                  com.example.aquascope                E  sym 101: 14,0
        2026-10-07 12:32:07.653 11133-11656 symbol                  com.example.aquascope                E  sym 102: 14,0
        2026-10-07 12:32:07.653 11133-11656 symbol                  com.example.aquascope                E  sym 103: 14,0
        2026-10-07 12:32:07.654 11133-11656 symbol                  com.example.aquascope                E  sym 104: 14,0
        2026-10-07 12:32:07.654 11133-11656 symbol                  com.example.aquascope                E  sym 105: 14,0
        2026-10-07 12:32:07.655 11133-11656 symbol                  com.example.aquascope                E  sym 106: 14,0
        2026-10-07 12:32:07.655 11133-11656 symbol                  com.example.aquascope                E  sym 107: 14,0
        2026-10-07 12:32:07.656 11133-11656 symbol                  com.example.aquascope                E  sym 108: 14,0
        2026-10-07 12:32:07.656 11133-11656 symbol                  com.example.aquascope                E  sym 109: 14,0
        2026-10-07 12:32:07.657 11133-11656 symbol                  com.example.aquascope                E  sym 110: 14,0
        2026-10-07 12:32:07.658 11133-11656 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/5_1791356519195 Alice-Bit_Fill_Adapt-0.txt false
        2026-10-07 12:32:07.658 11133-11656 log                     com.example.aquascope                E  Sender_Latency_Str: 2026-10-07 12:31:59.196
        OFDM_freq_adapt
        sender encode image (ms): 1874
        sender get valid bin (ms): 2807
        sender encode signal (ms): 5
        sender generate signal (ms): 147
        2026-10-07 12:32:07.658 11133-11656 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/5_1791356519195,Alice-DataAdapt-0.txt false
        2026-10-07 12:32:07.735 11133-11656 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:32:07.762 11133-11656 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:32:07.763 11133-11656 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:32:07.765 11133-11656 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:32:07.767 11133-11656 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:32:07.771 11133-11656 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:32:07.776 11133-11656 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:32:07.780 11133-11656 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:32:07.784 11133-11656 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:32:07.813 11133-11656 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:32:07.837 11133-11656 AudioTrack              com.example.aquascope                W  Use of stream types is deprecated for operations other than volume control
        2026-10-07 12:32:07.837 11133-11656 AudioTrack              com.example.aquascope                W  See the documentation of AudioTrack() for what to use instead with android.media.AudioAttributes to qualify your playback use case
        2026-10-07 12:32:07.841 11133-11656 AudioTrack              com.example.aquascope                D  setVolume(0.600000, 0.600000) pid : 11133
        2026-10-07 12:32:08.325 11133-11133 InsetsController        com.example.aquascope                I  onStateChanged: host=com.example.aquascope/com.example.root.ffttest2.MainActivity, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {3 mType=ime mFrame=[0,0][0,0] mAttachedInsets=null mVisible=false mFlags=INVALID mSideHint=NONE mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=false mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:32:08.327 11133-11133 InsetsController        com.example.aquascope                I  onStateChanged: host=com.example.aquascope/com.example.root.ffttest2.MainActivity, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=false mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {3 mType=ime mFrame=[0,0][0,0] mAttachedInsets=null mVisible=false mFlags=INVALID mSideHint=NONE mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=false mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:32:08.327 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[0,75][1080,2340] display=[0,75][1080,2340] parentFrame=[0,0][0,0]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:32:10.613 11133-11656 asdf                    com.example.aquascope                E  writing sensors 139,1386,Alice-Sender_Sensor-0.txt
        2026-10-07 12:32:10.616 11133-11656 asdf                    com.example.aquascope                E  finish writing sensors 3
        2026-10-07 12:32:10.616 11133-11656 log                     com.example.aquascope                D  Sender_Latency_Str: 2026-10-07 12:31:59.196
        OFDM_freq_adapt
        sender encode image (ms): 1874
        sender get valid bin (ms): 2807
        sender encode signal (ms): 5
        sender generate signal (ms): 147
        sender send signal (ms): 2772
        2026-10-07 12:32:10.616 11133-11656 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/5_1791356519195 Alice-Latency_Sender-0.txt false
        2026-10-07 12:32:13.618 11133-11133 log                     com.example.aquascope                E  Bob is the receiver; starting the acoustic listen loop
        2026-10-07 12:32:52.873 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:32:52.876 11133-11133 GestureDetector         com.example.aquascope                I  obtain mCurrentDownEvent. id: 648448643 caller: android.view.ScaleGestureDetector.onTouchEvent:311 com.jjoe64.graphview.Viewport.onTouchEvent:655 com.jjoe64.graphview.GraphView.onTouchEvent:448
        2026-10-07 12:32:52.877 11133-11133 GestureDetector         com.example.aquascope                I  obtain mCurrentDownEvent. id: 648448643 caller: com.jjoe64.graphview.Viewport.onTouchEvent:656 com.jjoe64.graphview.GraphView.onTouchEvent:448 android.view.View.performOnTouchCallback:17597
        2026-10-07 12:32:52.877 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=high hint, reason=touch, vri=VRI[MainActivity]@17e0af0
        2026-10-07 12:32:52.903 11133-11133 GestureDetector         com.example.aquascope                I  obtain mCurrentMotionEventRaw. action: 2 id: 435435461
        2026-10-07 12:32:52.903 11133-11133 GestureDetector         com.example.aquascope                I  obtain mCurrentMotionEventRaw. action: 2 id: 435435461
        2026-10-07 12:32:52.994 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:32:55.995 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=no preference, reason=boost timeout, vri=VRI[MainActivity]@17e0af0
        2026-10-07 12:33:01.522 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:33:01.523 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=high hint, reason=touch, vri=VRI[MainActivity]@17e0af0
        2026-10-07 12:33:01.594 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:33:01.624 11133-11133 WindowManager           com.example.aquascope                I  WindowManagerGlobal#addView, ty=1002, view=android.widget.PopupWindow$PopupDecorView{b2915ad V.E...... R.....I. 0,0-0,0}, caller=android.view.WindowManagerImpl.addView:167 android.widget.PopupWindow.invokePopup:1686 android.widget.PopupWindow.showAsDropDown:1493
        2026-10-07 12:33:01.625 11133-11133 ViewRootImpl            com.example.aquascope                I  dVRR is disabled
        2026-10-07 12:33:01.627 11133-11220 NativeCust...ncyManager com.example.aquascope                D  [NativeCFMS] BpCustomFrequencyManager::BpCustomFrequencyManager()
        2026-10-07 12:33:01.635 11133-11133 InputTransport          com.example.aquascope                D  Input channel constructed: '936da52', fd=176
        2026-10-07 12:33:01.636 11133-11133 InsetsController        com.example.aquascope                I  onStateChanged: host=PopupWindow:c5ba8e3, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=false mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=false mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:33:01.636 11133-11133 VRI[PopupW...3]@5e720e2 com.example.aquascope                I  synced displayState. AttachInfo displayState=2
        2026-10-07 12:33:01.637 11133-11133 VRI[PopupW...3]@5e720e2 com.example.aquascope                I  setView = android.widget.PopupWindow$PopupDecorView@b2915ad IsHRR=false mFlingFrameRateChange=0 TM=true
        2026-10-07 12:33:01.638 11133-11133 WindowOnBackDispatcher  com.example.aquascope                D  setTopOnBackInvokedCallback (unwrapped): android.view.ViewRootImpl$$ExternalSyntheticLambda16@55a692e
        2026-10-07 12:33:01.644 11133-11133 BufferQueueConsumer     com.example.aquascope                D  [](id:2b7d00000007,api:0,p:-1,c:11133) connect: controlledByApp=false
        2026-10-07 12:33:01.645 11133-11133 BLASTBufferQueue        com.example.aquascope                I  [VRI[PopupWindow:c5ba8e3]@5e720e2#3](f:0,a:0,s:0) constructor()
        2026-10-07 12:33:01.645 11133-11133 BLASTBufferQueue_Java   com.example.aquascope                I  new BLASTBufferQueue, mName= VRI[PopupWindow:c5ba8e3]@5e720e2 mNativeObject= 0xb4000078aaf27d90 caller= android.view.ViewRootImpl.updateBlastSurfaceIfNeeded:3603 android.view.ViewRootImpl.relayoutWindow:11813 android.view.ViewRootImpl.performTraversals:4840 android.view.ViewRootImpl.doTraversal:3959 android.view.ViewRootImpl$TraversalRunnable.run:13034 android.view.Choreographer$CallbackRecord.run:1961 android.view.Choreographer$CallbackRecord.run:1970 android.view.Choreographer.doCallbacks:1423 android.view.Choreographer.doFrame:1348 android.view.Choreographer$FrameDisplayEventReceiver.run:1930
        2026-10-07 12:33:01.645 11133-11133 BLASTBufferQueue_Java   com.example.aquascope                I  update, w= 709 h= 945 mName = VRI[PopupWindow:c5ba8e3]@5e720e2 mNativeObject= 0xb4000078aaf27d90 sc.mNativeObject= 0xb4000077ea0dd550 format= -3 caller= android.view.ViewRootImpl.updateBlastSurfaceIfNeeded:3608 android.view.ViewRootImpl.relayoutWindow:11813 android.view.ViewRootImpl.performTraversals:4840 android.view.ViewRootImpl.doTraversal:3959 android.view.ViewRootImpl$TraversalRunnable.run:13034 android.view.Choreographer$CallbackRecord.run:1961
        2026-10-07 12:33:01.646 11133-11133 BLASTBufferQueue        com.example.aquascope                I  [VRI[PopupWindow:c5ba8e3]@5e720e2#3](f:0,a:0,s:0) update width=709 height=945 format=-3 mTransformHint=0
        2026-10-07 12:33:01.646 11133-11133 VRI[PopupW...3]@5e720e2 com.example.aquascope                I  Relayout returned: old=(506,1333,945,2008) new=(506,1333,945,2008) relayoutAsync=false req=(439,675)0 dur=6 res=0x3 s={true 0xb40000787a14a900} ch=true seqId=0
        2026-10-07 12:33:01.647 11133-11133 VRI[PopupW...3]@5e720e2 com.example.aquascope                I  performConfigurationChange setNightDimText nightDimLevel=0
        2026-10-07 12:33:01.648 11133-11133 VRI[PopupW...3]@5e720e2 com.example.aquascope                D  mThreadedRenderer.initialize() mSurface={isValid=true 0xb40000787a14a900} hwInitialized=true
        2026-10-07 12:33:01.660 11133-11133 AbsListView             com.example.aquascope                D   in onLayout changed
        2026-10-07 12:33:01.660 11133-11133 VRI[PopupW...3]@5e720e2 com.example.aquascope                D  reportNextDraw android.view.ViewRootImpl.performTraversals:5507 android.view.ViewRootImpl.doTraversal:3959 android.view.ViewRootImpl$TraversalRunnable.run:13034 android.view.Choreographer$CallbackRecord.run:1961 android.view.Choreographer$CallbackRecord.run:1970
        2026-10-07 12:33:01.661 11133-11133 VRI[PopupW...3]@5e720e2 com.example.aquascope                D  Setup new sync=wmsSync-VRI[PopupWindow:c5ba8e3]@5e720e2#12
        2026-10-07 12:33:01.661 11133-11133 VRI[PopupW...3]@5e720e2 com.example.aquascope                I  Creating new active sync group VRI[PopupWindow:c5ba8e3]@5e720e2#13
        2026-10-07 12:33:01.662 11133-11133 VRI[PopupW...3]@5e720e2 com.example.aquascope                D  registerCallbacksForSync syncBuffer=false
        2026-10-07 12:33:01.663 11133-11293 VRI[PopupW...3]@5e720e2 com.example.aquascope                D  Received frameDrawingCallback syncResult=0 frameNum=1.
        2026-10-07 12:33:01.663 11133-11293 VRI[PopupW...3]@5e720e2 com.example.aquascope                I  mWNT: t=0xb4000076fa1a5d00 mBlastBufferQueue=0xb4000078aaf27d90 fn= 1 HdrRenderState mRenderHdrSdrRatio=1.0 caller= android.view.ViewRootImpl$12.onFrameDraw:15561 android.view.ThreadedRenderer$1.onFrameDraw:751 <bottom of call stack>
        2026-10-07 12:33:01.664 11133-11293 VRI[PopupW...3]@5e720e2 com.example.aquascope                I  Setting up sync and frameCommitCallback
        2026-10-07 12:33:01.665 11133-11220 BLASTBufferQueue        com.example.aquascope                I  [VRI[PopupWindow:c5ba8e3]@5e720e2#3](f:0,a:0,s:0) onFrameAvailable the first frame is available
        2026-10-07 12:33:01.666 11133-11220 BLASTBufferQueue        com.example.aquascope                I  [VRI[PopupWindow:c5ba8e3]@5e720e2#3](f:0,a:1,s:0) acquireNextBufferLocked size=709x945 mFrameNumber=1 applyTransaction=true mTimestamp=222455697510027(auto) mPendingTransactions.size=0 graphicBufferId=47815870906410 transform=0
        2026-10-07 12:33:01.666 11133-11220 VRI[PopupW...3]@5e720e2 com.example.aquascope                I  Received frameCommittedCallback lastAttemptedDrawFrameNum=1 didProduceBuffer=true
        2026-10-07 12:33:01.667 11133-11220 HWUI                    com.example.aquascope                D  CFMS:: SetUp Pid : 11133    Tid : 11220
        2026-10-07 12:33:01.667 11133-11133 VRI[PopupW...3]@5e720e2 com.example.aquascope                D  reportDrawFinished seqId=0
        2026-10-07 12:33:01.668 11133-11220 HWUI                    com.example.aquascope                D  HWUI - treat SMPTE_170M as sRGB
        2026-10-07 12:33:01.668 11133-11133 VRI[PopupW...3]@5e720e2 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[506,1333][945,2008] display=[-100000,-100000][100000,100000] parentFrame=[0,0][0,0] attachedFrame=[0,75][1080,2340]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:33:01.668 11133-11133 VRI[PopupW...3]@5e720e2 com.example.aquascope                I  handleResized mSyncSeqId = 0
        2026-10-07 12:33:01.668 11133-11133 VRI[PopupW...3]@5e720e2 com.example.aquascope                D  reportNextDraw android.view.ViewRootImpl.handleResized:2995 android.view.ViewRootImpl.-$$Nest$mhandleResized:0 android.view.ViewRootImpl$W.resized:14200 android.app.servertransaction.WindowStateResizeItem.execute:81 android.app.servertransaction.WindowStateTransactionItem.execute:62
        2026-10-07 12:33:01.669 11133-11133 VRI[PopupW...3]@5e720e2 com.example.aquascope                D  Setup new sync=wmsSync-VRI[PopupWindow:c5ba8e3]@5e720e2#14
        2026-10-07 12:33:01.669 11133-11133 VRI[PopupW...3]@5e720e2 com.example.aquascope                I  Creating new active sync group VRI[PopupWindow:c5ba8e3]@5e720e2#15
        2026-10-07 12:33:01.669 11133-11133 VRI[PopupW...3]@5e720e2 com.example.aquascope                D  registerCallbacksForSync syncBuffer=false
        2026-10-07 12:33:01.670 11133-11294 VRI[PopupW...3]@5e720e2 com.example.aquascope                D  Received frameDrawingCallback syncResult=0 frameNum=2.
        2026-10-07 12:33:01.670 11133-11294 VRI[PopupW...3]@5e720e2 com.example.aquascope                I  Setting up sync and frameCommitCallback
        2026-10-07 12:33:01.670 11133-11220 VRI[PopupW...3]@5e720e2 com.example.aquascope                I  Received frameCommittedCallback lastAttemptedDrawFrameNum=2 didProduceBuffer=false
        2026-10-07 12:33:01.670 11133-11220 BLASTBufferQueue_Java   com.example.aquascope                I  gatherPendingTransactions, mName= VRI[PopupWindow:c5ba8e3]@5e720e2 mNativeObject= 0xb4000078aaf27d90 frameNumber= 2 caller= android.view.ViewRootImpl$12.lambda$onFrameDraw$3:15645 android.view.ViewRootImpl$12.$r8$lambda$CTo3ExVBk8akdVTGlqHPAoYLRVI:0 android.view.ViewRootImpl$12$$ExternalSyntheticLambda1.onFrameCommit:0 android.view.ThreadedRenderer$1.lambda$onFrameDraw$0:763 android.view.ThreadedRenderer$1$$ExternalSyntheticLambda0.onFrameCommit:0 <bottom of call stack>
        2026-10-07 12:33:01.670 11133-11133 VRI[PopupW...3]@5e720e2 com.example.aquascope                D  reportDrawFinished seqId=0
        2026-10-07 12:33:01.689 11133-11133 ImeFocusController      com.example.aquascope                I  onPreWindowFocus: skipped hasWindowFocus=false mHasImeFocus=true
        2026-10-07 12:33:01.690 11133-11133 ImeFocusController      com.example.aquascope                I  onPostWindowFocus: skipped hasWindowFocus=false mHasImeFocus=true
        2026-10-07 12:33:01.710 11133-11133 VRI[PopupW...3]@5e720e2 com.example.aquascope                D  mThreadedRenderer.initializeIfNeeded()#2 mSurface={isValid=true 0xb40000787a14a900}
        2026-10-07 12:33:01.710 11133-11133 ImeFocusController      com.example.aquascope                I  onPreWindowFocus: skipped hasWindowFocus=true mHasImeFocus=false
        2026-10-07 12:33:01.710 11133-11133 ImeFocusController      com.example.aquascope                I  onPostWindowFocus: skipped hasWindowFocus=true mHasImeFocus=false
        2026-10-07 12:33:02.280 11133-11133 VRI[PopupW...3]@5e720e2 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:33:02.281 11133-11133 VRI[PopupW...3]@5e720e2 com.example.aquascope                I  call setFrameRateCategory for touch hint category=high hint, reason=touch, vri=VRI[PopupWindow:c5ba8e3]@5e720e2
        2026-10-07 12:33:02.369 11133-11133 VRI[PopupW...3]@5e720e2 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:33:02.369 11133-11133 AbsListView             com.example.aquascope                D  onTouchUp() mTouchMode : 0
        2026-10-07 12:33:02.437 11133-11133 BLASTBufferQueue_Java   com.example.aquascope                I  update, w= 709 h= 945 mName = VRI[PopupWindow:c5ba8e3]@5e720e2 mNativeObject= 0xb4000078aaf27d90 sc.mNativeObject= 0xb4000077ea0dd550 format= -3 caller= android.view.ViewRootImpl.updateBlastSurfaceIfNeeded:3592 android.view.ViewRootImpl.relayoutWindow:11813 android.view.ViewRootImpl.performTraversals:4840 android.view.ViewRootImpl.doTraversal:3959 android.view.ViewRootImpl$TraversalRunnable.run:13034 android.view.Choreographer$CallbackRecord.run:1961
        2026-10-07 12:33:02.437 11133-11133 VRI[PopupW...3]@5e720e2 com.example.aquascope                I  Relayout returned: old=(506,1333,945,2008) new=(506,1333,945,2008) relayoutAsync=true req=(439,675)0 dur=0 res=0x0 s={true 0xb40000787a14a900} ch=false seqId=0
        2026-10-07 12:33:02.437 11133-11133 VRI[PopupW...3]@5e720e2 com.example.aquascope                I  registerCallbackForPendingTransactions
        2026-10-07 12:33:02.438 11133-11293 VRI[PopupW...3]@5e720e2 com.example.aquascope                I  mWNT: t=0xb4000076fa17eef0 mBlastBufferQueue=0xb4000078aaf27d90 fn= 21 HdrRenderState mRenderHdrSdrRatio=1.0 caller= android.view.ViewRootImpl$10.onFrameDraw:6615 android.view.ViewRootImpl$4.onFrameDraw:2485 android.view.ThreadedRenderer$1.onFrameDraw:751
        2026-10-07 12:33:02.461 11133-11133 log                     com.example.aquascope                D  onstop
        2026-10-07 12:33:02.761 11133-11133 log                     com.example.aquascope                E  Bob is the receiver; starting the acoustic listen loop
        2026-10-07 12:33:02.764 11133-11994 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/5_1791356519195 Bob-AdaptParams-0.txt false
        2026-10-07 12:33:02.766 11133-11994 log                     com.example.aquascope                E  appendtofile Bob-Feedback-log.txt false
        2026-10-07 12:33:02.767 11133-11994 log                     com.example.aquascope                D  Bob-DataRx-0-0
        2026-10-07 12:33:02.767 11133-11994 recorder                com.example.aquascope                E  minbuffersize 7680
        2026-10-07 12:33:02.792 11133-11998 recorder                com.example.aquascope                E  start 1
        2026-10-07 12:33:02.800 11133-11994 log                     com.example.aquascope                E  sig length 960000,DataRx
        2026-10-07 12:33:03.069 11133-11133 Choreographer           com.example.aquascope                I  Skipped 36 frames!  The application may be doing too much work on its main thread.
        2026-10-07 12:33:03.071 11133-11133 WindowManager           com.example.aquascope                I  WindowManagerGlobal#removeView, ty=1002, view=android.widget.PopupWindow$PopupDecorView{b2915ad V.E...... R.....ID 0,0-439,675}, caller=android.view.WindowManagerGlobal.removeView:654 android.view.WindowManagerImpl.removeViewImmediate:254 android.widget.PopupWindow.dismissImmediate:2380
        2026-10-07 12:33:03.071 11133-11133 WindowOnBackDispatcher  com.example.aquascope                D  setTopOnBackInvokedCallback (unwrapped): null
        2026-10-07 12:33:03.075 11133-11133 VRI[PopupW...3]@5e720e2 com.example.aquascope                I  dispatchDetachedFromWindow
        2026-10-07 12:33:03.075 11133-11133 BLASTBufferQueue        com.example.aquascope                I  [VRI[PopupWindow:c5ba8e3]@5e720e2#3](f:0,a:3,s:0) destructor()
        2026-10-07 12:33:03.076 11133-11133 BufferQueueConsumer     com.example.aquascope                D  [VRI[PopupWindow:c5ba8e3]@5e720e2#3(BLAST Consumer)3](id:2b7d00000007,api:0,p:-1,c:11133) disconnect
        2026-10-07 12:33:03.082 11133-11133 InputTransport          com.example.aquascope                D  Input channel destroyed: '936da52', fd=176
        2026-10-07 12:33:03.096 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                D  mThreadedRenderer.initializeIfNeeded()#2 mSurface={isValid=true 0xb40000787a100ef0}
        2026-10-07 12:33:03.100 11133-11133 InputMethodManagerUtils com.example.aquascope                D  startInputInner - Id : 0
        2026-10-07 12:33:03.100 11133-11133 InputMethodManager      com.example.aquascope                I  startInputInner - IInputMethodManagerGlobalInvoker.startInputOrWindowGainedFocus
        2026-10-07 12:33:03.107 11133-11459 InputTransport          com.example.aquascope                D  Input channel constructed: 'ClientS', fd=174
        2026-10-07 12:33:03.110 11133-11133 InsetsController        com.example.aquascope                D  hide(ime())
        2026-10-07 12:33:03.110 11133-11133 ImeTracker              com.example.aquascope                I  com.example.aquascope:48b6f58a: onCancelled at PHASE_CLIENT_ALREADY_HIDDEN
        2026-10-07 12:33:03.362 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[0,75][1080,2340] display=[0,75][1080,2340] parentFrame=[0,0][0,0]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:33:04.562 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:04.562 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:04.563 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:04.563 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:04.565 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:04.571 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:04.571 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:04.571 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:04.571 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:04.572 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:04.572 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:04.572 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:04.572 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:04.572 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:04.572 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:04.574 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:04.579 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:04.579 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:04.579 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:04.579 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:04.579 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:04.579 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:04.595 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=no preference, reason=boost timeout, vri=VRI[MainActivity]@17e0af0
        2026-10-07 12:33:04.717 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 20558.0
        2026-10-07 12:33:04.717 11133-11994 log                     com.example.aquascope                E  Listening........ (0.07, 2)
        2026-10-07 12:33:04.772 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 57MB AllocSpace bytes, 303(24MB) LOS objects, 0% free, 190MB/190MB, paused 740us,8.786ms total 176.434ms
        2026-10-07 12:33:04.773 11133-11142 InputTransport          com.example.aquascope                D  Input channel destroyed: 'ClientS', fd=174
        2026-10-07 12:33:05.033 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:05.033 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:05.034 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:05.034 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:05.036 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:05.042 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:05.042 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:05.042 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:05.043 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:05.043 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:05.043 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:05.043 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:05.043 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:05.043 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:05.044 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:05.045 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:05.050 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:05.050 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:05.050 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:05.050 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:05.051 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:05.051 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:05.094 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 5186.0
        2026-10-07 12:33:05.095 11133-11994 log                     com.example.aquascope                E  Listening........ (0.04, 3)
        2026-10-07 12:33:05.102 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 69MB AllocSpace bytes, 543(42MB) LOS objects, 25% free, 138MB/186MB, paused 603us,14.054ms total 319.698ms
        2026-10-07 12:33:05.593 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:05.593 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:05.594 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:05.594 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:05.596 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:05.601 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:05.601 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:05.601 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:05.601 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:05.602 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:05.602 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:05.602 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:05.602 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:05.602 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:05.602 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:05.604 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:05.609 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:05.609 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:05.609 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:05.609 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:05.609 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:05.609 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:05.703 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 34120.0
        2026-10-07 12:33:05.703 11133-11994 log                     com.example.aquascope                E  Listening........ (0.11, 4)
        2026-10-07 12:33:05.803 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 61MB AllocSpace bytes, 521(44MB) LOS objects, 22% free, 145MB/186MB, paused 576us,10.814ms total 174.759ms
        2026-10-07 12:33:05.856 11133-11133 InsetsController        com.example.aquascope                I  onStateChanged: host=com.example.aquascope/com.example.root.ffttest2.MainActivity, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {3 mType=ime mFrame=[0,0][0,0] mAttachedInsets=null mVisible=false mFlags=INVALID mSideHint=NONE mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=false mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:33:05.860 11133-11133 InsetsController        com.example.aquascope                I  onStateChanged: host=com.example.aquascope/com.example.root.ffttest2.MainActivity, from=android.view.ViewRootImpl.onInsetsStateChanged:3038, state=InsetsState: {mDisplayFrame=Rect(0, 0 - 1080, 2340), mDisplayCutout=DisplayCutout{insets=Rect(0, 75 - 0, 0) waterfall=Insets{left=0, top=0, right=0, bottom=0} boundingRect={Bounds=[Rect(0, 0 - 0, 0), Rect(468, 0 - 612, 75), Rect(0, 0 - 0, 0), Rect(0, 0 - 0, 0)]} cutoutPathParserInfo={CutoutPathParserInfo{displayWidth=1080 displayHeight=2340 physicalDisplayWidth=1080 physicalDisplayHeight=2340 density={2.8125} cutoutSpec={M 0,0 H -25.6 V 26.66666666666667 H 25.6 V 0 H 0 Z @dp} rotation={0} scale={1.0} physicalPixelDisplaySizeRatio={1.0}}} sideOverrides={}}, mRoundedCorners=RoundedCorners{[RoundedCorner{position=TopLeft, radius=113, center=Point(113, 113)}, RoundedCorner{position=TopRight, radius=113, center=Point(967, 113)}, RoundedCorner{position=BottomRight, radius=113, center=Point(967, 2227)}, RoundedCorner{position=BottomLeft, radius=113, center=Point(113, 2227)}]}  mRoundedCornerFrame=Rect(0, 0 - 1080, 2340), mPrivacyIndicatorBounds=PrivacyIndicatorBounds {static bounds=Rect(956, 0 - 1080, 75) rotation=0}, mDisplayShape=DisplayShape{ spec=-311912193 displayWidth=1080 displayHeight=2340 physicalPixelDisplaySizeRatio=1.0 rotation=0 offsetX=0 offsetY=0 scale=1.0}, mSources= { InsetsSource: {9c780000 mType=statusBars mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=false mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780005 mType=mandatorySystemGestures mFrame=[0,0][1080,109] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {9c780006 mType=tappableElement mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {3 mType=ime mFrame=[0,0][0,0] mAttachedInsets=null mVisible=false mFlags=INVALID mSideHint=NONE mBoundingRects=null}, InsetsSource: {27 mType=displayCutout mFrame=[0,0][1080,75] mAttachedInsets=null mVisible=true mFlags= mSideHint=TOP mBoundingRects=null}, InsetsSource: {64ed0001 mType=navigationBars mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=false mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0004 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null}, InsetsSource: {64ed0005 mType=mandatorySystemGestures mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0006 mType=tappableElement mFrame=[0,2205][1080,2340] mAttachedInsets=null mVisible=true mFlags= mSideHint=BOTTOM mBoundingRects=null}, InsetsSource: {64ed0024 mType=systemGestures mFrame=[0,0][0,0] mAttachedInsets=null mVisible=true mFlags= mSideHint=NONE mBoundingRects=null} }
        2026-10-07 12:33:05.860 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  handleResized, frames=ClientWindowFrames{frame=[0,75][1080,2340] display=[0,75][1080,2340] parentFrame=[0,0][0,0]} displayId=0 dragResizing=false compatScale=1.0 frameChanged=false attachedFrameChanged=false configChanged=false displayChanged=false compatScaleChanged=false dragResizingChanged=false
        2026-10-07 12:33:06.072 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:06.072 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:06.073 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:06.073 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:06.074 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:06.080 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:06.080 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:06.080 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:06.080 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:06.080 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:06.080 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:06.080 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:06.080 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:06.081 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:06.081 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:06.083 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:06.088 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:06.088 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:06.088 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:06.088 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:06.088 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:06.089 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:06.099 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 39MB AllocSpace bytes, 320(24MB) LOS objects, 35% free, 85MB/133MB, paused 1.044ms,10.308ms total 295.678ms
        2026-10-07 12:33:06.182 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 10120.0
        2026-10-07 12:33:06.183 11133-11994 log                     com.example.aquascope                E  Listening........ (0.11, 5)
        2026-10-07 12:33:06.269 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 40MB AllocSpace bytes, 332(26MB) LOS objects, 5% free, 125MB/133MB, paused 556us,8.523ms total 138.613ms
        2026-10-07 12:33:06.559 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:06.559 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:06.559 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:06.559 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:06.561 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:06.563 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 27MB AllocSpace bytes, 223(17MB) LOS objects, 36% free, 83MB/131MB, paused 669us,13.142ms total 293.185ms
        2026-10-07 12:33:06.566 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:06.566 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:06.566 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:06.566 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:06.566 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:06.567 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:06.567 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:06.567 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:06.567 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:06.567 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:06.569 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:06.573 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:06.573 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:06.574 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:06.574 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:06.574 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:06.574 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:06.666 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 28820.0
        2026-10-07 12:33:06.666 11133-11994 log                     com.example.aquascope                E  Listening........ (0.09, 6)
        2026-10-07 12:33:06.750 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 39MB AllocSpace bytes, 321(25MB) LOS objects, 2% free, 128MB/131MB, paused 692us,8.654ms total 138.989ms
        2026-10-07 12:33:07.031 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:07.031 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:07.032 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:07.032 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:07.034 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:07.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:07.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:07.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:07.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:07.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:07.040 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:07.040 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:07.040 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:07.040 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:07.040 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:07.046 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:07.050 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 29MB AllocSpace bytes, 234(18MB) LOS objects, 36% free, 84MB/132MB, paused 872us,17.230ms total 299.099ms
        2026-10-07 12:33:07.056 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:07.056 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:07.056 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:07.056 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:07.057 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:07.057 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:07.148 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 35898.0
        2026-10-07 12:33:07.149 11133-11994 log                     com.example.aquascope                E  Listening........ (0.09, 7)
        2026-10-07 12:33:07.233 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 40MB AllocSpace bytes, 339(27MB) LOS objects, 5% free, 125MB/132MB, paused 851us,8.848ms total 140.761ms
        2026-10-07 12:33:07.486 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 27MB AllocSpace bytes, 218(17MB) LOS objects, 36% free, 81MB/129MB, paused 568us,8.208ms total 252.238ms
        2026-10-07 12:33:07.556 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:07.556 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:07.556 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:07.557 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:07.558 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:07.563 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:07.563 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:07.563 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:07.563 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:07.564 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:07.564 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:07.564 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:07.564 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:07.564 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:07.564 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:07.566 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:07.570 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:07.570 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:07.571 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:07.571 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:07.571 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:07.571 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:07.693 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 11898.0
        2026-10-07 12:33:07.693 11133-11994 log                     com.example.aquascope                E  Listening........ (0.09, 8)
        2026-10-07 12:33:07.747 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 40MB AllocSpace bytes, 337(27MB) LOS objects, 0% free, 179MB/179MB, paused 561us,9.237ms total 140.582ms
        2026-10-07 12:33:08.017 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 60MB AllocSpace bytes, 489(37MB) LOS objects, 36% free, 81MB/129MB, paused 673us,8.073ms total 269.619ms
        2026-10-07 12:33:08.060 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:08.060 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:08.060 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:08.060 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:08.062 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:08.067 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:08.067 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:08.067 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:08.067 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:08.067 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:08.067 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:08.067 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:08.067 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:08.068 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:08.068 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:08.069 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:08.074 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:08.074 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:08.074 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:08.074 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:08.075 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:08.075 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:08.164 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 9372.0
        2026-10-07 12:33:08.165 11133-11994 log                     com.example.aquascope                E  Listening........ (0.10, 9)
        2026-10-07 12:33:08.237 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 39MB AllocSpace bytes, 328(26MB) LOS objects, 1% free, 127MB/129MB, paused 682us,8.999ms total 127.172ms
        2026-10-07 12:33:08.487 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 28MB AllocSpace bytes, 227(17MB) LOS objects, 36% free, 81MB/129MB, paused 726us,7.932ms total 248.992ms
        2026-10-07 12:33:08.532 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:08.532 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:08.533 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:08.533 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:08.534 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:08.539 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:08.539 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:08.539 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:08.539 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:08.540 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:08.540 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:08.540 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:08.540 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:08.540 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:08.540 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:08.542 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:08.547 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:08.547 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:08.547 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:08.547 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:08.547 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:08.547 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:08.637 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 26494.0
        2026-10-07 12:33:08.637 11133-11994 log                     com.example.aquascope                E  Listening........ (0.18, 10)
        2026-10-07 12:33:08.713 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 40MB AllocSpace bytes, 336(27MB) LOS objects, 3% free, 125MB/129MB, paused 571us,10.367ms total 130.092ms
        2026-10-07 12:33:08.962 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 27MB AllocSpace bytes, 219(17MB) LOS objects, 36% free, 81MB/129MB, paused 679us,7.918ms total 248.918ms
        2026-10-07 12:33:09.008 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:09.008 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:09.009 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:09.009 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:09.010 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:09.015 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:09.015 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:09.015 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:09.015 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:09.016 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:09.016 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:09.016 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:09.016 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:09.016 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:09.016 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:09.018 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:09.023 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:09.023 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:09.023 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:09.023 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:09.023 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:09.023 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:09.112 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 2494.0
        2026-10-07 12:33:09.113 11133-11994 log                     com.example.aquascope                E  Listening........ (0.18, 11)
        2026-10-07 12:33:09.185 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 39MB AllocSpace bytes, 327(26MB) LOS objects, 1% free, 128MB/129MB, paused 545us,9.051ms total 124.367ms
        2026-10-07 12:33:09.439 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 28MB AllocSpace bytes, 230(18MB) LOS objects, 36% free, 81MB/129MB, paused 644us,9.905ms total 254.241ms
        2026-10-07 12:33:09.584 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:09.584 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:09.585 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:09.585 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:09.586 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:09.591 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:09.591 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:09.591 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:09.591 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:09.591 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:09.591 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:09.591 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:09.591 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:09.592 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:09.592 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:09.593 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:09.598 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:09.598 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:09.598 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:09.598 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:09.599 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:09.599 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:09.723 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 13038.0
        2026-10-07 12:33:09.723 11133-11994 log                     com.example.aquascope                E  Listening........ (0.12, 12)
        2026-10-07 12:33:09.766 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 41MB AllocSpace bytes, 339(27MB) LOS objects, 0% free, 178MB/178MB, paused 649us,8.946ms total 131.003ms
        2026-10-07 12:33:10.034 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 60MB AllocSpace bytes, 487(37MB) LOS objects, 36% free, 83MB/131MB, paused 590us,8.451ms total 268.156ms
        2026-10-07 12:33:10.052 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:10.052 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:10.052 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:10.052 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:10.054 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:10.059 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:10.059 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:10.059 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:10.059 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:10.059 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:10.059 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:10.059 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:10.060 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:10.060 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:10.060 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:10.062 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:10.066 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:10.066 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:10.067 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:10.067 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:10.067 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:10.067 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:10.159 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 35876.0
        2026-10-07 12:33:10.160 11133-11994 log                     com.example.aquascope                E  Listening........ (0.12, 13)
        2026-10-07 12:33:10.244 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 40MB AllocSpace bytes, 333(26MB) LOS objects, 3% free, 126MB/131MB, paused 562us,10.476ms total 140.807ms
        2026-10-07 12:33:10.552 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:10.552 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:10.552 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:10.553 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:10.553 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 27MB AllocSpace bytes, 222(17MB) LOS objects, 36% free, 83MB/131MB, paused 766us,15.176ms total 308.936ms
        2026-10-07 12:33:10.554 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:10.559 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:10.560 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:10.560 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:10.560 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:10.560 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:10.560 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:10.560 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:10.560 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:10.561 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:10.561 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:10.562 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:10.567 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:10.567 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:10.568 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:10.568 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:10.568 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:10.568 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:10.694 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 11876.0
        2026-10-07 12:33:10.694 11133-11994 log                     com.example.aquascope                E  Listening........ (0.12, 14)
        2026-10-07 12:33:10.742 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 41MB AllocSpace bytes, 338(27MB) LOS objects, 0% free, 178MB/178MB, paused 511us,10.170ms total 138.308ms
        2026-10-07 12:33:11.031 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:11.031 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:11.032 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:11.032 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:11.033 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:11.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:11.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:11.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:11.040 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:11.040 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:11.040 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:11.040 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:11.040 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:11.041 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:11.041 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:11.044 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:11.050 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:11.050 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:11.050 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:11.050 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:11.051 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:11.051 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:11.062 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 60MB AllocSpace bytes, 488(37MB) LOS objects, 34% free, 90MB/138MB, paused 780us,14.155ms total 319.298ms
        2026-10-07 12:33:11.149 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 6106.0
        2026-10-07 12:33:11.149 11133-11994 log                     com.example.aquascope                E  Listening........ (0.09, 15)
        2026-10-07 12:33:11.226 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 42MB AllocSpace bytes, 351(28MB) LOS objects, 10% free, 123MB/138MB, paused 568us,9.039ms total 135.944ms
        2026-10-07 12:33:11.538 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 25MB AllocSpace bytes, 206(16MB) LOS objects, 36% free, 83MB/131MB, paused 994us,13.932ms total 310.695ms
        2026-10-07 12:33:11.569 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:11.569 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:11.570 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:11.571 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:11.572 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:11.577 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:11.577 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:11.577 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:11.577 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:11.578 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:11.578 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:11.578 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:11.578 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:11.578 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:11.579 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:11.580 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:11.585 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:11.585 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:11.585 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:11.585 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:11.585 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:11.586 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:11.675 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 34150.0
        2026-10-07 12:33:11.675 11133-11994 log                     com.example.aquascope                E  Listening........ (0.10, 16)
        2026-10-07 12:33:11.766 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 40MB AllocSpace bytes, 335(27MB) LOS objects, 4% free, 126MB/131MB, paused 545us,9.309ms total 144.048ms
        2026-10-07 12:33:12.068 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:12.068 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:12.069 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:12.069 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:12.071 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:12.073 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 27MB AllocSpace bytes, 220(17MB) LOS objects, 36% free, 84MB/132MB, paused 1.001ms,13.985ms total 306.508ms
        2026-10-07 12:33:12.076 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:12.076 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:12.077 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:12.077 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:12.077 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:12.077 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:12.077 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:12.077 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:12.077 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:12.078 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:12.079 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:12.084 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:12.084 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:12.084 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:12.084 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:12.084 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:12.084 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:12.211 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 15986.0
        2026-10-07 12:33:12.211 11133-11994 log                     com.example.aquascope                E  Listening........ (0.15, 17)
        2026-10-07 12:33:12.252 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 40MB AllocSpace bytes, 335(27MB) LOS objects, 0% free, 179MB/179MB, paused 893us,9.184ms total 131.278ms
        2026-10-07 12:33:12.542 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:12.542 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:12.543 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:12.543 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:12.545 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:12.551 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:12.551 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:12.552 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:12.552 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:12.552 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:12.552 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:12.552 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:12.552 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:12.553 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:12.554 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:12.555 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:12.560 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:12.560 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:12.561 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:12.561 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:12.561 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:12.561 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:12.583 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 60MB AllocSpace bytes, 491(37MB) LOS objects, 31% free, 105MB/153MB, paused 3.719ms,15.157ms total 329.664ms
        2026-10-07 12:33:12.685 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 22060.0
        2026-10-07 12:33:12.685 11133-11994 log                     com.example.aquascope                E  Listening........ (0.14, 18)
        2026-10-07 12:33:12.744 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 47MB AllocSpace bytes, 392(31MB) LOS objects, 0% free, 168MB/168MB, paused 520us,11.048ms total 138.471ms
        2026-10-07 12:33:13.032 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:13.032 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:13.032 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:13.033 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:13.034 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:13.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:13.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:13.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:13.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:13.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:13.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:13.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:13.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:13.040 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:13.040 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:13.042 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:13.046 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:13.046 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:13.047 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:13.047 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:13.047 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:13.048 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:13.075 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 53MB AllocSpace bytes, 434(33MB) LOS objects, 30% free, 107MB/155MB, paused 991us,13.433ms total 329.443ms
        2026-10-07 12:33:13.098 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 31872.0
        2026-10-07 12:33:13.099 11133-11994 log                     com.example.aquascope                E  Listening........ (0.08, 19)
        2026-10-07 12:33:13.591 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:13.591 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:13.592 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:13.592 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:13.594 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:13.599 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:13.599 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:13.599 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:13.600 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:13.600 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:13.600 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:13.600 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:13.600 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:13.601 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:13.601 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:13.602 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:13.607 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:13.607 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:13.608 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:13.608 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:13.608 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:13.608 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:13.704 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 7872.0
        2026-10-07 12:33:13.705 11133-11994 log                     com.example.aquascope                E  Listening........ (0.08, 20)
        2026-10-07 12:33:13.727 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 33MB AllocSpace bytes, 288(25MB) LOS objects, 0% free, 194MB/194MB, paused 541us,9.632ms total 136.525ms
        2026-10-07 12:33:14.055 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:14.055 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:14.056 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:14.056 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:14.057 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:14.060 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 68MB AllocSpace bytes, 553(43MB) LOS objects, 36% free, 84MB/132MB, paused 820us,10.726ms total 332.596ms
        2026-10-07 12:33:14.063 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:14.063 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:14.064 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:14.064 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:14.064 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:14.065 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:14.065 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:14.065 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:14.065 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:14.065 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:14.067 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:14.072 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:14.072 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:14.072 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:14.072 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:14.072 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:14.072 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:14.162 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 25626.0
        2026-10-07 12:33:14.162 11133-11994 log                     com.example.aquascope                E  Listening........ (0.08, 21)
        2026-10-07 12:33:14.250 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 40MB AllocSpace bytes, 333(26MB) LOS objects, 4% free, 126MB/132MB, paused 512us,10.259ms total 140.900ms
        2026-10-07 12:33:14.455 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:33:14.462 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=high hint, reason=touch, vri=VRI[MainActivity]@17e0af0
        2026-10-07 12:33:14.542 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 27MB AllocSpace bytes, 222(17MB) LOS objects, 36% free, 83MB/131MB, paused 1.105ms,14.053ms total 292.078ms
        2026-10-07 12:33:14.545 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:14.545 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:14.546 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:14.546 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:14.547 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:14.552 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:14.552 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:14.553 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:14.553 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:14.553 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:14.553 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:14.553 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:14.553 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:14.554 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:14.554 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:14.555 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:14.561 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:14.561 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:14.562 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:14.562 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:14.562 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:14.562 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:14.656 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 1626.0
        2026-10-07 12:33:14.656 11133-11994 log                     com.example.aquascope                E  Listening........ (0.08, 22)
        2026-10-07 12:33:14.740 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 41MB AllocSpace bytes, 344(27MB) LOS objects, 5% free, 124MB/131MB, paused 558us,9.259ms total 138.643ms
        2026-10-07 12:33:14.772 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:33:15.013 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 26MB AllocSpace bytes, 211(16MB) LOS objects, 36% free, 83MB/131MB, paused 852us,8.564ms total 272.689ms
        2026-10-07 12:33:15.037 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:15.037 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:15.038 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:15.038 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:15.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:15.044 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:15.044 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:15.045 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:15.045 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:15.045 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:15.045 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:15.045 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:15.045 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:15.046 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:15.046 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:15.047 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:15.052 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:15.052 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:15.052 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:15.053 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:15.053 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:15.053 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:15.150 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 9286.0
        2026-10-07 12:33:15.151 11133-11994 log                     com.example.aquascope                E  Listening........ (0.20, 23)
        2026-10-07 12:33:15.550 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:15.550 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:15.551 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:15.551 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:15.553 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:15.558 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:15.558 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:15.559 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:15.559 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:15.559 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:15.560 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:15.560 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:15.560 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:15.560 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:15.561 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:15.562 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:15.568 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:15.568 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:15.568 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:15.568 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:15.569 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:15.569 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:15.625 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 0.0
        2026-10-07 12:33:15.625 11133-11994 log                     com.example.aquascope                E  Listening........ (0.00, 24)
        2026-10-07 12:33:15.650 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 23MB AllocSpace bytes, 189(16MB) LOS objects, 14% free, 113MB/131MB, paused 178us,8.192ms total 58.578ms
        2026-10-07 12:33:16.085 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:16.085 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:16.086 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:16.086 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:16.087 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:16.092 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:16.092 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:16.092 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:16.093 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:16.093 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:16.093 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:16.093 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:16.093 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:16.093 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:16.094 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:16.095 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:16.100 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:16.100 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:16.100 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:16.100 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:16.101 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:16.101 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:16.232 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 28522.0
        2026-10-07 12:33:16.232 11133-11994 log                     com.example.aquascope                E  Listening........ (0.07, 25)
        2026-10-07 12:33:16.270 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 39MB AllocSpace bytes, 323(26MB) LOS objects, 0% free, 187MB/187MB, paused 3.294ms,8.984ms total 135.583ms
        2026-10-07 12:33:16.561 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:16.561 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:16.562 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:16.562 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:16.564 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:16.570 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:16.570 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:16.570 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:16.570 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:16.571 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:16.571 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:16.571 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:16.571 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:16.572 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:16.573 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:16.574 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:16.579 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:16.579 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:16.580 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:16.580 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:16.580 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:16.580 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:16.589 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 0.0
        2026-10-07 12:33:16.589 11133-11994 log                     com.example.aquascope                E  Listening........ (0.00, 26)
        2026-10-07 12:33:16.603 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 64MB AllocSpace bytes, 523(41MB) LOS objects, 35% free, 86MB/134MB, paused 991us,16.803ms total 331.813ms
        2026-10-07 12:33:17.033 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:17.033 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:17.034 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:17.034 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:17.036 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:17.042 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:17.043 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:17.043 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:17.043 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:17.044 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:17.044 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:17.044 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:17.044 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:17.044 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:17.045 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:17.046 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:17.051 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:17.051 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:17.051 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:17.051 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:17.051 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:17.051 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:17.135 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 7984.0
        2026-10-07 12:33:17.135 11133-11994 log                     com.example.aquascope                E  Listening........ (0.09, 27)
        2026-10-07 12:33:17.245 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 44MB AllocSpace bytes, 380(33MB) LOS objects, 9% free, 121MB/134MB, paused 3.118ms,9.881ms total 156.676ms
        2026-10-07 12:33:17.549 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 24MB AllocSpace bytes, 190(15MB) LOS objects, 36% free, 83MB/131MB, paused 925us,12.296ms total 302.415ms
        2026-10-07 12:33:17.569 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:17.569 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:17.570 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:17.570 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:17.571 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:17.576 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:17.576 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:17.577 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:17.577 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:17.577 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:17.577 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:17.577 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:17.577 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:17.578 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:17.578 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:17.580 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:17.584 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:17.584 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:17.585 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:17.585 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:17.585 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:17.585 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:17.675 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 29728.0
        2026-10-07 12:33:17.675 11133-11994 log                     com.example.aquascope                E  Listening........ (0.09, 28)
        2026-10-07 12:33:17.754 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 40MB AllocSpace bytes, 335(27MB) LOS objects, 4% free, 126MB/131MB, paused 540us,9.834ms total 132.222ms
        2026-10-07 12:33:17.772 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=no preference, reason=boost timeout, vri=VRI[MainActivity]@17e0af0
        2026-10-07 12:33:18.058 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 27MB AllocSpace bytes, 220(17MB) LOS objects, 36% free, 83MB/131MB, paused 764us,13.197ms total 303.236ms
        2026-10-07 12:33:18.086 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:18.086 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:18.087 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:18.087 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:18.089 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:18.094 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:18.094 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:18.094 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:18.094 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:18.094 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:18.094 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:18.094 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:18.094 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:18.095 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:18.095 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:18.096 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:18.101 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:18.101 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:18.102 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:18.102 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:18.102 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:18.102 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:18.192 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 5728.0
        2026-10-07 12:33:18.192 11133-11994 log                     com.example.aquascope                E  Listening........ (0.09, 29)
        2026-10-07 12:33:18.288 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 43MB AllocSpace bytes, 356(28MB) LOS objects, 7% free, 122MB/131MB, paused 659us,8.940ms total 150.182ms
        2026-10-07 12:33:18.554 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:18.554 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:18.555 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:18.555 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:18.557 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:18.562 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:18.562 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:18.563 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:18.563 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:18.563 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:18.563 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:18.563 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:18.563 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:18.564 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:18.564 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:18.566 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:18.570 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:18.571 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:18.571 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:18.571 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:18.571 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:18.571 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:18.588 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 24MB AllocSpace bytes, 199(15MB) LOS objects, 33% free, 95MB/143MB, paused 871us,14.701ms total 298.267ms
        2026-10-07 12:33:18.702 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 17596.0
        2026-10-07 12:33:18.702 11133-11994 log                     com.example.aquascope                E  Listening........ (0.41, 30)
        2026-10-07 12:33:18.752 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 43MB AllocSpace bytes, 359(28MB) LOS objects, 0% free, 174MB/174MB, paused 3.049ms,9.902ms total 138.240ms
        2026-10-07 12:33:19.030 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:19.031 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:19.031 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:19.031 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:19.033 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:19.038 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:19.038 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:19.038 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:19.038 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:19.038 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:19.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:19.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:19.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:19.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:19.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:19.041 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:19.045 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:19.045 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:19.046 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:19.046 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:19.046 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:19.046 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:19.073 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 57MB AllocSpace bytes, 467(35MB) LOS objects, 29% free, 113MB/161MB, paused 1.010ms,13.104ms total 319.234ms
        2026-10-07 12:33:19.125 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 35284.0
        2026-10-07 12:33:19.126 11133-11994 log                     com.example.aquascope                E  Listening........ (0.17, 31)
        2026-10-07 12:33:19.233 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 53MB AllocSpace bytes, 439(34MB) LOS objects, 31% free, 106MB/154MB, paused 687us,9.411ms total 138.606ms
        2026-10-07 12:33:19.583 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:19.583 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:19.584 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:19.584 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:19.586 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:19.592 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:19.592 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:19.592 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:19.592 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:19.593 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:19.593 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:19.593 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:19.593 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:19.594 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:19.594 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:19.595 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:19.601 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:19.601 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:19.601 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:19.602 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:19.602 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:19.602 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:19.616 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 13MB AllocSpace bytes, 111(8408KB) LOS objects, 33% free, 97MB/145MB, paused 1.261ms,14.158ms total 106.349ms
        2026-10-07 12:33:19.717 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 11284.0
        2026-10-07 12:33:19.717 11133-11994 log                     com.example.aquascope                E  Listening........ (0.17, 32)
        2026-10-07 12:33:19.988 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 101MB AllocSpace bytes, 827(64MB) LOS objects, 36% free, 83MB/131MB, paused 801us,10.045ms total 358.175ms
        2026-10-07 12:33:20.062 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:20.062 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:20.063 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:20.063 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:20.065 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:20.070 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:20.070 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:20.071 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:20.071 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:20.071 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:20.071 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:20.071 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:20.071 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:20.072 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:20.072 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:20.073 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:20.076 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 316KB AllocSpace bytes, 0(0B) LOS objects, 34% free, 86MB/131MB, paused 681us,13.966ms total 86.636ms
        2026-10-07 12:33:20.078 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:20.078 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:20.079 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:20.079 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:20.079 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:20.079 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:20.192 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 9164.0
        2026-10-07 12:33:20.193 11133-11994 log                     com.example.aquascope                E  Listening........ (0.12, 33)
        2026-10-07 12:33:20.469 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 101MB AllocSpace bytes, 826(64MB) LOS objects, 36% free, 83MB/131MB, paused 728us,10.861ms total 355.421ms
        2026-10-07 12:33:20.541 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:20.541 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:20.542 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:20.542 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:20.544 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:20.549 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:20.550 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:20.550 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:20.550 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:20.550 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:20.551 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:20.551 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:20.551 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:20.551 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:20.552 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:20.553 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:20.558 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:20.558 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:20.558 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:20.558 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:20.559 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:20.559 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:20.563 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 356KB AllocSpace bytes, 1(48KB) LOS objects, 33% free, 88MB/131MB, paused 866us,14.472ms total 92.995ms
        2026-10-07 12:33:20.668 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 26720.0
        2026-10-07 12:33:20.669 11133-11994 log                     com.example.aquascope                E  Listening........ (0.13, 34)
        2026-10-07 12:33:20.945 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 100MB AllocSpace bytes, 825(64MB) LOS objects, 36% free, 83MB/131MB, paused 792us,11.499ms total 354.147ms
        2026-10-07 12:33:21.015 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:21.015 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:21.015 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:21.016 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:21.018 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:21.025 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:21.025 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:21.026 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:21.026 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:21.026 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:21.027 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:21.027 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:21.027 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:21.027 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:21.027 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:21.029 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:21.035 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:21.035 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:21.035 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:21.035 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:21.036 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:21.036 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:21.038 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 328KB AllocSpace bytes, 1(48KB) LOS objects, 32% free, 88MB/131MB, paused 863us,13.928ms total 91.780ms
        2026-10-07 12:33:21.145 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 2720.0
        2026-10-07 12:33:21.145 11133-11994 log                     com.example.aquascope                E  Listening........ (0.13, 35)
        2026-10-07 12:33:21.427 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 100MB AllocSpace bytes, 827(64MB) LOS objects, 36% free, 83MB/131MB, paused 3.633ms,11.909ms total 361.946ms
        2026-10-07 12:33:21.518 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 340KB AllocSpace bytes, 0(0B) LOS objects, 35% free, 85MB/131MB, paused 868us,13.608ms total 91.318ms
        2026-10-07 12:33:21.576 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:21.576 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:21.577 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:21.577 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:21.579 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:21.585 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:21.585 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:21.586 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:21.586 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:21.587 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:21.587 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:21.587 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:21.587 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:21.587 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:21.588 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:21.590 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:21.597 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:21.597 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:21.597 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:21.597 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:21.598 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:21.598 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:21.675 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 17450.0
        2026-10-07 12:33:21.675 11133-11994 log                     com.example.aquascope                E  Listening........ (0.13, 36)
        2026-10-07 12:33:21.971 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 67MB AllocSpace bytes, 555(44MB) LOS objects, 36% free, 83MB/131MB, paused 678us,14.182ms total 339.467ms
        2026-10-07 12:33:21.983 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:33:21.985 11133-11133 GestureDetector         com.example.aquascope                I  obtain mCurrentDownEvent. id: 943102844 caller: android.view.ScaleGestureDetector.onTouchEvent:311 com.jjoe64.graphview.Viewport.onTouchEvent:655 com.jjoe64.graphview.GraphView.onTouchEvent:448
        2026-10-07 12:33:21.986 11133-11133 GestureDetector         com.example.aquascope                I  obtain mCurrentDownEvent. id: 943102844 caller: com.jjoe64.graphview.Viewport.onTouchEvent:656 com.jjoe64.graphview.GraphView.onTouchEvent:448 android.view.View.performOnTouchCallback:17597
        2026-10-07 12:33:21.987 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=high hint, reason=touch, vri=VRI[MainActivity]@17e0af0
        2026-10-07 12:33:22.001 11133-11133 GestureDetector         com.example.aquascope                I  obtain mCurrentMotionEventRaw. action: 2 id: 1054042946
        2026-10-07 12:33:22.001 11133-11133 GestureDetector         com.example.aquascope                I  obtain mCurrentMotionEventRaw. action: 2 id: 1054042946
        2026-10-07 12:33:22.059 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:22.059 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:22.059 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:22.059 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:22.061 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:22.067 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:22.067 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:22.067 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:22.068 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:22.068 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:22.068 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:22.068 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:22.068 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:22.069 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:22.069 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:22.070 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:22.075 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:22.075 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:22.075 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:22.075 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:22.076 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:22.076 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:22.194 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 22MB AllocSpace bytes, 188(16MB) LOS objects, 0% free, 174MB/174MB, paused 599us,8.734ms total 98.954ms
        2026-10-07 12:33:22.222 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 19728.0
        2026-10-07 12:33:22.222 11133-11994 log                     com.example.aquascope                E  Listening........ (0.23, 37)
        2026-10-07 12:33:22.362 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:33:22.410 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 78MB AllocSpace bytes, 638(48MB) LOS objects, 36% free, 84MB/132MB, paused 3.490ms,8.924ms total 216.203ms
        2026-10-07 12:33:22.521 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:22.521 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:22.521 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:22.521 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:22.523 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:22.528 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:22.528 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:22.528 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:22.528 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:22.528 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:22.528 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:22.528 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:22.528 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:22.529 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:22.529 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:22.530 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:22.535 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:22.535 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:22.536 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:22.536 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:22.536 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:22.536 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:22.627 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 6328.0
        2026-10-07 12:33:22.628 11133-11994 log                     com.example.aquascope                E  Listening........ (0.22, 38)
        2026-10-07 12:33:22.752 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 38MB AllocSpace bytes, 305(23MB) LOS objects, 36% free, 83MB/131MB, paused 304us,2.128ms total 135.649ms
        2026-10-07 12:33:22.878 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:33:23.038 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:23.038 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:23.038 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:23.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:23.040 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:23.046 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:23.046 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:23.046 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:23.046 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:23.046 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:23.046 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:23.046 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:23.046 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:23.047 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:23.047 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:23.048 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:23.054 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:23.054 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:23.054 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:23.054 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:23.054 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:23.054 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:23.153 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 9390.0
        2026-10-07 12:33:23.153 11133-11994 log                     com.example.aquascope                E  Listening........ (0.14, 39)
        2026-10-07 12:33:23.154 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:33:23.175 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 37MB AllocSpace bytes, 309(25MB) LOS objects, 0% free, 133MB/133MB, paused 1.407ms,8.664ms total 87.315ms
        2026-10-07 12:33:23.449 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 31MB AllocSpace bytes, 254(20MB) LOS objects, 36% free, 82MB/130MB, paused 813us,8.330ms total 273.117ms
        2026-10-07 12:33:23.595 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:23.595 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:23.595 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:23.596 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:23.598 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:23.605 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:23.605 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:23.605 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:23.605 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:23.605 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:23.605 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:23.605 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:23.605 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:23.606 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:23.606 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:23.608 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:23.613 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:23.613 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:23.613 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:23.613 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:23.614 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:23.614 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:23.705 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 28100.0
        2026-10-07 12:33:23.706 11133-11994 log                     com.example.aquascope                E  Listening........ (0.11, 40)
        2026-10-07 12:33:23.795 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 40MB AllocSpace bytes, 330(26MB) LOS objects, 2% free, 127MB/130MB, paused 599us,11.180ms total 145.837ms
        2026-10-07 12:33:23.946 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:33:23.986 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:33:24.035 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:24.035 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:24.035 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:24.035 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:24.037 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:24.042 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:24.042 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:24.042 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:24.042 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:24.043 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:24.043 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:24.043 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:24.043 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:24.043 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:24.044 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:24.045 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:24.050 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:24.050 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:24.050 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:24.050 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:24.051 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:24.051 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:24.060 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 28MB AllocSpace bytes, 225(17MB) LOS objects, 35% free, 87MB/135MB, paused 658us,8.166ms total 264.107ms
        2026-10-07 12:33:24.137 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 4100.0
        2026-10-07 12:33:24.138 11133-11994 log                     com.example.aquascope                E  Listening........ (0.11, 41)
        2026-10-07 12:33:24.244 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 25MB AllocSpace bytes, 207(15MB) LOS objects, 36% free, 84MB/132MB, paused 511us,9.917ms total 80.683ms
        2026-10-07 12:33:24.528 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:24.528 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:24.529 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:24.529 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:24.530 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:24.535 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:24.535 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:24.535 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:24.535 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:24.536 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:24.536 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:24.536 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:24.536 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:24.536 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:24.537 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:24.538 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:24.543 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:24.543 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:24.543 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:24.543 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:24.543 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:24.543 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:24.586 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 5906.0
        2026-10-07 12:33:24.586 11133-11994 log                     com.example.aquascope                E  Listening........ (0.09, 42)
        2026-10-07 12:33:24.846 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 35MB AllocSpace bytes, 287(24MB) LOS objects, 36% free, 83MB/131MB, paused 3.551ms,7.995ms total 267.307ms
        2026-10-07 12:33:25.012 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:25.012 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:25.012 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:25.013 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:25.014 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:25.019 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:25.019 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:25.019 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:25.019 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:25.019 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:25.019 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:25.019 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:25.019 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:25.020 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:25.020 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:25.022 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:25.027 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:25.027 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:25.027 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:25.027 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:25.028 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:25.028 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:25.117 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 27826.0
        2026-10-07 12:33:25.117 11133-11994 log                     com.example.aquascope                E  Listening........ (0.10, 43)
        2026-10-07 12:33:25.194 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 43MB AllocSpace bytes, 354(28MB) LOS objects, 5% free, 124MB/131MB, paused 747us,11.060ms total 134.029ms
        2026-10-07 12:33:25.444 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 25MB AllocSpace bytes, 209(17MB) LOS objects, 36% free, 82MB/130MB, paused 725us,8.091ms total 250.131ms
        2026-10-07 12:33:25.584 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:25.584 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:25.584 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:25.584 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:25.586 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:25.591 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:25.591 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:25.591 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:25.591 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:25.591 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:25.591 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:25.591 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:25.591 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:25.592 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:25.592 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:25.593 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:25.598 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:25.598 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:25.598 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:25.598 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:25.599 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:25.599 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:25.724 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 3826.0
        2026-10-07 12:33:25.724 11133-11994 log                     com.example.aquascope                E  Listening........ (0.10, 44)
        2026-10-07 12:33:25.762 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 39MB AllocSpace bytes, 329(26MB) LOS objects, 0% free, 181MB/181MB, paused 541us,9.303ms total 125.568ms
        2026-10-07 12:33:26.036 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:26.036 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:26.037 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:26.037 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:26.038 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:26.043 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:26.043 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:26.044 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:26.044 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:26.044 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 61MB AllocSpace bytes, 497(37MB) LOS objects, 36% free, 85MB/133MB, paused 660us,8.316ms total 281.111ms
        2026-10-07 12:33:26.044 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:26.044 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:26.044 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:26.044 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:26.045 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:26.045 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:26.046 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:26.051 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:26.051 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:26.051 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:26.051 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:26.052 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:26.052 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:26.140 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 9398.0
        2026-10-07 12:33:26.140 11133-11994 log                     com.example.aquascope                E  Listening........ (0.10, 45)
        2026-10-07 12:33:26.227 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 40MB AllocSpace bytes, 332(26MB) LOS objects, 4% free, 127MB/133MB, paused 507us,8.844ms total 138.221ms
        2026-10-07 12:33:26.534 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:26.534 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:26.535 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:26.535 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:26.535 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 27MB AllocSpace bytes, 223(17MB) LOS objects, 36% free, 84MB/132MB, paused 981us,15.196ms total 307.560ms
        2026-10-07 12:33:26.537 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:26.541 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:26.541 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:26.542 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:26.542 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:26.542 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:26.542 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:26.542 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:26.542 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:26.543 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:26.543 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:26.544 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:26.549 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:26.549 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:26.549 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:26.549 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:26.550 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:26.550 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:26.593 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 18606.0
        2026-10-07 12:33:26.593 11133-11994 log                     com.example.aquascope                E  Listening........ (0.00, 46)
        2026-10-07 12:33:26.687 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 33MB AllocSpace bytes, 278(22MB) LOS objects, 36% free, 84MB/132MB, paused 424us,10.707ms total 99.988ms
        2026-10-07 12:33:26.989 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=no preference, reason=boost timeout, vri=VRI[MainActivity]@17e0af0
        2026-10-07 12:33:27.008 11133-11133 log                     com.example.aquascope                D  onstop
        2026-10-07 12:33:27.009 11133-11133 thread                  com.example.aquascope                E  halting......
        2026-10-07 12:33:27.009 11133-11133 log                     com.example.aquascope                E  start startmethod from startWrapper
        2026-10-07 12:33:27.012 11133-11133 log                     com.example.aquascope                E  startMethod: manual veto selected ACOUSTIC; sending on the acoustic channel only
        2026-10-07 12:33:27.020 11133-12015 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/5_1791356607016 Alice-AdaptParams-0.txt false
        2026-10-07 12:33:27.021 11133-12015 log                     com.example.aquascope                E  appendtofile Alice-Sounding-log.txt false
        2026-10-07 12:33:27.022 11133-12015 log                     com.example.aquascope                E  appendtofile Alice-Data-log.txt false
        2026-10-07 12:33:27.023 11133-12015 log                     com.example.aquascope                E  appendtofile Alice-Battery_Level-log.txt false
        2026-10-07 12:33:27.032 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:27.032 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:27.032 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:27.032 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:27.034 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:27.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:27.039 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:27.040 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:27.040 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:27.040 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:27.040 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:27.040 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for input and output arrays
        2026-10-07 12:33:27.040 11133-11994 FFT_DEBUG               com.example.aquascope                D  Initializing input and output arrays
        2026-10-07 12:33:27.041 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from Java array to input array
        2026-10-07 12:33:27.041 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating FFTW plan
        2026-10-07 12:33:27.042 11133-11994 FFT_DEBUG               com.example.aquascope                D  Executing FFTW plan
        2026-10-07 12:33:27.047 11133-11994 FFT_DEBUG               com.example.aquascope                D  Allocating memory for result arrays
        2026-10-07 12:33:27.047 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data from output array to result arrays
        2026-10-07 12:33:27.047 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java arrays to hold the results
        2026-10-07 12:33:27.047 11133-11994 FFT_DEBUG               com.example.aquascope                D  Copying data to Java arrays
        2026-10-07 12:33:27.048 11133-11994 FFT_DEBUG               com.example.aquascope                D  Creating Java object array to hold the result arrays
        2026-10-07 12:33:27.048 11133-11994 FFT_DEBUG               com.example.aquascope                D  Cleaning up
        2026-10-07 12:33:27.142 11133-11998 thread                  com.example.aquascope                E  recording thread end elegantly......
        2026-10-07 12:33:27.243 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 25MB AllocSpace bytes, 221(18MB) LOS objects, 0% free, 133MB/133MB, paused 1.511ms,3.036ms total 145.159ms
        2026-10-07 12:33:27.292 11133-11994 log                     com.example.aquascope                E  xcorr_out-1.0 21164.0
        2026-10-07 12:33:27.294 11133-11994 log                     com.example.aquascope                E  Listening........ (0.14, 47)
        2026-10-07 12:33:27.296 11133-11994 System.err              com.example.aquascope                W  java.lang.InterruptedException
        2026-10-07 12:33:27.296 11133-11994 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Native Method)
        2026-10-07 12:33:27.297 11133-11994 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep0(Thread.java:689)
        2026-10-07 12:33:27.298 11133-11994 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Thread.java:667)
        2026-10-07 12:33:27.298 11133-11994 System.err              com.example.aquascope                W  	at java.lang.Thread.sleep(Thread.java:580)
        2026-10-07 12:33:27.299 11133-11994 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.OfflineRecorder.get_FIFO(OfflineRecorder.java:134)
        2026-10-07 12:33:27.300 11133-11994 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.Utils.waitForData(Utils.java:2158)
        2026-10-07 12:33:27.300 11133-11994 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.work(SendChirpAsyncTask.java:672)
        2026-10-07 12:33:27.301 11133-11994 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.doInBackground(SendChirpAsyncTask.java:230)
        2026-10-07 12:33:27.301 11133-11994 System.err              com.example.aquascope                W  	at com.example.root.ffttest2.SendChirpAsyncTask.doInBackground(SendChirpAsyncTask.java:34)
        2026-10-07 12:33:27.301 11133-11994 System.err              com.example.aquascope                W  	at android.os.AsyncTask$3.call(AsyncTask.java:396)
        2026-10-07 12:33:27.301 11133-11994 System.err              com.example.aquascope                W  	at java.util.concurrent.FutureTask.run(FutureTask.java:328)
        2026-10-07 12:33:27.301 11133-11994 System.err              com.example.aquascope                W  	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1100)
        2026-10-07 12:33:27.301 11133-11994 System.err              com.example.aquascope                W  	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
        2026-10-07 12:33:27.301 11133-11994 System.err              com.example.aquascope                W  	at java.lang.Thread.run(Thread.java:1572)
        2026-10-07 12:33:27.507 11133-11141 ample.aquascope         com.example.aquascope                I  Background concurrent mark compact GC freed 42MB AllocSpace bytes, 341(26MB) LOS objects, 36% free, 84MB/132MB, paused 267us,19.605ms total 262.994ms
        2026-10-07 12:33:28.736 11133-12015 log                     com.example.aquascope                D  use codebook 4096 do nothing
        2026-10-07 12:33:28.736 11133-12015 log                     com.example.aquascope                E  Sender_Latency_Str: 2026-10-07 12:33:27.018
        LoRa
        sender encode image (ms): 1677
        2026-10-07 12:33:28.736 11133-12015 log                     com.example.aquascope                E  send embedding: [168, 3276, 3765, 2485, 933, 2553, 2972, 2543, 1666, 1209, 1497, 554, 533, 4085, 4089, 3718, 702, 3483, 1958, 3027, 2371, 3751, 3302, 2371, 338, 222, 3914, 1195, 3957, 2044, 956, 821, 552, 2415, 880, 2953, 2082, 712, 385, 3082, 2140, 3844, 3635, 989, 1363, 2513, 3335, 2918, 3977, 256, 2866, 1576, 3522, 557, 2195, 2997, 371, 3375, 1809, 3275, 2021, 3361, 359, 2241]
        2026-10-07 12:33:28.737 11133-12015 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/5_1791356607016 Alice-Send_Embedding_Sequence-0.txt false
        2026-10-07 12:33:28.737 11133-12015 log                     com.example.aquascope                D  use codebook 4096 do nothing
        2026-10-07 12:33:32.279 11133-12015 log                     com.example.aquascope                E  Sender_Latency_Str: 2026-10-07 12:33:27.018
        LoRa
        sender encode image (ms): 1677
        sender encode signal (ms): 2
        2026-10-07 12:33:32.280 11133-12015 log                     com.example.aquascope                E  all_sent_symbol =>9,9,17,13,9,21,21,5,23,28,16,1,3,9,16,15,2,3,12,12,23,9,26,24,23,21,27,18,23,0,27,14,10,7,15,13,26,0,27,26,25,29,24,16,5,26,26,7,27,25,19,11,17,31,9,31,25,23,18,26,27,31,17,18,8,27,28,13,3,6,24,31,26,3,22,10,17,29,10,11,26,9,13,14,16,24,28,31,14,11,31,26,30,28,9,13,21,12,5,19,12,25,1,14,12,30,18,28,22,25,28,27,17,8,31,17,15,16,13,28,17,20,10,7,31,15,4,25,14,1,15,8,24,8,6,24,26,28,0,20,26,0,8,3,25,15,2,21,30,31,0,4,7,31,1,29,3,3,16,31,16,14,0,15,1,18,8,24,5,18,29,31,4,26,30,5,29,17,10,10,10,11,8,24,5,16,3,3,26,8,16,9,1,28,2,10,29,12,11,9,2,4,6,9,26,0,10,21,4,3,31,16,28,7,14,3,3,22,21,21,28,13,30,26,17,4,15,17,2,6,18,9,30,1,24,22,1,9,5,2,1,24,23,7,24,6,29,14,17,29,12,2,16,4,0,3,19,21,30,21,2,30,2,20,8,26,16,21,16,24,30,19,22,15,
        2026-10-07 12:33:32.280 11133-12015 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/5_1791356607016 Alice-Sent_Symbols-0.txt false
        2026-10-07 12:33:32.284 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.291 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.296 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.300 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.311 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.315 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.319 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.323 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.327 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.331 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.335 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.339 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.343 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.348 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.352 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.356 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.360 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.364 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.368 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.372 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.376 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.380 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.384 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.388 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.392 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.396 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.400 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.404 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.408 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.412 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.416 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.421 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.425 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.429 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.433 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.437 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.441 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.445 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.449 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.453 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.457 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.461 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.465 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.468 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.471 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.473 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.476 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.479 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.481 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.484 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.486 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.488 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.491 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.493 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.496 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.498 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.500 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.503 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.506 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.508 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.510 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.513 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.516 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.518 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.521 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.523 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.525 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.528 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.531 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.533 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.536 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.538 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.541 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.543 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.546 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.548 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.551 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.553 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.556 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.558 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.561 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.563 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.566 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.568 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.570 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.572 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.572 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.573 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.574 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.574 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.575 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.575 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.576 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.576 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.577 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.578 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.578 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.579 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.580 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.580 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.581 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.582 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.582 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.583 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.583 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.584 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.585 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.585 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.586 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.587 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.587 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.588 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.589 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.589 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.590 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.591 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.591 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.592 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.592 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.593 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.593 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.594 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.595 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.595 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.596 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.597 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.597 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.598 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.598 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.599 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.600 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.600 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.601 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.601 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.601 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.602 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.602 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.602 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.603 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.603 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.603 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.603 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.604 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.604 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.604 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.605 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.605 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.605 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.606 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.606 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.606 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.607 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.607 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.607 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.607 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.608 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.608 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.608 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.609 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.609 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.609 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.610 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.610 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.610 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.610 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.611 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.611 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.611 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.612 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.612 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.612 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.613 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.613 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.613 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.613 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.614 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.614 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.614 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.615 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.615 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.615 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.616 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.616 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.616 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.616 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.617 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.617 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.617 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.618 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.618 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.618 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.619 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.619 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.619 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.619 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.620 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.620 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.620 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.621 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.621 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.621 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.622 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.622 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.622 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.622 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.623 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.623 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.623 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.624 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.624 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.624 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.625 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.625 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.625 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.625 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.626 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.626 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.626 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.627 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.627 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.627 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.628 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.628 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.628 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.629 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.629 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.629 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.629 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.630 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.630 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.630 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.631 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.631 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.631 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.631 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.632 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.632 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.633 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.633 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.634 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.634 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.634 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.635 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.635 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.635 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.636 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.636 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.636 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.637 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.637 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.637 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.637 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.638 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.638 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.638 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.639 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.639 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.639 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.640 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.640 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.640 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.641 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.641 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.641 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.642 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.642 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.642 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.643 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.643 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.643 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.644 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.644 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.644 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.645 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.645 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.645 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.646 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.647 11133-12015 log                     com.example.aquascope                D  send linear
        2026-10-07 12:33:32.647 11133-12015 log                     com.example.aquascope                E  Sender_Latency_Str: 2026-10-07 12:33:27.018
        LoRa
        sender encode image (ms): 1677
        sender encode signal (ms): 2
        sender generate signal (ms): 366
        2026-10-07 12:33:32.647 11133-12015 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/5_1791356607016,Alice-DataChirp-0.txt false
        2026-10-07 12:33:32.711 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 41MB AllocSpace bytes, 6(536KB) LOS objects, 29% free, 94MB/132MB, paused 631us,9.052ms total 73.121ms
        2026-10-07 12:33:32.830 11133-12015 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:33:32.840 11133-12015 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:33:32.841 11133-12015 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:33:32.842 11133-12015 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:33:32.863 11133-12015 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:33:32.864 11133-12015 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:33:32.865 11133-12015 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:33:32.868 11133-12015 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:33:32.868 11133-12015 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:33:32.876 11133-12015 AudioManager            com.example.aquascope                W  setStreamMute is deprecated. adjustStreamVolume should be used instead.
        2026-10-07 12:33:32.914 11133-12015 AudioTrack              com.example.aquascope                W  Use of stream types is deprecated for operations other than volume control
        2026-10-07 12:33:32.914 11133-12015 AudioTrack              com.example.aquascope                W  See the documentation of AudioTrack() for what to use instead with android.media.AudioAttributes to qualify your playback use case
        2026-10-07 12:33:32.915 11133-12015 AudioTrack              com.example.aquascope                D  setVolume(0.600000, 0.600000) pid : 11133
        2026-10-07 12:33:39.205 11133-12015 asdf                    com.example.aquascope                E  writing sensors 314,3144,Alice-Sender_Sensor-0.txt
        2026-10-07 12:33:39.208 11133-12015 asdf                    com.example.aquascope                E  finish writing sensors 3
        2026-10-07 12:33:39.208 11133-12015 log                     com.example.aquascope                D  Sender_Latency_Str: 2026-10-07 12:33:27.018
        LoRa
        sender encode image (ms): 1677
        sender encode signal (ms): 2
        sender generate signal (ms): 366
        sender send signal (ms): 6290
        2026-10-07 12:33:39.208 11133-12015 log                     com.example.aquascope                E  writetofile /storage/emulated/0/Android/data/com.example.aquascope/files/5_1791356607016 Alice-Latency_Sender-0.txt false
        2026-10-07 12:33:42.209 11133-11133 log                     com.example.aquascope                E  Bob is the receiver; starting the acoustic listen loop
        2026-10-07 12:38:58.427 11133-11141 ample.aquascope         com.example.aquascope                I  Background young concurrent mark compact GC freed 34MB AllocSpace bytes, 4(80KB) LOS objects, 35% free, 86MB/132MB, paused 574us,14.120ms total 125.312ms
        2026-10-07 12:44:22.809 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:44:22.811 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=high hint, reason=touch, vri=VRI[MainActivity]@17e0af0
        2026-10-07 12:44:23.102 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:44:23.661 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 12:44:23.795 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 12:44:26.796 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=no preference, reason=boost timeout, vri=VRI[MainActivity]@17e0af0
        2026-10-07 12:58:26.649 11133-11141 ample.aquascope         com.example.aquascope                I  NativeAlloc concurrent mark compact GC freed 21MB AllocSpace bytes, 8(2832KB) LOS objects, 36% free, 83MB/131MB, paused 928us,13.406ms total 364.449ms
        2026-10-07 12:58:26.649 11133-11141 ample.aquascope         com.example.aquascope                W  Reducing the number of considered missed Gc histogram windows from 117 to 100
        2026-10-07 13:17:05.794 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 0
        2026-10-07 13:17:05.795 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=high hint, reason=touch, vri=VRI[MainActivity]@17e0af0
        2026-10-07 13:17:05.942 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  ViewPostIme pointer 1
        2026-10-07 13:17:08.943 11133-11133 VRI[MainAc...y]@17e0af0 com.example.aquascope                I  call setFrameRateCategory for touch hint category=no preference, reason=boost timeout, vri=VRI[MainActivity]@17e0af0
        2026-10-07 13:18:18.896 11133-11141 ample.aquascope         com.example.aquascope                I  NativeAlloc concurrent mark compact GC freed 21MB AllocSpace bytes, 0(0B) LOS objects, 36% free, 83MB/131MB, paused 671us,8.236ms total 283.302ms
        2026-10-07 13:18:18.896 11133-11141 ample.aquascope         com.example.aquascope                W  Reducing the number of considered missed Gc histogram windows from 119 to 100

        int w = small.getWidth();
        int h = small.getHeight();
        int[] pixels = new int[w * h];
        small.getPixels(pixels, 0, w, 0, 0, w, h);

        // Greyscale plane, computed once and reused by both metrics.
        float[] grey = new float[w * h];
        double sum = 0.0;
        for (int i = 0; i < pixels.length; i++) {
            int c = pixels[i];
            float v = (0.299f * Color.red(c) + 0.587f * Color.green(c) + 0.114f * Color.blue(c)) / 255f;
            grey[i] = v;
            sum += v;
        }
        float meanLuminance = (float) (sum / pixels.length);

        // Variance of the Laplacian over the interior (border pixels have no
        // full 3x3 neighbourhood).
        double lapSum = 0.0;
        double lapSumSq = 0.0;
        int n = 0;
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                float v = laplacian(grey, w, h, x, y);
                lapSum += v;
                lapSumSq += v * v;
                n++;
            }
        }
        double mean = (n == 0) ? 0.0 : lapSum / n;
        double variance = (n == 0) ? 0.0 : (lapSumSq / n) - (mean * mean);
        if (variance < 0) variance = 0;
        float laplacianVariance = (float) variance;

        // Primary signal: blur -> turbidity.
        float sharpness = laplacianVariance / RouterConfig.TURBIDITY_BASELINE_VARIANCE;
        if (sharpness > 1f) sharpness = 1f;
        if (sharpness < 0f) sharpness = 0f;
        float turbidity = 1f - sharpness;

        // A frame this flat is not "turbid water", it is a dead or mispointed
        // camera. Treat it as unusable either way, but say which so the log
        // distinguishes a calibration problem from a real optical veto.
        if (laplacianVariance < RouterConfig.TURBIDITY_FLOOR_VARIANCE) {
            Utils.logd("Turbidity: variance below floor (" + laplacianVariance
                    + " < " + RouterConfig.TURBIDITY_FLOOR_VARIANCE
                    + "), frame has no usable detail - check camera or recalibrate baseline");
        }

        // Secondary signal: darkness also means an unusable optical path.
        float darkness = 0f;
        if (meanLuminance < RouterConfig.DARKNESS_LUMINANCE_THRESHOLD) {
            darkness = RouterConfig.DARKNESS_TURBIDITY_WEIGHT
                    * (1f - meanLuminance / RouterConfig.DARKNESS_LUMINANCE_THRESHOLD);
        }
        float combined = turbidity + darkness;
        if (combined > 1f) combined = 1f;

        Utils.logd("Turbidity: variance=" + laplacianVariance
                + " meanLum=" + meanLuminance
                + " turbidity=" + combined);

        recycle(small);
        if (crop != frame) recycle(crop);

        return new ContextVector(combined, prevNoise, meanLuminance, laplacianVariance);
    }

    private static float laplacian(float[] g, int w, int h, int x, int y) {
        int i = y * w + x;
        // 4-neighbour Laplacian kernel.
        return 4f * g[i] - g[i - 1] - g[i + 1] - g[i - w] - g[i + w];
    }

    private static Bitmap centreCropToSquare(Bitmap b) {
        int w = b.getWidth();
        int h = b.getHeight();
        int side = Math.min(w, h);
        int x = (w - side) / 2;
        int y = (h - side) / 2;
        if (side == w && side == h) return b;
        return Bitmap.createBitmap(b, x, y, side, side);
    }

    private static void recycle(Bitmap b) {
        if (b != null && !b.isRecycled()) b.recycle();
    }
}
