package com.minichain.miniassistant

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.minichain.miniassistant.permissions.PermissionsHandler
import com.minichain.miniassistant.ui.MainComponent
import com.minichain.miniassistant.ui.theme.MiniAssistantTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {

      PermissionsHandler(
        activity = this,
        onAllPermissionsGranted = {
          startMainService()
        }
      )

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

  private fun startMainService() {
    Intent(applicationContext, MainService::class.java).also {
      it.action = MainService.Action.Start.toString()
      startService(it)
    }
  }
}
