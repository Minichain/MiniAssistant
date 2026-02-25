package com.minichain.miniassistant

import kotlinx.coroutines.flow.MutableSharedFlow

object DataBridge {
  val events = MutableSharedFlow<Event>()
}