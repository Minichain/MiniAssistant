package com.minichain.miniassistant.battery

import android.content.Context
import android.os.BatteryManager
import com.minichain.miniassistant.bridge.DataBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BatteryTrackingService(
  private val context: Context,
  private val scope: CoroutineScope
) {

  private val batteryManager by lazy {
    context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
  }

  private var tracking = false
  private val trackingPeriod = 10000L

  fun start() {
    val file = File(context.filesDir, "battery_tests/battery_level_${getFormattedDate("yyyy_MM_dd_HH_mm")}.txt")
    file.parentFile?.mkdirs()
    file.writeText("/** BATTERY TRACKER STARTED **/\n")
    scope.launch {
      tracking = true
      while (tracking) {
        val currentCapacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        DataBridge.addConsoleLine("${getFormattedDate("yyyy/MM/dd HH:mm")}: Battery level: $currentCapacity %")
        file.appendText("${getFormattedDate("yyyy/MM/dd HH:mm")}: Battery level: $currentCapacity %\n")
        delay(trackingPeriod)
      }
    }
  }

  private fun getFormattedDate(pattern: String): String {
    val current = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return current.format(formatter)
  }

  fun stop() {
    tracking = false
  }
}
