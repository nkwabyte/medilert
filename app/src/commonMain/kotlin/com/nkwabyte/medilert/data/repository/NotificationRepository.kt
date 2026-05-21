package com.nkwabyte.medilert.data.repository

import com.nkwabyte.medilert.data.FirebaseResult
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.messaging.messaging

data class NotificationPreferences(
    val remindersEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val missedDoseAlerts: Boolean = true,
    val lowAdherenceAlerts: Boolean = true
)

class NotificationRepository {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth
    private val messaging = Firebase.messaging

    private val uid get() = auth.currentUser?.uid ?: error("No authenticated user")
    private fun userDoc() = firestore.collection("users").document(uid)

    suspend fun saveFcmToken(): FirebaseResult<Unit> {
        return try {
            val token = messaging.getToken()
            userDoc().update("fcmToken" to token)
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to save FCM token", e)
        }
    }

    suspend fun subscribeToTopic(topic: String) {
        try { messaging.subscribeToTopic(topic) } catch (_: Exception) {}
    }

    suspend fun unsubscribeFromTopic(topic: String) {
        try { messaging.unsubscribeFromTopic(topic) } catch (_: Exception) {}
    }

    suspend fun getNotificationPreferences(): FirebaseResult<NotificationPreferences> {
        return try {
            val snapshot = userDoc().get()
            val map = snapshot.get<Map<String, Any>?>("notificationPrefs")
            val prefs = if (map != null) {
                NotificationPreferences(
                    remindersEnabled = map["remindersEnabled"] as? Boolean ?: true,
                    soundEnabled = map["soundEnabled"] as? Boolean ?: false,
                    vibrationEnabled = map["vibrationEnabled"] as? Boolean ?: true,
                    missedDoseAlerts = map["missedDoseAlerts"] as? Boolean ?: true,
                    lowAdherenceAlerts = map["lowAdherenceAlerts"] as? Boolean ?: true
                )
            } else {
                NotificationPreferences()
            }
            FirebaseResult.Success(prefs)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to load notification preferences", e)
        }
    }

    suspend fun updateNotificationPreferences(prefs: NotificationPreferences): FirebaseResult<Unit> {
        return try {
            userDoc().update(
                "notificationPrefs" to mapOf(
                    "remindersEnabled" to prefs.remindersEnabled,
                    "soundEnabled" to prefs.soundEnabled,
                    "vibrationEnabled" to prefs.vibrationEnabled,
                    "missedDoseAlerts" to prefs.missedDoseAlerts,
                    "lowAdherenceAlerts" to prefs.lowAdherenceAlerts
                )
            )
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to update notification preferences", e)
        }
    }
}
