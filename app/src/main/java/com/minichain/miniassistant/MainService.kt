package com.minichain.miniassistant

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.Log
import com.minichain.miniassistant.assistant.AssistantService
import com.minichain.miniassistant.battery.BatteryTrackingService
import com.minichain.miniassistant.video.VideoService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MainService : Service() {

  companion object {
    const val CHANNEL_ID: String = "notification_channel"
    const val NOTIFICATION_ID = 1
    const val DISMISSAL_NOTIFICATION_ACTION_NAME = "mini_assistant_notification_dismissed"
  }

  private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
  private val assistantService: AssistantService by lazy { AssistantService(this, scope) }
  private val batteryTrackingService: BatteryTrackingService by lazy { BatteryTrackingService(this, scope) }
  private val videoService: VideoService by lazy { VideoService(this, scope) }

  private lateinit var powerManager: PowerManager
  private lateinit var wakeLock: WakeLock

  enum class Action {
    StartService, StopService
  }

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Log.d("MAIN_SERVICE", "onStartCommand()")
    when (intent?.action) {
      Action.StartService.toString() -> start()
      Action.StopService.toString() -> stop()
    }
    return super.onStartCommand(intent, flags, startId)
  }

  private fun start() {
    Log.d("MAIN_SERVICE", "start()")
    registerReceiver(notificationDismissalReceiver, IntentFilter(DISMISSAL_NOTIFICATION_ACTION_NAME), RECEIVER_NOT_EXPORTED)
    startForeground(NOTIFICATION_ID, getNotification(applicationContext))
    powerManager = (getSystemService(Context.POWER_SERVICE) as PowerManager)
    powerManager.run {
      wakeLock = newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "${this@MainService.javaClass.name}::MiniAssistantWakeLock").apply {
        acquire()
      }
    }
    assistantService.start()
    batteryTrackingService.start()
    videoService.start()
  }

  private val notificationDismissalReceiver = object : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
      startForeground(NOTIFICATION_ID, getNotification(applicationContext))
    }
  }

  private fun stop() {
    stopSelf()
  }

  override fun onDestroy() {
    Log.d("MAIN_SERVICE","Destroying MainService...")
    unregisterReceiver(notificationDismissalReceiver)
    wakeLock.release()
    assistantService.stop()
    batteryTrackingService.stop()
    super.onDestroy()
  }
}
