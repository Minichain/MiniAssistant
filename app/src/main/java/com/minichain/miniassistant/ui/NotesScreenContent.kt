package com.minichain.miniassistant.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minichain.miniassistant.Note
import com.minichain.miniassistant.bridge.DataBridge
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun NotesScreenContent() {
  Surface(
    modifier = Modifier.fillMaxSize(),
  ) {
    Column(
      modifier = Modifier
        .padding(12.dp)
        .fillMaxSize()
    ) {

      var notes: List<Note> by remember { mutableStateOf(emptyList()) }

      Text(
        modifier = Modifier
          .fillMaxWidth()
          .weight(0.2f),
        fontSize = 20.sp,
        text = "Notes"
      )

      LazyColumn(
        modifier = Modifier
          .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        item {
          Spacer(modifier = Modifier.height(12.dp))
        }
        notes.forEach { note ->
          item {
            NoteContent(note)
          }
        }
        item {
          Spacer(modifier = Modifier.height(12.dp))
        }
      }

      LaunchedEffect(Unit) {
        DataBridge.notes
          .filter { it.isNotEmpty() }
          .onEach { notes = it }
          .launchIn(this)
      }
    }
  }
}

@Composable
private fun NoteContent(note: Note) {
  Card(
    elevation = CardDefaults.cardElevation(
      defaultElevation = 6.dp
    )
  ) {
    Column(
      modifier = Modifier.padding(8.dp)
    ) {
      Text(
        text = note.date.toString()
      )
      Text(
        text = note.message
      )
    }
  }
}
