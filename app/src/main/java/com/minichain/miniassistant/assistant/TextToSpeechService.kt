package com.minichain.miniassistant.assistant

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.util.Log
import com.minichain.miniassistant.Constants
import com.minichain.miniassistant.bridge.DataBridge
import com.minichain.miniassistant.bridge.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Locale

class TextToSpeechService(
  private val context: Context,
  private val scope: CoroutineScope
) {

  private lateinit var textToSpeech: TextToSpeech
  private lateinit var audioManager: AudioManager

  fun start() {
    textToSpeech = TextToSpeech(
      context
    ) { status ->
      Log.d("TEXT_TO_SPEECH_SERVICE", "Text to speech status: $status")
    }
    audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    textToSpeech.setAudioAttributes(
      AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()
    )
    textToSpeech.setLanguage(Locale.ENGLISH)
    scope.launch {
      DataBridge.events
        .filterIsInstance<Event.TextToSpeechEvent>()
        .onEach {
          Log.d("TEXT_TO_SPEECH_SERVICE", "Text to speech: $it")
          adjustVolume()
          textToSpeech.speak(it.text, TextToSpeech.QUEUE_FLUSH, null, "")
        }
        .launchIn(this)
    }
  }

  private fun adjustVolume() {
    audioManager.setStreamVolume(
      Constants.AUDIO_STREAM,
      (audioManager.getStreamMaxVolume(Constants.AUDIO_STREAM).toFloat() * Constants.DEFAULT_VOLUME).toInt(),
      0
    )
  }

  fun stop() {
    textToSpeech.stop()
  }
}