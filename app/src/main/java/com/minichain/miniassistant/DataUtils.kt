package com.minichain.miniassistant

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun getFormattedDate(pattern: String): String {
  val current = LocalDateTime.now()
  val formatter = DateTimeFormatter.ofPattern(pattern)
  return current.format(formatter)
}
