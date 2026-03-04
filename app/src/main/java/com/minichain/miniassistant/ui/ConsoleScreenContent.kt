package com.minichain.miniassistant.ui

import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.minichain.miniassistant.bridge.DataBridge
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun ConsoleScreenContent() {

  var consoleEventsList: List<String> by remember { mutableStateOf(emptyList()) }
  val listState = rememberLazyListState()

  Surface(
    modifier = Modifier.fillMaxSize(),
  ) {
    LazyColumn(
      modifier = Modifier
        .padding(12.dp)
        .fillMaxSize(),
      state = listState
    ) {
      consoleEventsList.forEach { consoleMessage ->
        item {
          Text(text = consoleMessage)
        }
      }
    }
  }

  LaunchedEffect(Unit) {
    DataBridge.consoleLines
      .filter { it.isNotEmpty() }
      .onEach { consoleLines ->
        consoleEventsList = consoleLines
        if (consoleLines.size > 1) {
          listState.animateScrollToItem(consoleLines.size - 1)
        }
      }
      .launchIn(this)
  }
}