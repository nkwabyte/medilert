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

    private fun getFileName(tone: String): String {
        return when (tone) {
            "Happy Bells", "Default" -> "happy_bells_notification"
            "Bell" -> "bell_notification"
            "Clear Tones" -> "clear_announce_tones"
            "Urgent Loop" -> "urgent_simple_tone_loop"
            else -> "happy_bells_notification"
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun playNotificationSound(tone: String) {
        val fileName = getFileName(tone)
        val url = NSBundle.mainBundle.URLForResource(fileName, withExtension = "wav")
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
        val fileName = getFileName(tone)
        val url = NSBundle.mainBundle.URLForResource(fileName, withExtension = "wav")
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

    actual fun stopNotificationSound() {
        try {
            currentPlayer?.stop()
            currentPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
