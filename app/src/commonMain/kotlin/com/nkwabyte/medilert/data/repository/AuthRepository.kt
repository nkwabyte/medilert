package com.nkwabyte.medilert.data.repository

import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.data.platform.clearAuthCredentials
import com.nkwabyte.medilert.data.platform.getGoogleIdToken
import com.nkwabyte.medilert.data.platform.sendPhoneVerificationCode
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.PhoneAuthProvider
import dev.gitlive.firebase.auth.auth

class AuthRepository {
    private val auth = Firebase.auth

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isLoggedIn: Boolean get() = currentUser != null

    suspend fun signInWithEmail(email: String, password: String): FirebaseResult<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password)
            FirebaseResult.Success(result.user!!)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Sign-in failed", e)
        }
    }

    suspend fun registerWithEmail(email: String, password: String): FirebaseResult<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password)
            FirebaseResult.Success(result.user!!)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Registration failed", e)
        }
    }

    suspend fun signInWithGoogle(): FirebaseResult<FirebaseUser> {
        return when (val tokenResult = getGoogleIdToken()) {
            is FirebaseResult.Error -> tokenResult
            is FirebaseResult.Success -> {
                try {
                    val credential = GoogleAuthProvider.credential(tokenResult.data, null)
                    val result = auth.signInWithCredential(credential)
                    FirebaseResult.Success(result.user!!)
                } catch (e: Exception) {
                    FirebaseResult.Error(e.message ?: "Google sign-in failed", e)
                }
            }
            FirebaseResult.Loading -> FirebaseResult.Error("Unexpected state")
        }
    }

    suspend fun sendPasswordResetEmail(email: String): FirebaseResult<Unit> {
        return try {
            auth.sendPasswordResetEmail(email)
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to send reset email", e)
        }
    }

    suspend fun signOut() {
        auth.signOut()
        try { clearAuthCredentials() } catch (_: Exception) {}
    }

    fun sendVerificationCode(
        phoneNumber: String,
        onCodeSent: (String) -> Unit,
        onVerificationFailed: (String) -> Unit
    ) {
        sendPhoneVerificationCode(phoneNumber, onCodeSent, onVerificationFailed)
    }

    suspend fun verifyPhoneCode(verificationId: String, code: String): FirebaseResult<FirebaseUser> {
        return try {
            val credential = PhoneAuthProvider(auth).credential(verificationId, code)
            val result = auth.signInWithCredential(credential)
            FirebaseResult.Success(result.user!!)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Verification failed", e)
        }
    }
}
