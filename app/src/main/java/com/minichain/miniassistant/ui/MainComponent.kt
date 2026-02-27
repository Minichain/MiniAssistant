package com.minichain.miniassistant.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
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

@Composable
fun MainComponent(
  modifier: Modifier
) {
  Box(
    modifier = modifier.fillMaxSize()
  ) {

    Column(
      modifier = Modifier.fillMaxSize()
    ) {
      AssistantState(
        modifier = Modifier
          .fillMaxWidth()
          .weight(0.2f)
      )

      Notes(
        modifier = Modifier
          .fillMaxWidth()
          .weight(0.4f)
      )

      Console(
        modifier = Modifier
          .fillMaxWidth()
          .weight(0.4f)
      )
    }
  }
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

@Composable
private fun Notes(
  modifier: Modifier
) {

  Surface(
    modifier = modifier,
    shadowElevation = 8.dp
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

@Composable
private fun Console(
  modifier: Modifier
) {

  var consoleEventsList: List<Event.ConsoleEvent> by remember { mutableStateOf(emptyList()) }
  val listState = rememberLazyListState()

  Surface(
    modifier = modifier,
    shadowElevation = 8.dp
  ) {
    LazyColumn(
      modifier = Modifier
        .padding(12.dp)
        .fillMaxSize(),
      state = listState
    ) {
      consoleEventsList.forEach { consoleEvent ->
        item {
          Text(consoleEvent.message)
        }
      }
    }
  }

  LaunchedEffect(Unit) {
    DataBridge.events
      .filterIsInstance<Event.ConsoleEvent>()
      .onEach {
        println("MAIN_ACTIVITY: ConsoleEvent: ${it.message}")
        val newList = consoleEventsList.toMutableList()
        newList.add(it)
        consoleEventsList = newList
        listState.animateScrollToItem(newList.size - 1)
      }
      .launchIn(this)
  }
}
