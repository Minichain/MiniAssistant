package com.minichain.miniassistant.bridge

sealed class Event {
  data class ConsoleEvent(val message: String) : Event()
}