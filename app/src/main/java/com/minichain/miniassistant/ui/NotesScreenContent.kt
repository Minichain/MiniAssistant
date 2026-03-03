package com.minichain.miniassistant.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minichain.miniassistant.bridge.DataBridge
import com.minichain.miniassistant.bridge.Event
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun NotesScreenContent() {
  Surface(
    modifier = Modifier.fillMaxSize(),
  ) {
    Column(
      modifier = Modifier
        .padding(12.dp)
        .fillMaxSize()
    ) {

      var note: String by remember { mutableStateOf("") }

      Text(
        modifier = Modifier
          .fillMaxWidth()
          .weight(0.2f),
        fontSize = 20.sp,
        text = "Notes"
      )
      Text(
        modifier = Modifier
          .fillMaxWidth()
          .weight(0.8f),
        text = note
      )

      LaunchedEffect(Unit) {
        DataBridge.events
          .filterIsInstance<Event.NoteTakenEvent>()
          .onEach { note = it.note }
          .launchIn(this)
      }
    }
  }
}
