package com.minichain.miniassistant.bridge

import com.minichain.miniassistant.assistant.AssistantState

sealed class Event {
  data class AssistantStateUpdate(val state: AssistantState) : Event()
  data class ConsoleEvent(val message: String) : Event()
  data class TextToSpeechEvent(val text: String) : Event()
  data class NoteTakenEvent(val note: String) : Event()
}