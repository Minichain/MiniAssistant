package com.minichain.miniassistant.assistant

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import com.minichain.miniassistant.Constants

class TonePlayer(private val context: Context) {

  private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
  private val soundPool: SoundPool
  private val maxStreams = 2
  private val tonesToIdsMap: Map<Tone, Int>

  init {
    soundPool = SoundPool.Builder()
      .setMaxStreams(maxStreams)
      .setAudioAttributes(
        AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_NOTIFICATION)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build()
      )
      .build()

    tonesToIdsMap = Tone.entries.associateWith { soundPool.load(context, it.res, 0) }
  }

  fun play(tone: Tone) {
    audioManager.setStreamVolume(
      Constants.AUDIO_STREAM,
      (audioManager.getStreamMaxVolume(Constants.AUDIO_STREAM).toFloat() * Constants.DEFAULT_VOLUME).toInt(),
      0
    )
    tonesToIdsMap[tone]?.let {
      soundPool.play(it, 1f, 1f, 0, 0, 1f)
    }
  }
}
