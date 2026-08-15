package com.nkwabyte.medilert.util

import com.nkwabyte.medilert.model.DoseStatus
import com.nkwabyte.medilert.model.MedicationSchedule
import kotlinx.datetime.Clock
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

actual object MedicationAlarmScheduler {

    actual fun scheduleAlarm(
        id: String,
        medicationName: String,
        dose: String,
        unit: String,
        triggerTimeMillis: Long,
        tone: String
    ) {
        try {
            val now = (NSDate().timeIntervalSince1970 * 1000).toLong()
            val delayMillis = triggerTimeMillis - now
            if (delayMillis <= 0) return

            val seconds = (delayMillis / 1000.0).coerceAtLeast(1.0)

            dispatch_async(dispatch_get_main_queue()) {
                try {
                    val center = UNUserNotificationCenter.currentNotificationCenter()
                    val content = UNMutableNotificationContent().apply {
                        setTitle("⏰ Medication Reminder: $medicationName")
                        val doseStr = if (dose.isNotBlank()) "Take $dose $unit of $medicationName" else "Time to take your scheduled dose of $medicationName"
                        setBody("$doseStr. Tap to confirm or log your dose in MediLert.")

                        val soundFileName = when (tone) {
                            "Happy Bells", "Default" -> "happy_bells_notification.wav"
                            "Bell" -> "bell_notification.wav"
                            "Clear Tones" -> "clear_announce_tones.wav"
                            "Urgent Loop" -> "urgent_simple_tone_loop.wav"
                            else -> "happy_bells_notification.wav"
                        }
                        setSound(UNNotificationSound.soundNamed(soundFileName) ?: UNNotificationSound.defaultSound())
                    }

                    val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(seconds, repeats = false)
                    val request = UNNotificationRequest.requestWithIdentifier(id, content, trigger)

                    center.addNotificationRequest(request, withCompletionHandler = null)
                } catch (e: Throwable) {
                    println("Error scheduling iOS notification: ${e.message}")
                }
            }
        } catch (e: Throwable) {
            println("Error in scheduleAlarm: ${e.message}")
        }
    }

    actual fun cancelAlarm(id: String) {
        try {
            dispatch_async(dispatch_get_main_queue()) {
                try {
                    val center = UNUserNotificationCenter.currentNotificationCenter()
                    center.removePendingNotificationRequestsWithIdentifiers(listOf(id))
                } catch (e: Throwable) {
                    println("Error canceling iOS notification: ${e.message}")
                }
            }
        } catch (e: Throwable) {
            println("Error in cancelAlarm: ${e.message}")
        }
    }

    actual fun scheduleAllUpcoming(schedules: List<MedicationSchedule>, tone: String) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()
            schedules.forEach { schedule ->
                if (schedule.status == DoseStatus.UPCOMING) {
                    val triggerMillis = parseScheduleTimeToEpochMillis(schedule.scheduledTime, schedule.date)
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
        } catch (e: Throwable) {
            println("Error in scheduleAllUpcoming: ${e.message}")
        }
    }
}
