package com.minichain.miniassistant

import ai.picovoice.porcupine.Porcupine.BuiltInKeyword
import ai.picovoice.porcupine.PorcupineManager
import ai.picovoice.porcupine.PorcupineManagerCallback
import android.content.Context

class PicovoiceService(val context: Context) {

  fun start() {

    val wakeWordCallback = object : PorcupineManagerCallback {
      override fun invoke(keywordIndex: Int) {
        if (keywordIndex == 0) {
          println("PICOVOICE_SERVICE: \"Hey, Mini\" detected!")
        }
      }
    }

    val porcupineManager: PorcupineManager =
      PorcupineManager.Builder()
        .setAccessKey(BuildConfig.PICOVOICE_API_KEY)
        .setKeywordPaths(arrayOf("Hey-Mini_en_android_v4_0_0.ppn")) //File in ../src/main/assets folder
//        .setKeywords(arrayOf(BuiltInKeyword.PORCUPINE, BuiltInKeyword.BUMBLEBEE))
        .build(context, wakeWordCallback)

    porcupineManager.start()
  }

}
