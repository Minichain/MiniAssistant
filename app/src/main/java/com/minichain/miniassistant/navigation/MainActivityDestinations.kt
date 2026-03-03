package com.minichain.miniassistant.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.minichain.miniassistant.ui.MainPageContent
import kotlinx.serialization.Serializable

fun NavGraphBuilder.mainActivityDestinations(navigation: CustomNavigation) {
  composable<MainPage> { navBackStackEntry ->
    MainPageContent(
      screen = enumValueOf<MainPageScreen>(navBackStackEntry.toRoute<MainPage>().screen)
    )
  }
}

@Serializable
data class MainPage(
  val screen: String
)
