package com.minichain.miniassistant.bridge

import kotlinx.coroutines.flow.MutableSharedFlow

object DataBridge {
  val events = MutableSharedFlow<Event>()
}