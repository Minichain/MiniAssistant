package com.minichain.miniassistant.assistant

import ai.picovoice.rhino.RhinoInference
import ai.picovoice.rhino.RhinoManager
import ai.picovoice.rhino.RhinoManagerCallback
import android.content.Context
import android.util.Log
import com.minichain.miniassistant.BuildConfig
import kotlinx.coroutines.CoroutineScope

class SpeechToIntentService(
  private val context: Context,
  private val scope: CoroutineScope,
  val onIntentDetected: (intent: Intent) -> Unit
) {

  private val rhinoManager by lazy {
    RhinoManager.Builder()
      .setAccessKey(BuildConfig.PICOVOICE_API_KEY)
      .setContextPath("Mini-Speech-to-Intent_en_android_v4_0_0.rhn") //File in ../src/main/assets folder
      .build(context, rhinoManagerCallback)
  }

  private val rhinoManagerCallback by lazy {
    RhinoManagerCallback { inference: RhinoInference ->
      Log.d("ASSISTANT_SERVICE", "inference intent: ${inference.intent}")
      if (inference.isUnderstood) {
        onIntentDetected(Intent.valueOf(inference.intent))
      } else {
        onIntentDetected(Intent.Unknown)
      }
    }
  }

  fun start() {
    rhinoManager.process()
  }

  fun stop() {
    //Rhino is stopped automatically after RhinoManagerCallback is called
  }
}