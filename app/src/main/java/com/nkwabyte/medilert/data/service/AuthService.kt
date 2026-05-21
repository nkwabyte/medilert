package com.nkwabyte.medilert.data.service

import android.app.Activity
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.nkwabyte.medilert.BuildConfig
import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.data.PreferencesManager
import com.nkwabyte.medilert.data.repository.AuthRepository
import com.nkwabyte.medilert.data.repository.NotificationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * AuthService sits between ViewModels and AuthRepository.
 * It owns the Firebase auth state stream and runs post-auth setup
 * (FCM token registration, topic subscriptions) after every sign-in.
 * Also manages session persistence and activity tracking.
 */
class AuthService(
    private val context: Context? = null,
    private val authRepository: AuthRepository = AuthRepository(),
    private val notificationRepository: NotificationRepository = NotificationRepository()
) {
    private val prefsManager: PreferencesManager? by lazy {
        context?.let { PreferencesManager.getInstance(it) }
    }

    /**
     * A cold Flow that emits the current FirebaseUser on every auth state change.
     * Emits null when the user is signed out.
     */
    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        FirebaseAuth.getInstance().addAuthStateListener(listener)
        awaitClose { FirebaseAuth.getInstance().removeAuthStateListener(listener) }
    }

    val currentUser: FirebaseUser? get() = authRepository.currentUser
    val isLoggedIn: Boolean get() = authRepository.isLoggedIn

    suspend fun signInWithEmail(email: String, password: String): FirebaseResult<FirebaseUser> {
        val result = authRepository.signInWithEmail(email, password)
        if (result is FirebaseResult.Success) {
            postAuthSetup()
            prefsManager?.updateLastActivityTime()
        }
        return result
    }

    suspend fun registerWithEmail(email: String, password: String): FirebaseResult<FirebaseUser> {
        val result = authRepository.registerWithEmail(email, password)
        if (result is FirebaseResult.Success) {
            postAuthSetup()
            prefsManager?.updateLastActivityTime()
        }
        return result
    }

    suspend fun signInWithGoogle(context: Context): FirebaseResult<FirebaseUser> {
        val result = authRepository.signInWithGoogle(context, BuildConfig.GOOGLE_WEB_CLIENT_ID)
        if (result is FirebaseResult.Success) {
            postAuthSetup()
            prefsManager?.updateLastActivityTime()
        }
        return result
    }

    suspend fun sendPasswordResetEmail(email: String): FirebaseResult<Unit> =
        authRepository.sendPasswordResetEmail(email)

    fun sendPhoneVerificationCode(
        activity: Activity,
        phoneNumber: String,
        onCodeSent: (String) -> Unit,
        onVerificationFailed: (String) -> Unit
    ) {
        authRepository.sendVerificationCode(activity, phoneNumber, onCodeSent, onVerificationFailed)
    }

    suspend fun verifyPhoneCode(verificationId: String, code: String): FirebaseResult<FirebaseUser> {
        val result = authRepository.verifyPhoneCode(verificationId, code)
        if (result is FirebaseResult.Success) {
            postAuthSetup()
            prefsManager?.updateLastActivityTime()
        }
        return result
    }

    suspend fun signOut(context: Context) {
        try {
            notificationRepository.unsubscribeFromTopic("medication_reminders")
        } catch (_: Exception) { }
        authRepository.signOut(context)
        // Clear session data but preserve onboarding status and remember me settings
        prefsManager?.clearSession()
    }

    /**
     * Called after every successful sign-in.
     * Non-fatal — auth is not rolled back if these fail.
     */
    private suspend fun postAuthSetup() {
        try {
            notificationRepository.saveFcmToken()
            notificationRepository.subscribeToTopic("medication_reminders")
        } catch (_: Exception) { }
    }
}
