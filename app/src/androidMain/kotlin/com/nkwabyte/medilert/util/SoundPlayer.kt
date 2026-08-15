package com.nkwabyte.medilert.util

import android.media.MediaPlayer
import com.nkwabyte.medilert.MedilertApplication
import com.nkwabyte.medilert.R

actual object SoundPlayer {
    private var currentPlayer: MediaPlayer? = null

    actual fun playNotificationSound(tone: String) {
        try {
            val resId = getToneResourceId(tone)
            val context = MedilertApplication.appContext
            
            stopNotificationSound()
            val mediaPlayer = MediaPlayer.create(context, resId)
            currentPlayer = mediaPlayer
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()

            // Stop and release after 30 seconds
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (currentPlayer == mediaPlayer) {
                    stopNotificationSound()
                }
            }, 30000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun playSoundOnce(tone: String) {
        try {
            val resId = getToneResourceId(tone)
            val context = MedilertApplication.appContext
            stopNotificationSound()
            val mediaPlayer = MediaPlayer.create(context, resId)
            currentPlayer = mediaPlayer
            mediaPlayer?.isLooping = false
            mediaPlayer?.setOnCompletionListener {
                it.release()
                if (currentPlayer == it) currentPlayer = null
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getToneResourceId(tone: String): Int {
        return when (tone) {
            "Bell" -> R.raw.bell_notification
            "Clear Tones" -> R.raw.clear_announce_tones
            "Happy Bells" -> R.raw.happy_bells_notification
            "Urgent Loop", "Default" -> R.raw.urgent_simple_tone_loop
            else -> R.raw.bell_notification
        }
    }

    actual fun stopNotificationSound() {
        try {
            if (currentPlayer?.isPlaying == true) {
                currentPlayer?.stop()
            }
            currentPlayer?.release()
            currentPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
