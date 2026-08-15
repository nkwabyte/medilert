package com.nkwabyte.medilert.util

expect object SoundPlayer {
    fun playNotificationSound(tone: String)
    fun playSoundOnce(tone: String = "Bell")
    fun stopNotificationSound()
}
