package com.minichain.miniassistant

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.minichain.miniassistant.MainService.Companion.CHANNEL_ID

class MiniApp : Application() {
  override fun onCreate() {
    super.onCreate()
    val notificationChannel = NotificationChannel(
      CHANNEL_ID,
      "Notifications",
      NotificationManager.IMPORTANCE_HIGH
    )
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(notificationChannel)
  }
}