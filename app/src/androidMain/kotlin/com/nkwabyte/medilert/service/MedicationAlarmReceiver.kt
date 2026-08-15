package com.nkwabyte.medilert.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.nkwabyte.medilert.MainActivity
import com.nkwabyte.medilert.R
import com.nkwabyte.medilert.util.SoundPlayer

class MedicationAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "medilert_medication_reminders"
        const val ACTION_STOP_ALARM = "com.nkwabyte.medilert.ACTION_STOP_ALARM"
        const val EXTRA_ID = "extra_id"
        const val EXTRA_MED_NAME = "extra_med_name"
        const val EXTRA_DOSE = "extra_dose"
        const val EXTRA_UNIT = "extra_unit"
        const val EXTRA_TONE = "extra_tone"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_STOP_ALARM) {
            SoundPlayer.stopNotificationSound()
            val id = intent.getStringExtra(EXTRA_ID) ?: ""
            if (id.isNotBlank()) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(id.hashCode())
            }
            return
        }

        val id = intent.getStringExtra(EXTRA_ID) ?: System.currentTimeMillis().toString()
        val medName = intent.getStringExtra(EXTRA_MED_NAME) ?: "Medication"
        val dose = intent.getStringExtra(EXTRA_DOSE) ?: ""
        val unit = intent.getStringExtra(EXTRA_UNIT) ?: ""
        val tone = intent.getStringExtra(EXTRA_TONE) ?: "Bell"

        // 1. Play continuous 20-30s sound alert
        try {
            SoundPlayer.playNotificationSound(tone)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Post high-priority notification with full-screen banner and silence action
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "reminder")
            putExtra("medication_name", medName)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            id.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            action = ACTION_STOP_ALARM
            putExtra(EXTRA_ID, id)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            (id + "_stop").hashCode(),
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dosageText = if (dose.isNotBlank()) "Take $dose $unit of $medName" else "Time to take your scheduled dose of $medName"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⏰ Medication Reminder: $medName")
            .setContentText(dosageText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$dosageText. Tap to confirm or log your dose in MediLert."))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .addAction(android.R.drawable.ic_lock_power_off, "Silence Alarm", stopPendingIntent)
            .build()

        notificationManager.notify(id.hashCode(), notification)
    }

    private fun createNotificationChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Medication Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Urgent alerts and sounds when it is time to take prescribed medications."
                    enableLights(true)
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }
}
