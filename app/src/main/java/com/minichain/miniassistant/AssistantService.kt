package com.minichain.miniassistant

import android.content.Context
import android.util.Log
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

  private var wakeWordService: WakeWordService = WakeWordService(
    context = context,
    scope = scope,
    onWakeWordDetected = {
      scope.launch {
        DataBridge.events.emit(Event.ConsoleEvent("Wake word \"Hey, Mini!\" detected"))
        assistantState.emit(AssistantState.SpeechToText)
      }
    }
  )

  private val speechToTextService = SpeechToTextService(
    context = context,
    scope = scope
  )

  fun start() {
    assistantState
      .onEach { state ->
        Log.d("ASSISTANT_SERVICE", "state: $state")
        DataBridge.events.emit(Event.ConsoleEvent("Assistant state: $state"))
        when (state) {
          AssistantState.WakeWord -> {
            speechToTextService.stop()
            wakeWordService.start()
          }
          AssistantState.SpeechToText -> {
            wakeWordService.stop()
            speechToTextService.start()
          }
        }
      }
      .launchIn(scope)
  }

  private enum class AssistantState {
    WakeWord,
    SpeechToText
  }
}
