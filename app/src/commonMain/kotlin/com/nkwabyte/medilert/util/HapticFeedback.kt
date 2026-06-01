package com.nkwabyte.medilert.util

import android.content.Context

expect object HapticFeedback {
    fun success(context: Context)
    fun error(context: Context)
    fun light(context: Context)
}
