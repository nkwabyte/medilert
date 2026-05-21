package com.nkwabyte.medilert.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.nkwabyte.medilert.data.FirebaseResult
import kotlinx.coroutines.tasks.await

/** User's notification preferences stored in Firestore under users/{uid}/notificationPrefs */
data class NotificationPreferences(
    val remindersEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val missedDoseAlerts: Boolean = true,
    val lowAdherenceAlerts: Boolean = true
)

class NotificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val messaging: FirebaseMessaging = FirebaseMessaging.getInstance()
) {
    private val uid get() = auth.currentUser?.uid ?: error("No authenticated user")
    private fun userDoc() = firestore.collection("users").document(uid)

    // ── FCM Token ─────────────────────────────────────────────────────────────

    /** Fetches the current FCM token and saves it to the user's Firestore document. */
    suspend fun saveFcmToken(): FirebaseResult<Unit> {
        return try {
            val token = messaging.token.await()
            userDoc().update("fcmToken", token).await()
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to save FCM token", e)
        }
    }

    // ── Topic subscriptions ───────────────────────────────────────────────────

    suspend fun subscribeToTopic(topic: String) {
        try { messaging.subscribeToTopic(topic).await() } catch (_: Exception) { }
    }

    suspend fun unsubscribeFromTopic(topic: String) {
        try { messaging.unsubscribeFromTopic(topic).await() } catch (_: Exception) { }
    }

    // ── Notification Preferences ──────────────────────────────────────────────

    suspend fun getNotificationPreferences(): FirebaseResult<NotificationPreferences> {
        return try {
            val snapshot = userDoc().get().await()
            @Suppress("UNCHECKED_CAST")
            val map = snapshot.get("notificationPrefs") as? Map<String, Any>
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
                "notificationPrefs", mapOf(
                    "remindersEnabled" to prefs.remindersEnabled,
                    "soundEnabled" to prefs.soundEnabled,
                    "vibrationEnabled" to prefs.vibrationEnabled,
                    "missedDoseAlerts" to prefs.missedDoseAlerts,
                    "lowAdherenceAlerts" to prefs.lowAdherenceAlerts
                )
            ).await()
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to update notification preferences", e)
        }
    }
}
