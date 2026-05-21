package com.nkwabyte.medilert.data

import com.russhwolf.settings.Settings
import kotlin.time.Clock

class PreferencesManager {
    private val settings = Settings()

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_LAST_LOGIN_EMAIL = "last_login_email"
        private const val KEY_LAST_ACTIVITY_TIME = "last_activity_time"
        private const val SESSION_TIMEOUT_HOURS = 12L

        val instance: PreferencesManager by lazy { PreferencesManager() }
    }

    fun hasCompletedOnboarding(): Boolean =
        settings.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    fun setOnboardingCompleted() =
        settings.putBoolean(KEY_ONBOARDING_COMPLETED, true)

    fun isRememberMeEnabled(): Boolean =
        settings.getBoolean(KEY_REMEMBER_ME, false)

    fun setRememberMe(enabled: Boolean) =
        settings.putBoolean(KEY_REMEMBER_ME, enabled)

    fun getLastLoginEmail(): String? =
        settings.getStringOrNull(KEY_LAST_LOGIN_EMAIL)

    fun saveLastLoginEmail(email: String) =
        settings.putString(KEY_LAST_LOGIN_EMAIL, email)

    fun clearLastLoginEmail() =
        settings.remove(KEY_LAST_LOGIN_EMAIL)

    fun updateLastActivityTime() =
        settings.putLong(KEY_LAST_ACTIVITY_TIME, Clock.System.now().toEpochMilliseconds())

    fun getLastActivityTime(): Long =
        settings.getLong(KEY_LAST_ACTIVITY_TIME, 0L)

    fun isSessionExpired(): Boolean {
        val lastActivityTime = getLastActivityTime()
        if (lastActivityTime == 0L) return false
        val elapsed = Clock.System.now().toEpochMilliseconds() - lastActivityTime
        val hoursSinceLastActivity = elapsed / (1000L * 60L * 60L)
        return hoursSinceLastActivity >= SESSION_TIMEOUT_HOURS
    }

    fun clearSession() {
        settings.remove(KEY_LAST_ACTIVITY_TIME)
        if (!isRememberMeEnabled()) {
            settings.remove(KEY_LAST_LOGIN_EMAIL)
        }
    }

    fun clear() = settings.clear()
}
