package com.nkwabyte.medilert.util

expect object HapticFeedback {
    fun success()
    fun error()
    fun light()
}
