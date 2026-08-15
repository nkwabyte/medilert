package com.nkwabyte.medilert.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSBundle
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time

actual object SoundPlayer {
    private var currentPlayer: AVAudioPlayer? = null

    private fun getFileName(tone: String): String {
        return when (tone) {
            "Bell" -> "bell_notification"
            "Clear Tones" -> "clear_announce_tones"
            "Happy Bells" -> "happy_bells_notification"
            "Urgent Loop", "Default" -> "urgent_simple_tone_loop"
            else -> "bell_notification"
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun playNotificationSound(tone: String) {
        val fileName = getFileName(tone)
        val url = NSBundle.mainBundle.URLForResource(fileName, withExtension = "wav")
        if (url != null) {
            try {
                currentPlayer?.stop()
                val player = AVAudioPlayer(contentsOfURL = url, error = null)
                player.numberOfLoops = -1 // loop infinitely
                player.prepareToPlay()
                player.play()
                currentPlayer = player

                // Stop after 30 seconds
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
        currentPlayer?.stop()
        currentPlayer = null
    }
}
