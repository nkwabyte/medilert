package com.nkwabyte.medilert.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.nkwabyte.medilert.MedilertApplication
import com.nkwabyte.medilert.model.DoseStatus
import com.nkwabyte.medilert.model.MedicationSchedule
import com.nkwabyte.medilert.service.MedicationAlarmReceiver
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

actual object MedicationAlarmScheduler {

    actual fun scheduleAlarm(
        id: String,
        medicationName: String,
        dose: String,
        unit: String,
        triggerTimeMillis: Long,
        tone: String
    ) {
        val now = System.currentTimeMillis()
        if (triggerTimeMillis <= now) return

        val context = MedilertApplication.appContext
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra(MedicationAlarmReceiver.EXTRA_ID, id)
            putExtra(MedicationAlarmReceiver.EXTRA_MED_NAME, medicationName)
            putExtra(MedicationAlarmReceiver.EXTRA_DOSE, dose)
            putExtra(MedicationAlarmReceiver.EXTRA_UNIT, unit)
            putExtra(MedicationAlarmReceiver.EXTRA_TONE, tone)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // In case exact alarm permission is restricted on Android 12+, fallback to setAndAllowWhileIdle
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                }
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun cancelAlarm(id: String) {
        val context = MedilertApplication.appContext
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, MedicationAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
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

            val localDateTime = kotlinx.datetime.LocalDateTime(
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

    private fun kotlinx.datetime.LocalDateTime.toInstant(zone: TimeZone): Instant {
        val epochSeconds = toEpochSecond(zone)
        return Instant.fromEpochSeconds(epochSeconds, nanosecond)
    }

    private fun kotlinx.datetime.LocalDateTime.toEpochSecond(zone: TimeZone): Long {
        // Approximate calculation with standard epoch conversion
        val days = java.time.LocalDate.of(year, monthNumber, dayOfMonth).toEpochDay()
        val secs = days * 86400L + hour * 3600L + minute * 60L + second
        val offsetSecs = java.time.ZoneId.systemDefault().rules.getOffset(java.time.Instant.now()).totalSeconds
        return secs - offsetSecs
    }
}
