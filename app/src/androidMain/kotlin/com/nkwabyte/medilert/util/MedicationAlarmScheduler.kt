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
    }
}
