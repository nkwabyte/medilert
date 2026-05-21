package com.nkwabyte.medilert.data.service

import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.data.PreferencesManager
import com.nkwabyte.medilert.data.repository.AuthRepository
import com.nkwabyte.medilert.data.repository.NotificationRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow

class AuthService(
    private val authRepository: AuthRepository = AuthRepository(),
    private val notificationRepository: NotificationRepository = NotificationRepository(),
    private val prefsManager: PreferencesManager = PreferencesManager.instance
) {
    val authStateFlow: Flow<FirebaseUser?> = Firebase.auth.authStateChanged

    val currentUser: FirebaseUser? get() = authRepository.currentUser
    val isLoggedIn: Boolean get() = authRepository.isLoggedIn

    suspend fun signInWithEmail(email: String, password: String): FirebaseResult<FirebaseUser> {
        val result = authRepository.signInWithEmail(email, password)
        if (result is FirebaseResult.Success) {
            postAuthSetup()
            prefsManager.updateLastActivityTime()
        }
        return result
    }

    suspend fun registerWithEmail(email: String, password: String): FirebaseResult<FirebaseUser> {
        val result = authRepository.registerWithEmail(email, password)
        if (result is FirebaseResult.Success) {
            postAuthSetup()
            prefsManager.updateLastActivityTime()
        }
        return result
    }

    suspend fun signInWithGoogle(): FirebaseResult<FirebaseUser> {
        val result = authRepository.signInWithGoogle()
        if (result is FirebaseResult.Success) {
            postAuthSetup()
            prefsManager.updateLastActivityTime()
        }
        return result
    }

    suspend fun sendPasswordResetEmail(email: String): FirebaseResult<Unit> =
        authRepository.sendPasswordResetEmail(email)

    fun sendPhoneVerificationCode(
        phoneNumber: String,
        onCodeSent: (String) -> Unit,
        onVerificationFailed: (String) -> Unit
    ) {
        authRepository.sendVerificationCode(phoneNumber, onCodeSent, onVerificationFailed)
    }

    suspend fun verifyPhoneCode(verificationId: String, code: String): FirebaseResult<FirebaseUser> {
        val result = authRepository.verifyPhoneCode(verificationId, code)
        if (result is FirebaseResult.Success) {
            postAuthSetup()
            prefsManager.updateLastActivityTime()
        }
        return result
    }

    suspend fun signOut() {
        try { notificationRepository.unsubscribeFromTopic("medication_reminders") } catch (_: Exception) {}
        authRepository.signOut()
        prefsManager.clearSession()
    }

    private suspend fun postAuthSetup() {
        try {
            notificationRepository.saveFcmToken()
            notificationRepository.subscribeToTopic("medication_reminders")
        } catch (_: Exception) {}
    }
}
