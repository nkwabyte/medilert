package com.nkwabyte.medilert.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Manages user session timeout and activity tracking
 * Automatically logs out users after 12 hours of inactivity
 */
class SessionManager(
    private val context: Context,
    private val onSessionExpired: suspend () -> Unit
) {
    private val prefsManager = PreferencesManager.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.Default)
    private var sessionCheckJob: Job? = null

    companion object {
        // Check every 1 minute if session has expired
        private const val CHECK_INTERVAL_MS = 60_000L
    }

    /**
     * Start tracking user session
     */
    fun startSessionTracking() {
        // Update activity time when starting
        updateActivity()

        // Cancel existing job if any
        sessionCheckJob?.cancel()

        // Start periodic session check
        sessionCheckJob = scope.launch {
            while (true) {
                delay(CHECK_INTERVAL_MS)

                if (prefsManager.isSessionExpired()) {
                    // Session expired, logout user
                    onSessionExpired()
                    break
                }
            }
        }
    }

    /**
     * Stop session tracking (called on logout or app closure)
     */
    fun stopSessionTracking() {
        sessionCheckJob?.cancel()
        sessionCheckJob = null
    }

    /**
     * Update last activity time (call this on user interactions)
     */
    fun updateActivity() {
        prefsManager.updateLastActivityTime()
    }

    /**
     * Check if current session is valid
     */
    fun isSessionValid(): Boolean {
        return !prefsManager.isSessionExpired()
    }
}

