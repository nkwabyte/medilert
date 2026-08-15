package com.nkwabyte.medilert.util

import com.nkwabyte.medilert.model.MedicationSchedule

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
        tone: String = "Bell"
    )

    fun cancelAlarm(id: String)

    fun scheduleAllUpcoming(schedules: List<MedicationSchedule>, tone: String = "Bell")
}
