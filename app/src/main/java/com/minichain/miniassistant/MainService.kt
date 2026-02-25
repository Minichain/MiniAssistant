package com.minichain.miniassistant

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat

class MainService : Service() {

  companion object {
    const val CHANNEL_ID: String = "notification_channel"
  }

  private val picovoiceService = PicovoiceService(this)

  enum class Action {
    Start, Stop
  }

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    println("MAIN_SERVICE: onStartCommand()")
    when (intent?.action) {
      Action.Start.toString() -> start()
      Action.Stop.toString() -> stop()
    }
    return super.onStartCommand(intent, flags, startId)
  }

  private fun start() {
    println("MAIN_SERVICE: start()")
    val notification = NotificationCompat.Builder(this, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setContentTitle("Mini Assistant is active")
      .setContentText("Mini Assistant debug text")
      .build()
    startForeground(1, notification)

    picovoiceService.start()
  }

  private fun stop() {
    stopSelf()
  }
}
