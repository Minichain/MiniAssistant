package com.minichain.miniassistant.bridge

import com.minichain.miniassistant.Note
import com.minichain.miniassistant.video.VideoStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow

object DataBridge {

  val events = MutableSharedFlow<Event>()

  private val _consoleLines = MutableStateFlow<List<String>>(emptyList())
  val consoleLines = _consoleLines.asSharedFlow()

  private val _notes = MutableStateFlow<List<Note>>(emptyList())
  val notes = _notes.asSharedFlow()

  private val _videoStatus = MutableStateFlow<VideoStatus>(VideoStatus.Stopped)
  val videoStatus = _videoStatus.asSharedFlow()

  fun addConsoleLine(message: String) {
    val newList = _consoleLines.value.toMutableList()
    newList.add(message)
    _consoleLines.tryEmit(newList)
  }

  suspend fun addNote(note: Note) {
    val newList = _notes.value.toMutableList()
    newList.add(note)
    _notes.emit(newList)
  }

  suspend fun updateVideoStatus(newStatus: VideoStatus) {
    _videoStatus.emit(newStatus)
  }
}
