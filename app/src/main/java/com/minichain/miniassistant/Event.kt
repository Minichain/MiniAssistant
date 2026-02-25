package com.minichain.miniassistant

sealed class Event {
  data class ConsoleEvent(val message: String) : Event()
}