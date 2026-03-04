package com.minichain.miniassistant.ui

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.minichain.miniassistant.bridge.DataBridge
import com.minichain.miniassistant.bridge.Event
import com.minichain.miniassistant.video.VideoStatus
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Composable
fun VideoScreenContent() {
  Surface(
    modifier = Modifier.fillMaxSize(),
  ) {
    Box(
      modifier = Modifier.fillMaxSize()
    ) {

      var videoStatus: VideoStatus? by remember { mutableStateOf(null) }
      val scope = rememberCoroutineScope()

      LaunchedEffect(Unit) {
        DataBridge.videoStatus
          .onEach { videoStatus = it }
          .launchIn(this)
      }

      IconButton(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(12.dp),
        onClick = {
          Log.d("VIDEO_SCREEN", "Start/stop video button onClick")
          scope.launch {
            when (videoStatus) {
              VideoStatus.Stopped -> DataBridge.events.emit(Event.StartVideo)
              VideoStatus.Started -> DataBridge.events.emit(Event.StopVideo)
              else -> { /* Nothing */ }
            }
          }
        },
        enabled = videoStatus != null
      ) {
        Icon(
          imageVector = when (videoStatus) {
            VideoStatus.Stopped -> Icons.Default.PlayArrow
            VideoStatus.Started -> Icons.Default.Stop
            else -> Icons.Default.PlayArrow
          },
          contentDescription = "Start/stop video"
        )
      }
    }
  }
}
