package com.minichain.miniassistant.assistant.speech_to_text

import ai.picovoice.cheetah.Cheetah
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.minichain.miniassistant.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SpeechToTextService(
  private val context: Context,
  private val scope: CoroutineScope,
  val onTranscriptionDone: (text: String) -> Unit
) {

  private val cheetah by lazy {
    Cheetah.Builder()
      .setAccessKey(BuildConfig.PICOVOICE_API_KEY)
      .setModelPath("cheetah_params_fast.pv") //File in ../src/main/assets folder
      .build(context)
  }

  private var isRecording = false
  private val audioRecord: AudioRecord by lazy {
    var minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
    if (minBufferSize / 2 < BUFFER_SIZE_IN_SHORTS) {
      minBufferSize = BUFFER_SIZE_IN_SHORTS * 2 // Ensure buffer is large enough, adjusting if necessary
    }
    AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize)
  }

  fun start() {
    scope.launch {
      val audioBuffer = ShortArray(BUFFER_SIZE_IN_SHORTS) // Allocate buffer for 'chunk size' shorts
      audioRecord.startRecording()
      isRecording = true

      val transcript = StringBuilder()

      while (isRecording) {
        audioRecord.read(audioBuffer, 0, audioBuffer.size)
        val transcriptObj = cheetah.process(audioBuffer)
        transcript.append(transcriptObj.transcript)

        if (transcriptObj.isEndpoint) {
          val finalTranscriptObj = cheetah.flush()
          transcript.append(finalTranscriptObj.transcript)
          onTranscriptionDone(transcript.toString())
        }
      }
    }
  }

  fun stop() {
    isRecording = false
    audioRecord.stop()
  }

  companion object {
    private const val SAMPLE_RATE = 16000
    private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    private const val BUFFER_SIZE_IN_SHORTS = 512 // This is your 'chunk size' in terms of shorts
  }
}