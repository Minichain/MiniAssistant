package com.minichain.miniassistant

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Note
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.minichain.miniassistant.assistant.AssistantState
import com.minichain.miniassistant.bridge.DataBridge
import com.minichain.miniassistant.bridge.Event
import com.minichain.miniassistant.ui.theme.MiniAssistantTheme
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {

      HandlePermissions(this) {
        startMainService()
      }

      MiniAssistantTheme {
        Scaffold(
          modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
          MainComponent(
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }

  @Composable
  private fun MainComponent(
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
            .padding(8.dp)
        )

        Console(
          modifier = Modifier
            .fillMaxWidth()
            .weight(0.4f)
            .padding(8.dp)
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
    Column(
      modifier = modifier
    ) {

      var note: String by remember { mutableStateOf("") }

      Text(
        modifier = Modifier
          .fillMaxWidth()
          .weight(0.2f)
          .padding(8.dp),
        fontSize = 20.sp,
        text = "Notes"
      )
      Text(
        modifier = Modifier
          .fillMaxWidth()
          .weight(0.8f)
          .padding(8.dp),
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

  @Composable
  private fun Console(
    modifier: Modifier
  ) {

    var consoleEventsList: List<Event.ConsoleEvent> by remember { mutableStateOf(emptyList()) }

    LazyColumn(
      modifier = modifier
    ) {
      consoleEventsList.forEach { consoleEvent ->
        item {
          Text(consoleEvent.message)
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
        }
        .launchIn(this)
    }
  }

  @Composable
  private fun HandlePermissions(
    activity: Activity,
    onAllPermissionsGranted: () -> Unit
  ) {
    val context = LocalContext.current
    ActivityCompat.requestPermissions(
      activity,
      arrayOf(
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.RECORD_AUDIO
      ),
      0
    )
    LaunchedEffect(Unit) {
      launch {
        while (!permissionsGranted(context)) {
          // Do nothing...
        }
        onAllPermissionsGranted()
      }
    }
  }

  private fun permissionsGranted(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

  private fun startMainService() {
    Intent(applicationContext, MainService::class.java).also {
      it.action = MainService.Action.Start.toString()
      startService(it)
    }
  }
}
