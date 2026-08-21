package com.nkwabyte.medilert.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSBundle
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time

actual object SoundPlayer {
    private var currentPlayer: AVAudioPlayer? = null

    private fun getFileInfo(tone: String): Pair<String, String> {
        return when (tone) {
            "Happy Bells", "Default" -> Pair("happy_bells_notification", "wav")
            "Bell" -> Pair("bell_notification", "wav")
            "Clear Tones" -> Pair("clear_announce_tones", "wav")
            "Urgent Loop" -> Pair("urgent_simple_tone_loop", "wav")
            "time_for_medication_twi", "Twi" -> Pair("time_for_medication_twi", "mp3")
            "time_for_medication_english", "English" -> Pair("time_for_medication_english", "mp3")
            else -> {
                if (tone.contains("twi", ignoreCase = true) || tone.contains("akan", ignoreCase = true)) {
                    Pair("time_for_medication_twi", "mp3")
                } else if (tone.contains("english", ignoreCase = true) || tone.contains("medication", ignoreCase = true)) {
                    Pair("time_for_medication_english", "mp3")
                } else {
                    Pair("happy_bells_notification", "wav")
                }
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun playNotificationSound(tone: String) {
        val (fileName, ext) = getFileInfo(tone)
        var url = NSBundle.mainBundle.URLForResource(fileName, withExtension = ext)
        if (url == null) {
            val fallbackExt = if (ext == "wav") "mp3" else "wav"
            url = NSBundle.mainBundle.URLForResource(fileName, withExtension = fallbackExt)
        }
        if (url != null) {
            try {
                val session = AVAudioSession.sharedInstance()
                session.setCategory(AVAudioSessionCategoryPlayback, error = null)
                session.setActive(true, error = null)

                currentPlayer?.stop()
                val player = AVAudioPlayer(contentsOfURL = url, error = null)
                player.numberOfLoops = -1 // loop continuously
                player.prepareToPlay()
                player.play()
                currentPlayer = player

                // Automatically stop after 30 seconds
                val delayTime = dispatch_time(DISPATCH_TIME_NOW, 30_000_000_000)
                dispatch_after(delayTime, dispatch_get_main_queue()) {
                    if (currentPlayer == player) {
                        stopNotificationSound()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun playSoundOnce(tone: String) {
        val (fileName, ext) = getFileInfo(tone)
        var url = NSBundle.mainBundle.URLForResource(fileName, withExtension = ext)
        if (url == null) {
            val fallbackExt = if (ext == "wav") "mp3" else "wav"
            url = NSBundle.mainBundle.URLForResource(fileName, withExtension = fallbackExt)
        }
        if (url != null) {
            try {
                val session = AVAudioSession.sharedInstance()
                session.setCategory(AVAudioSessionCategoryPlayback, error = null)
                session.setActive(true, error = null)

                currentPlayer?.stop()
                val player = AVAudioPlayer(contentsOfURL = url, error = null)
                player.numberOfLoops = 0 // play once
                player.prepareToPlay()
                player.play()
                currentPlayer = player
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun playMedicationReminderSound(language: String) {
        val isTwi = language.contains("twi", ignoreCase = true) ||
                    language.contains("akan", ignoreCase = true) ||
                    language.equals("tw", ignoreCase = true)
        val tone = if (isTwi) "time_for_medication_twi" else "time_for_medication_english"
        playSoundOnce(tone)
    }

    actual fun stopNotificationSound() {
        try {
            currentPlayer?.stop()
            currentPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
