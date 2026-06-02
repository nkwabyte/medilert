package com.nkwabyte.medilert.util

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.nkwabyte.medilert.AndroidActivityHolder

actual object HapticFeedback {

    private fun vibrator(): Vibrator? {
        val ctx = AndroidActivityHolder.activity ?: return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (ctx.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            ctx.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    actual fun success() {
        val v = vibrator() ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION") v.vibrate(60)
        }
    }

    actual fun error() {
        val v = vibrator() ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 60, 80), -1))
        } else {
            @Suppress("DEPRECATION") v.vibrate(longArrayOf(0, 80, 60, 80), -1)
        }
    }

    actual fun light() {
        val v = vibrator() ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(30, 60))
        } else {
            @Suppress("DEPRECATION") v.vibrate(30)
        }
    }
}
