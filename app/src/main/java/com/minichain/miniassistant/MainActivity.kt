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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.minichain.miniassistant.ui.BottomBar
import com.minichain.miniassistant.navigation.CustomNavigation
import com.minichain.miniassistant.navigation.MainPage
import com.minichain.miniassistant.navigation.MainPageScreen
import com.minichain.miniassistant.navigation.mainActivityDestinations
import com.minichain.miniassistant.permissions.PermissionsHandler
import com.minichain.miniassistant.ui.TopBar
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

      val navController: NavHostController = rememberNavController()
      val navigation: CustomNavigation = { action -> navController.action() }

      MiniAssistantTheme {
        Scaffold(
          modifier = Modifier.fillMaxSize(),
          topBar = { TopBar() },
          bottomBar = { BottomBar(navigation, navController) }
        ) { innerPadding ->
          NavHost(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
            startDestination = MainPage(MainPageScreen.Console.toString())
          ) {
            mainActivityDestinations(navigation)
          }
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
