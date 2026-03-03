package com.minichain.miniassistant.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minichain.miniassistant.assistant.AssistantState
import com.minichain.miniassistant.bridge.DataBridge
import com.minichain.miniassistant.bridge.Event
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
  CenterAlignedTopAppBar(
    title = {
      AssistantState(modifier = Modifier)
    }
  )
}

@Composable
private fun AssistantState(
  modifier: Modifier
) {
  Box(
    modifier = modifier
  ) {

    var assistantState: AssistantState by remember { mutableStateOf(AssistantState.WakeWord) }

    Text(
      modifier = Modifier
        .align(Alignment.Center)
        .padding(12.dp),
      fontSize = 24.sp,
      text = when (assistantState) {
        AssistantState.WakeWord -> "Waiting for Wake Word"
        AssistantState.SpeechToIntent -> "What do you want me to do?"
        AssistantState.SpeechToText -> "Taking notes..."
      },
      color = when (assistantState) {
        AssistantState.WakeWord -> Color(34, 89, 49)
        AssistantState.SpeechToIntent -> Color(34, 45, 89)
        AssistantState.SpeechToText -> Color(89, 34, 34)
      }
    )

    LaunchedEffect(Unit) {
      DataBridge.events
        .filterIsInstance<Event.AssistantStateUpdate>()
        .onEach { assistantState = it.state }
        .launchIn(this)
    }
  }
}
