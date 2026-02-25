package com.minichain.miniassistant

import android.Manifest
import android.content.Intent
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.minichain.miniassistant.ui.theme.MiniAssistantTheme
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    ActivityCompat.requestPermissions(
      this,
      arrayOf(
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.RECORD_AUDIO
      ),
      0
    )

    setContent {

      Intent(applicationContext, MainService::class.java).also {
        it.action = MainService.Action.Start.toString()
        startService(it)
      }

      MiniAssistantTheme {

        val context = LocalContext.current
        var consoleEventsList: List<Event.ConsoleEvent> by remember { mutableStateOf(emptyList()) }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            LazyColumn(
              modifier = Modifier.fillMaxSize()
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
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(
    text = "Hello $name!",
    modifier = modifier
  )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MiniAssistantTheme {
    Greeting("Android")
  }
}