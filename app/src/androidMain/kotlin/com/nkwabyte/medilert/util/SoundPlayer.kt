package com.nkwabyte.medilert.util

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import com.nkwabyte.medilert.MedilertApplication
import com.nkwabyte.medilert.R

actual object SoundPlayer {
    private var currentPlayer: MediaPlayer? = null
    private var stopHandler: Handler? = null
    private var stopRunnable: Runnable? = null

    actual fun playNotificationSound(tone: String) {
        try {
            val resId = getToneResourceId(tone)
            val context = MedilertApplication.appContext
            
            stopNotificationSound()

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(audioAttributes)
                try {
                    setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
                } catch (_: Exception) {}

                val afd = context.resources.openRawResourceFd(resId)
                if (afd != null) {
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                    isLooping = true
                    prepare()
                    start()
                }
            }

            currentPlayer = mediaPlayer

            // Loop and play continuously for 30 seconds, then auto-stop
            val handler = Handler(Looper.getMainLooper())
            val runnable = Runnable {
                if (currentPlayer == mediaPlayer) {
                    stopNotificationSound()
                }
            }
            stopHandler = handler
            stopRunnable = runnable
            handler.postDelayed(runnable, 30_000L)
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

    actual fun playMedicationReminderSound(language: String) {
        val isTwi = language.contains("twi", ignoreCase = true) ||
                    language.contains("akan", ignoreCase = true) ||
                    language.equals("tw", ignoreCase = true)
        val tone = if (isTwi) "time_for_medication_twi" else "time_for_medication_english"
        playSoundOnce(tone)
    }

    private fun getToneResourceId(tone: String): Int {
        return when (tone) {
            "Happy Bells", "Default" -> R.raw.happy_bells_notification
            "Bell" -> R.raw.bell_notification
            "Clear Tones" -> R.raw.clear_announce_tones
            "Urgent Loop" -> R.raw.urgent_simple_tone_loop
            "time_for_medication_twi", "Twi" -> R.raw.time_for_medication_twi
            "time_for_medication_english", "English" -> R.raw.time_for_medication_english
            else -> {
                if (tone.contains("twi", ignoreCase = true) || tone.contains("akan", ignoreCase = true)) {
                    R.raw.time_for_medication_twi
                } else if (tone.contains("english", ignoreCase = true) || tone.contains("medication", ignoreCase = true)) {
                    R.raw.time_for_medication_english
                } else {
                    R.raw.happy_bells_notification
                }
            }
        }
    }

    actual fun stopNotificationSound() {
        try {
            stopRunnable?.let { stopHandler?.removeCallbacks(it) }
            stopRunnable = null
            stopHandler = null

            currentPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
            currentPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
