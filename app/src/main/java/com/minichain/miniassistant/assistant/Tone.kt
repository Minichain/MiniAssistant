package com.minichain.miniassistant.assistant

import com.minichain.miniassistant.R

enum class Tone(
  val res: Int
) {
  AssistantStarted(R.raw.notification_01),
  AssistantStopped(R.raw.notification_02)
}
