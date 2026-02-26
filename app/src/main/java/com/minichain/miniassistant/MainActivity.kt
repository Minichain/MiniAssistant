package com.minichain.miniassistant

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    var consoleEventsList: List<Event.ConsoleEvent> by remember { mutableStateOf(emptyList()) }
    Box(
      modifier = modifier.fillMaxSize()
    ) {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(8.dp)
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
