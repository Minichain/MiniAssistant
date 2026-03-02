package com.minichain.miniassistant.assistant.wake_word

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
internal class OpenWakeWordAudioRecorder(
  private var model: OpenWakeWordModel,
  private val scope: CoroutineScope,
  val onWakeWordDetected: () -> Unit
) {

  private var audioRecord: AudioRecord? = null
  private var isRecording = false

  fun start() {
    scope.launch {
      // Ensure the buffer size is at least as large as the chunk size needed
      var minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
      val bufferSizeInShorts = 1280 // This is your 'chunk size' in terms of shorts
      if (minBufferSize / 2 < bufferSizeInShorts) {
        minBufferSize = bufferSizeInShorts * 2 // Ensure buffer is large enough, adjusting if necessary
      }

      AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize).let { ar ->
        audioRecord = ar

        //        if (ar.state != AudioRecord.STATE_INITIALIZED) {
        //          // Initialization error handling
        //          return
        //        }

        val audioBuffer = ShortArray(bufferSizeInShorts) // Allocate buffer for 'chunk size' shorts
        ar.startRecording()
        isRecording = true

        while (isRecording) {
          // Reading data from the microphone in chunks
          ar.read(audioBuffer, 0, audioBuffer.size)
          val floatBuffer = FloatArray(audioBuffer.size)

          // Convert each short to float
          for (i in audioBuffer.indices) {
            // Convert by dividing by the maximum value of short to normalize
            floatBuffer[i] = audioBuffer[i] / 32768.0f // Normalize to range -1.0 to 1.0 if needed
          }
          val res = model.predictWakeWord(floatBuffer)
          if (res >= 0.05f) {
            isRecording = false
            onWakeWordDetected()
          }
        }

        releaseResources()
      }
    }
  }

  fun stopRecording() {
    scope.launch {
      isRecording = false
    }
  }

  private fun releaseResources() {
    scope.launch {
      audioRecord?.apply {
        stop()
        release()
      }
      audioRecord = null
    }
  }

  companion object {
    private const val SAMPLE_RATE = 16000
    private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
  }
}
