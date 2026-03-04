package com.minichain.miniassistant.video

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Range
import android.view.Surface
import androidx.core.app.ActivityCompat
import com.minichain.miniassistant.getFormattedDate
import java.io.File

class CameraManager(
  private val context: Context
) {

  private lateinit var backgroundHandlerThread: HandlerThread
  private lateinit var backgroundHandler: Handler

  companion object {
    private const val VIDEO_FRAME_RATE = 24
    private const val VIDEO_FRAME_WIDTH = 1280
    private const val VIDEO_FRAME_HEIGHT = 720
  }

  private var camera: CameraDevice? = null
  private var session: CameraCaptureSession? = null

  /**
   * adb exec-out run-as com.minichain.miniassistant cat files/videos/video_2026_03_05_17_12.mp4 > Desktop/video_2026_03_05_17_12.mp4
   * **/

  private val mediaRecorder by lazy {
    MediaRecorder(context).apply {
//      setAudioSource(MediaRecorder.AudioSource.MIC)
      setVideoSource(MediaRecorder.VideoSource.SURFACE) // Crucial for Camera2
      setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
      setVideoEncoder(MediaRecorder.VideoEncoder.H264)
//      setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
      setVideoSize(VIDEO_FRAME_WIDTH, VIDEO_FRAME_HEIGHT)
      setVideoFrameRate(VIDEO_FRAME_RATE)
      setVideoEncodingBitRate(10000000) // 10 Mbps
    }
  }

  fun openCameraAndStartSession() {

    val file = File(context.filesDir, "videos/video_${getFormattedDate("yyyy_MM_dd_HH_mm")}.mp4")
    file.parentFile?.mkdirs()
    mediaRecorder.setOutputFile(file)

    mediaRecorder.prepare()

    backgroundHandlerThread = HandlerThread("CameraVideoThread")
    backgroundHandlerThread.start()
    backgroundHandler = Handler(backgroundHandlerThread.looper)

    val cameraManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    cameraManager.addAvailabilityCallback()

    cameraManager.getBackCameraId()?.let { backCameraId ->
      val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
          Log.d("CAMERA_MANAGER", "Camera opened")
          this@CameraManager.camera = camera
          createCaptureSession(mediaRecorder.surface)
        }

        override fun onDisconnected(camera: CameraDevice) {
          Log.d("CAMERA_MANAGER", "Camera disconnected")
        }

        override fun onError(camera: CameraDevice, error: Int) {
          Log.d("CAMERA_MANAGER", "Camera error. Error: $error")
        }
      }

      if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
        cameraManager.openCamera(backCameraId, cameraStateCallback, backgroundHandler)
      }
    }
  }

  private fun createCaptureSession(surface: Surface) {
    Log.d("CAMERA_MANAGER", "Let's create capture session")
    val surfaceTargets = listOf(surface)

    val cameraCaptureSessionCallback = object : CameraCaptureSession.StateCallback() {
      override fun onConfigured(session: CameraCaptureSession) {
        Log.d("CAMERA_MANAGER", "Camera capture session configured!")
        this@CameraManager.session = session
        val captureRequest = session.device.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
        captureRequest.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(VIDEO_FRAME_RATE, VIDEO_FRAME_RATE))
        surfaceTargets.forEach { captureRequest.addTarget(it) }
        mediaRecorder.start()
        session.setRepeatingRequest(captureRequest.build(), null, backgroundHandler)
      }

      override fun onConfigureFailed(session: CameraCaptureSession) {
        Log.d("CAMERA_MANAGER", "Camera capture session configuration failed :(")
      }

      override fun onClosed(session: CameraCaptureSession) {
        super.onClosed(session)
        Log.d("CAMERA_MANAGER", "Camera capture session closed")
      }

      override fun onSurfacePrepared(session: CameraCaptureSession, surface: Surface) {
        super.onSurfacePrepared(session, surface)
        Log.d("CAMERA_MANAGER", "Camera capture session surface prepared")
      }
    }

    Log.d("CAMERA_MANAGER", "Creating capture session...")
    camera?.createCaptureSession(surfaceTargets, cameraCaptureSessionCallback)
  }

  private fun CameraDevice.createCaptureSession(
    surfaceTargets: List<Surface>,
    cameraCaptureSessionCallback: CameraCaptureSession.StateCallback
  ) {
    /** Old approach **/
    createCaptureSession(
      surfaceTargets,
      cameraCaptureSessionCallback,
      null
    )
  }

  private fun CameraManager.getBackCameraId(): String? {
    cameraIdList.forEach { cameraId ->
      val cameraCharacteristics = getCameraCharacteristics(cameraId)
      if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
        return cameraId
      }
    }
    return null
  }

  private fun CameraManager.addAvailabilityCallback() {
    val featureCameraExternal = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_EXTERNAL)
    Log.d("CAMERA_MANAGER", "featureCameraExternal: $featureCameraExternal")

    Log.d("CAMERA_MANAGER", "Available cameras:")
    cameraIdList.forEachIndexed { index, cameraId ->
      val cameraCharacteristics = getCameraCharacteristics(cameraId)
      Log.d("CAMERA_MANAGER", "Available camera[${index}]: Lens facing: ${cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)}")
    }

    val availabilityCallback = object : CameraManager.AvailabilityCallback() {
      override fun onCameraAvailable(cameraId: String) {
        super.onCameraAvailable(cameraId)
        Log.d("CAMERA_MANAGER", "Camera available :) cameraId: $cameraId")
      }

      override fun onCameraUnavailable(cameraId: String) {
        super.onCameraUnavailable(cameraId)
        Log.d("CAMERA_MANAGER", "Camera unavailable :( cameraId: $cameraId")
      }
    }
    registerAvailabilityCallback(availabilityCallback, backgroundHandler)
  }

  fun stopSessionAndCloseCamera() {
    mediaRecorder.stop()
    mediaRecorder.reset()
    mediaRecorder.release()

    session?.stopRepeating()
    session?.close()
    camera?.close()
    backgroundHandlerThread.quitSafely()
    backgroundHandlerThread.join()
  }
}
