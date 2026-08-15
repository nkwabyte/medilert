package com.nkwabyte.medilert.util

expect object SoundPlayer {
    fun playNotificationSound(tone: String = "Happy Bells")
    fun playSoundOnce(tone: String = "Happy Bells")
    fun stopNotificationSound()
}
