package com.minichain.miniassistant.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.minichain.miniassistant.navigation.MainPageScreen

@Composable
fun MainPageContent(
  screen: MainPageScreen
) {
  Box(
    modifier = Modifier.fillMaxSize()
  ) {
    when (screen) {
      MainPageScreen.Console -> ConsoleScreenContent()
      MainPageScreen.Video -> VideoScreenContent()
      MainPageScreen.Notes -> NotesScreenContent()
    }
  }
}
