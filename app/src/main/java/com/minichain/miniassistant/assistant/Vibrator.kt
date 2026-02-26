package com.minichain.miniassistant.assistant

import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager

class Vibrator(context: Context) {

  private var vibrator = (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator

  enum class VibrationType {
    SINGLE_BEAT,
    DOUBLE_BEAT,
  }

  fun vibrate(
    vibrationType: VibrationType = VibrationType.SINGLE_BEAT
  ) {
    when (vibrationType) {
      VibrationType.SINGLE_BEAT -> {
        vibrator.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE))
      }
      VibrationType.DOUBLE_BEAT -> {
        val timings = longArrayOf(0, 200, 100, 200)
        val amplitudes = intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE)
        val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
        vibrator.vibrate(effect)
      }
    }
  }
}
