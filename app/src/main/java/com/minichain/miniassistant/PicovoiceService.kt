package com.minichain.miniassistant

import ai.picovoice.porcupine.PorcupineManager
import ai.picovoice.porcupine.PorcupineManagerCallback
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PicovoiceService(
  val context: Context,
  val scope: CoroutineScope
) {

  fun start() {
    scope.launch {
      val wakeWordCallback = PorcupineManagerCallback { keywordIndex ->
        if (keywordIndex == 0) {
          println("PICOVOICE_SERVICE: \"Hey, Mini\" detected!")
          scope.launch {
            DataBridge.events.emit(Event.ConsoleEvent("Wake word \"Hey, Mini!\" detected"))
          }
        }
      }

      val porcupineManager: PorcupineManager =
        PorcupineManager.Builder()
          .setAccessKey(BuildConfig.PICOVOICE_API_KEY)
          .setKeywordPaths(arrayOf("Hey-Mini_en_android_v4_0_0.ppn")) //File in ../src/main/assets folder
          .build(context, wakeWordCallback)

      porcupineManager.start()
    }
  }

}
