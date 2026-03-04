package com.minichain.miniassistant.video

import android.content.Context
import android.util.Log
import com.minichain.miniassistant.bridge.DataBridge
import com.minichain.miniassistant.bridge.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class VideoService(
  private val context: Context,
  private val scope: CoroutineScope
) {

  private var videoStarted: Boolean = false

  private val cameraManager by lazy {
    CameraManager(context)
  }

  fun start() {
    scope.launch(Dispatchers.Main) {
      DataBridge.events
        .filterIsInstance<Event.StartVideo>()
        .distinctUntilChanged()
        .onEach {
          Log.d("VIDEO_SERVICE", "Starting video...")
          videoStarted = true
          DataBridge.updateVideoStatus(VideoStatus.Started)
          cameraManager.openCameraAndStartSession()
        }
        .launchIn(this)

      DataBridge.events
        .filterIsInstance<Event.StopVideo>()
        .onEach {
          Log.d("VIDEO_SERVICE", "Stopping video...")
          cameraManager.stopSessionAndCloseCamera()
          DataBridge.updateVideoStatus(VideoStatus.Stopped)
          videoStarted = false
        }
        .launchIn(this)
    }
  }


}