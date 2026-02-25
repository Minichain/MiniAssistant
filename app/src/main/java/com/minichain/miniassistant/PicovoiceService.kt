package com.minichain.miniassistant

import ai.picovoice.cheetah.Cheetah
import ai.picovoice.porcupine.PorcupineManager
import ai.picovoice.porcupine.PorcupineManagerCallback
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PicovoiceService(
  val context: Context,
  val scope: CoroutineScope
) {

  @androidx.annotation.RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
  fun start() {
//    startPorcupine()
    startCheetah()
  }

  private fun startPorcupine() {
    val wakeWordCallback = PorcupineManagerCallback { keywordIndex ->
      if (keywordIndex == 0) {
        println("PICOVOICE_SERVICE: \"Hey, Mini\" detected!")
        scope.launch {
          DataBridge.events.emit(Event.ConsoleEvent("Wake word \"Hey, Mini!\" detected"))
        }
      }
    }

    val porcupineManager: PorcupineManager =
      PorcupineManager.Builder()
        .setAccessKey(BuildConfig.PICOVOICE_API_KEY)
        .setKeywordPaths(arrayOf("Hey-Mini_en_android_v4_0_0.ppn")) //File in ../src/main/assets folder
        .build(context, wakeWordCallback)

    porcupineManager.start()
  }

  private var audioRecord: AudioRecord? = null
  private var isRecording = false

  private fun startCheetah() {
    val cheetah: Cheetah =
      Cheetah.Builder()
        .setAccessKey(BuildConfig.PICOVOICE_API_KEY)
        .setModelPath("cheetah_params_fast.pv") //File in ../src/main/assets folder
        .build(context)

    scope.launch {

      var minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
      val bufferSizeInShorts = 512 // This is your 'chunk size' in terms of shorts
      if (minBufferSize / 2 < bufferSizeInShorts) {
        minBufferSize = bufferSizeInShorts * 2 // Ensure buffer is large enough, adjusting if necessary
      }

      AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize).let { ar ->
        audioRecord = ar

        val audioBuffer = ShortArray(bufferSizeInShorts) // Allocate buffer for 'chunk size' shorts
        ar.startRecording()
        isRecording = true

        var transcript = StringBuilder()

        while (isRecording) {
          ar.read(audioBuffer, 0, audioBuffer.size)
          val transcriptObj = cheetah.process(audioBuffer)
          transcript.append(transcriptObj.transcript)

          if (transcriptObj.isEndpoint) {
            val finalTranscriptObj = cheetah.flush()
            transcript.append(finalTranscriptObj.transcript)
            DataBridge.events.emit(Event.ConsoleEvent(transcript.toString()))
            transcript = StringBuilder()
          }
        }
      }
    }
  }

  companion object {
    private const val SAMPLE_RATE = 16000
    private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
  }
}
