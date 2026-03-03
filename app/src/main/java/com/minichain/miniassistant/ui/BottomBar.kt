package com.minichain.miniassistant.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Note
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.toRoute
import com.minichain.miniassistant.navigation.CustomNavigation
import com.minichain.miniassistant.navigation.MainPage
import com.minichain.miniassistant.navigation.MainPageScreen
import com.minichain.miniassistant.navigation.replaceCurrentDestinationTo
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun BottomBar(
  navigation: CustomNavigation,
  navController: NavHostController
) {
  var selectedTab: String? by remember { mutableStateOf(null) }

  LaunchedEffect(Unit) {
    navController.currentBackStackEntryFlow
      .onEach {
        selectedTab = when {
          it.destination.hasRoute<MainPage>() -> it.toRoute<MainPage>().screen
          else -> null
        }
      }
      .launchIn(this)
  }

  BottomBarContent(
    selectedTab = selectedTab,
    onClick = { destination ->
      navigation { replaceCurrentDestinationTo(destination) }
    }
  )
}

@Composable
private fun BottomBarContent(
  selectedTab: String?,
  onClick: (destination: Any) -> Unit
) {
  NavigationBar {
    BottomBarItem.entries.forEach { item ->
      NavigationBarItem(
        selected = selectedTab == item.screen.name,
        icon = { Icon(imageVector = item.icon, contentDescription = null) },
        label = { Text(text = item.title, overflow = TextOverflow.Ellipsis, maxLines = 1) },
        alwaysShowLabel = false,
        onClick = { onClick(MainPage(item.screen.name)) }
      )
    }
  }
}

private enum class BottomBarItem(
  val screen: MainPageScreen,
  val icon: ImageVector,
  val title: String
) {
  Console(MainPageScreen.Console, Icons.Outlined.Computer, "Console"),
  Camera(MainPageScreen.Video, Icons.Outlined.Videocam, "Video"),
  Notes(MainPageScreen.Notes, Icons.AutoMirrored.Outlined.Note, "Notes")
}

@Preview
@Composable
private fun BottomBarPreview() {
  BottomBarContent(
    selectedTab = MainPageScreen.Console.name,
    onClick = {}
  )
}
