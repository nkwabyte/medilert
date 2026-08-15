package com.nkwabyte.medilert.util

import com.nkwabyte.medilert.model.MedicationSchedule
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Cross-platform scheduler for on-device medication alarms and background notifications.
 *
 * Runs locally without needing cloud push notifications or App Store/Play Store servers.
 */
expect object MedicationAlarmScheduler {
    fun scheduleAlarm(
        id: String,
        medicationName: String,
        dose: String,
        unit: String,
        triggerTimeMillis: Long,
        tone: String = "Happy Bells"
    )

    fun cancelAlarm(id: String)

    fun scheduleAllUpcoming(schedules: List<MedicationSchedule>, tone: String = "Happy Bells")
}

fun parseScheduleTimeToEpochMillis(timeStr: String, dateStr: String): Long {
    return try {
        val now = Clock.System.now()
        val localNow = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val parts = timeStr.trim().split(" ")
        val timeParts = parts[0].split(":")
        var hour = timeParts[0].toInt()
        val minute = if (timeParts.size > 1) timeParts[1].toInt() else 0
        if (parts.size > 1 && parts[1].equals("PM", ignoreCase = true) && hour < 12) {
            hour += 12
        } else if (parts.size > 1 && parts[1].equals("AM", ignoreCase = true) && hour == 12) {
            hour = 0
        }

        val targetDate = if (dateStr.isNotBlank()) {
            try {
                val dParts = dateStr.split("-")
                if (dParts.size == 3) {
                    LocalDate(dParts[0].toInt(), dParts[1].toInt(), dParts[2].toInt())
                } else localNow.date
            } catch (_: Exception) {
                localNow.date
            }
        } else localNow.date

        val localDateTime = LocalDateTime(
            targetDate.year,
            targetDate.monthNumber,
            targetDate.dayOfMonth,
            hour,
            minute,
            0,
            0
        )
        localDateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    } catch (_: Exception) {
        0L
    }
}
