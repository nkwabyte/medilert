package com.nkwabyte.medilert.util

expect object SoundPlayer {
    fun playNotificationSound(tone: String)
    fun stopNotificationSound()
}
