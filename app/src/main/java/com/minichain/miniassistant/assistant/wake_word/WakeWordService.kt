package com.minichain.miniassistant.assistant.wake_word

import ai.picovoice.porcupine.PorcupineManager
import ai.picovoice.porcupine.PorcupineManagerCallback
import android.content.Context
import android.util.Log
import com.minichain.miniassistant.BuildConfig
import kotlinx.coroutines.CoroutineScope

class WakeWordService(
  private val context: Context,
  private val scope: CoroutineScope,
  val onWakeWordDetected: () -> Unit
) {

  private val porcupineManager by lazy {
    PorcupineManager.Builder()
      .setAccessKey(BuildConfig.PICOVOICE_API_KEY)
      .setKeywordPaths(arrayOf("Hey-Mini_en_android_v4_0_0.ppn")) //File in ../src/main/assets folder
      .build(context, wakeWordCallback)
  }

  private val wakeWordCallback by lazy {
    PorcupineManagerCallback { keywordIndex ->
      if (keywordIndex == 0) {
        Log.d("WAKE_WORD_SERVICE", "\"Hey, Mini\" detected!")
        onWakeWordDetected()
      }
    }
  }

  fun start() {
    porcupineManager.start()
  }

  fun stop() {
    porcupineManager.stop()
  }
}