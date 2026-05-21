package com.nkwabyte.medilert.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Manages app preferences like onboarding completion status, remember me, and session management
 */
class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "medilert_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_LAST_LOGIN_EMAIL = "last_login_email"
        private const val KEY_LAST_ACTIVITY_TIME = "last_activity_time"
        private const val SESSION_TIMEOUT_HOURS = 12

        @Volatile
        private var instance: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: PreferencesManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    /**
     * Check if user has completed onboarding
     */
    fun hasCompletedOnboarding(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    /**
     * Mark onboarding as completed
     */
    fun setOnboardingCompleted() {
        prefs.edit { putBoolean(KEY_ONBOARDING_COMPLETED, true) }
    }

    /**
     * Check if remember me is enabled
     */
    fun isRememberMeEnabled(): Boolean {
        return prefs.getBoolean(KEY_REMEMBER_ME, false)
    }

    /**
     * Set remember me preference
     */
    fun setRememberMe(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_REMEMBER_ME, enabled) }
    }

    /**
     * Get last login email
     */
    fun getLastLoginEmail(): String? {
        return prefs.getString(KEY_LAST_LOGIN_EMAIL, null)
    }

    /**
     * Save last login email
     */
    fun saveLastLoginEmail(email: String) {
        prefs.edit { putString(KEY_LAST_LOGIN_EMAIL, email) }
    }

    /**
     * Clear last login email
     */
    fun clearLastLoginEmail() {
        prefs.edit { remove(KEY_LAST_LOGIN_EMAIL) }
    }

    /**
     * Update last activity time to current time
     */
    fun updateLastActivityTime() {
        val currentTime = System.currentTimeMillis()
        prefs.edit { putLong(KEY_LAST_ACTIVITY_TIME, currentTime) }
    }

    /**
     * Get last activity time
     */
    fun getLastActivityTime(): Long {
        return prefs.getLong(KEY_LAST_ACTIVITY_TIME, 0L)
    }

    /**
     * Check if session has expired (12 hours of inactivity)
     * Returns true if session is expired based on last activity time
     * Note: Session expiration is independent of remember me status
     */
    fun isSessionExpired(): Boolean {
        val lastActivityTime = getLastActivityTime()
        if (lastActivityTime == 0L) {
            // No activity recorded yet, session is not expired
            return false
        }

        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - lastActivityTime
        val hoursSinceLastActivity = timeDifference / (1000 * 60 * 60)

        return hoursSinceLastActivity >= SESSION_TIMEOUT_HOURS
    }

    /**
     * Clear session data (called on logout)
     */
    fun clearSession() {
        prefs.edit {
            remove(KEY_LAST_ACTIVITY_TIME)
            if (!isRememberMeEnabled()) {
                remove(KEY_LAST_LOGIN_EMAIL)
            }
        }
    }

    /**
     * Clear all preferences (useful for complete logout)
     */
    fun clear() {
        prefs.edit { clear() }
    }
}

