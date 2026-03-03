package com.minichain.miniassistant.navigation

import androidx.navigation.NavHostController

typealias CustomNavigation = (action: NavHostController.() -> Unit) -> Unit

fun NavHostController.replaceCurrentDestinationTo(destination: Any) {
  navigate(destination) {
    currentDestination?.route?.let {
      popUpTo(it) {
        inclusive = true
      }
    }
    restoreState = true
    launchSingleTop = true
  }
}

fun NavHostController.navigateTo(destination: Any) {
  navigate(destination) {
    restoreState = true
    launchSingleTop = true
  }
}
