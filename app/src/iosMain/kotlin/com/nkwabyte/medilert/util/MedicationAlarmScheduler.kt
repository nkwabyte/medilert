package com.nkwabyte.medilert.util

import com.nkwabyte.medilert.model.DoseStatus
import com.nkwabyte.medilert.model.MedicationSchedule
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

actual object MedicationAlarmScheduler {

    actual fun scheduleAlarm(
        id: String,
        medicationName: String,
        dose: String,
        unit: String,
        triggerTimeMillis: Long,
        tone: String
    ) {
        val now = (NSDate().timeIntervalSince1970 * 1000).toLong()
        val delayMillis = triggerTimeMillis - now
        if (delayMillis <= 0) return

        val seconds = (delayMillis / 1000.0).coerceAtLeast(1.0)

        val center = UNUserNotificationCenter.currentNotificationCenter()
        val content = UNMutableNotificationContent().apply {
            setTitle("⏰ Medication Reminder: $medicationName")
            val doseStr = if (dose.isNotBlank()) "Take $dose $unit of $medicationName" else "Time to take your scheduled dose of $medicationName"
            setBody("$doseStr. Tap to confirm or log your dose in MediLert.")
            
            val soundFileName = when (tone) {
                "Bell" -> "bell_notification.wav"
                "Clear Tones" -> "clear_announce_tones.wav"
                "Happy Bells" -> "happy_bells_notification.wav"
                "Urgent Loop", "Default" -> "urgent_simple_tone_loop.wav"
                else -> "bell_notification.wav"
            }
            setSound(UNNotificationSound.soundNamed(soundFileName) ?: UNNotificationSound.defaultSound())
        }

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(seconds, repeats = false)
        val request = UNNotificationRequest.requestWithIdentifier(id, content, trigger)

        center.addNotificationRequest(request) { error ->
            if (error != null) {
                // Handle or log error
            }
        }
    }

    actual fun cancelAlarm(id: String) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removePendingNotificationRequestsWithIdentifiers(listOf(id))
    }

    actual fun scheduleAllUpcoming(schedules: List<MedicationSchedule>, tone: String) {
        val now = Clock.System.now().toEpochMilliseconds()
        schedules.forEach { schedule ->
            if (schedule.status == DoseStatus.UPCOMING) {
                val triggerMillis = parseScheduleTime(schedule.scheduledTime, schedule.date)
                if (triggerMillis > now) {
                    scheduleAlarm(
                        id = schedule.id,
                        medicationName = schedule.medicationName,
                        dose = schedule.dose.toString(),
                        unit = schedule.unit,
                        triggerTimeMillis = triggerMillis,
                        tone = tone
                    )
                }
            }
        }
    }

    private fun parseScheduleTime(timeStr: String, dateStr: String): Long {
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
                        kotlinx.datetime.LocalDate(dParts[0].toInt(), dParts[1].toInt(), dParts[2].toInt())
                    } else localNow.date
                } catch (_: Exception) {
                    localNow.date
                }
            } else localNow.date

            // Simple epoch calculation for current date
            val daysFromEpoch = (targetDate.year - 1970) * 365 + targetDate.dayOfYear
            val totalSeconds = daysFromEpoch * 86400L + hour * 3600L + minute * 60L
            totalSeconds * 1000L
        } catch (_: Exception) {
            0L
        }
    }
}
