package com.minichain.miniassistant.assistant

import android.content.Context
import android.util.Log
import com.minichain.miniassistant.bridge.DataBridge
import com.minichain.miniassistant.bridge.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AssistantService(
  context: Context,
  private val scope: CoroutineScope
) {

  private val assistantState = MutableStateFlow(AssistantState.WakeWord)

  private var wakeWordService: WakeWordService = WakeWordService(
    context = context,
    scope = scope,
    onWakeWordDetected = {
      scope.launch {
        DataBridge.events.emit(Event.ConsoleEvent("WAKE WORD: \"Hey, Mini!\" detected"))
        assistantState.emit(AssistantState.SpeechToIntent)
      }
    }
  )

  private val speechToIntentService = SpeechToIntentService(
    context = context,
    scope = scope,
    onIntentDetected = { intent ->
      scope.launch {
        DataBridge.events.emit(Event.ConsoleEvent("INTENT: $intent"))
        when (intent) {
          Intent.StartVideo -> {
            assistantState.emit(AssistantState.WakeWord)
          }
          Intent.StopVideo -> {
            assistantState.emit(AssistantState.WakeWord)
          }
          Intent.TakeNotes -> {
            assistantState.emit(AssistantState.SpeechToText)
          }
        }
      }
    }
  )

  private val speechToTextService = SpeechToTextService(
    context = context,
    scope = scope,
    onTranscriptionDone = { text ->
      scope.launch {
        DataBridge.events.emit(Event.ConsoleEvent("SPEECH-TO-TEXT: $text"))
        assistantState.emit(AssistantState.WakeWord)
      }
    }
  )

  fun start() {
    assistantState
      .onEach { state ->
        Log.d("ASSISTANT_SERVICE", "state: $state")
        DataBridge.events.emit(Event.ConsoleEvent("Assistant state: $state"))
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

  private enum class AssistantState {
    WakeWord,
    SpeechToIntent,
    SpeechToText
  }
}
