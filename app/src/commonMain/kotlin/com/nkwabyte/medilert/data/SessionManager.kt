package com.nkwabyte.medilert.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SessionManager(
    private val prefsManager: PreferencesManager,
    private val onSessionExpired: suspend () -> Unit
) {
    constructor(onSessionExpired: suspend () -> Unit) : this(PreferencesManager.instance, onSessionExpired)

    private val scope get() = CoroutineScope(Dispatchers.Default)

    private var sessionCheckJob: Job? = null

    companion object {
        private const val CHECK_INTERVAL_MS = 60_000L
    }

    fun startSessionTracking() {
        updateActivity()
        sessionCheckJob?.cancel()
        sessionCheckJob = scope.launch {
            while (true) {
                delay(CHECK_INTERVAL_MS)
                if (prefsManager.isSessionExpired()) {
                    onSessionExpired()
                    break
                }
            }
        }
    }

    fun stopSessionTracking() {
        sessionCheckJob?.cancel()
        sessionCheckJob = null
    }

    fun updateActivity() = prefsManager.updateLastActivityTime()

    fun isSessionValid(): Boolean = !prefsManager.isSessionExpired()
}
