package com.minichain.miniassistant.assistant

import android.content.Context
import android.util.Log
import com.minichain.miniassistant.Note
import com.minichain.miniassistant.assistant.speech_to_intent.Intent
import com.minichain.miniassistant.assistant.speech_to_intent.SpeechToIntentService
import com.minichain.miniassistant.assistant.speech_to_text.SpeechToTextService
import com.minichain.miniassistant.assistant.wake_word.WakeWordService
import com.minichain.miniassistant.bridge.DataBridge
import com.minichain.miniassistant.bridge.Event
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AssistantService(
  private val context: Context,
  private val scope: CoroutineScope
) {

  private val assistantState = MutableStateFlow(AssistantState.WakeWord)

  private lateinit var tonePlayer: TonePlayer
  private lateinit var vibrator: Vibrator

  private val textToSpeechService = TextToSpeechService(
    context = context,
    scope = scope
  )

  private var wakeWordService: WakeWordService = WakeWordService(
    context = context,
    scope = scope,
    onWakeWordDetected = {
      scope.launch {
        DataBridge.addConsoleLine("WAKE WORD: \"Hey, Mini!\" detected")
        tonePlayer.play(Tone.AssistantStarted)
        assistantState.emit(AssistantState.SpeechToIntent)
      }
    }
  )

  private val speechToIntentService = SpeechToIntentService(
    context = context,
    scope = scope,
    onIntentDetected = { intent ->
      scope.launch {
        DataBridge.addConsoleLine("INTENT: $intent")
        processIntent(intent)
      }
    }
  )

  private val speechToTextService = SpeechToTextService(
    context = context,
    scope = scope,
    onTranscriptionDone = { text ->
      tonePlayer.play(Tone.AssistantStopped)
      scope.launch {
        DataBridge.addConsoleLine("SPEECH-TO-TEXT: $text")
        DataBridge.addNote(Note(Date(), text))
        assistantState.emit(AssistantState.WakeWord)
      }
    }
  )

  fun start() {
    tonePlayer = TonePlayer(context)
    vibrator = Vibrator(context)
    textToSpeechService.start()
    assistantState
      .onEach { state ->
        Log.d("ASSISTANT_SERVICE", "state: $state")
        DataBridge.events.emit(Event.AssistantStateUpdate(state))
        DataBridge.addConsoleLine("Assistant state: $state")
        when (state) {
          AssistantState.WakeWord -> {
            speechToTextService.stop()
            speechToIntentService.stop()
            wakeWordService.start()
          }
          AssistantState.SpeechToIntent -> {
            wakeWordService.stop()
            speechToTextService.stop()
            speechToIntentService.start()
          }
          AssistantState.SpeechToText -> {
            wakeWordService.stop()
            speechToIntentService.stop()
            speechToTextService.start()
          }
        }
      }
      .launchIn(scope)
  }

  private fun processIntent(intent: Intent) {
    when (intent) {
      Intent.StartVideo -> {
        scope.launch {
          DataBridge.events.emit(Event.TextToSpeechEvent("Starting video"))
          DataBridge.events.emit(Event.StartVideo)
          assistantState.emit(AssistantState.WakeWord)
        }
        tonePlayer.play(Tone.AssistantStopped)
      }
      Intent.StopVideo -> {
        scope.launch {
          DataBridge.events.emit(Event.TextToSpeechEvent("Stopping video"))
          DataBridge.events.emit(Event.StopVideo)
          assistantState.emit(AssistantState.WakeWord)
        }
        tonePlayer.play(Tone.AssistantStopped)
      }
      Intent.TakeNotes -> {
        scope.launch {
          DataBridge.events.emit(Event.TextToSpeechEvent("Taking notes"))
          assistantState.emit(AssistantState.SpeechToText)
        }
      }
      Intent.Unknown -> {
        scope.launch {
          DataBridge.events.emit(Event.TextToSpeechEvent("Sorry, I couldn't understand"))
          assistantState.emit(AssistantState.WakeWord)
        }
        tonePlayer.play(Tone.AssistantStopped)
      }
    }
  }

  fun stop() {
    textToSpeechService.stop()
    wakeWordService.stop()
    speechToIntentService.stop()
    speechToTextService.stop()
  }
}
