package com.nkwabyte.medilert.data.repository

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.nkwabyte.medilert.data.FirebaseResult
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class AuthRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isLoggedIn: Boolean get() = currentUser != null

    // Store verification ID for phone auth
    private var verificationId: String? = null

    /** Email + password sign-in */
    suspend fun signInWithEmail(email: String, password: String): FirebaseResult<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            FirebaseResult.Success(result.user!!)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Sign-in failed", e)
        }
    }

    /** Email + password registration */
    suspend fun registerWithEmail(email: String, password: String): FirebaseResult<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            FirebaseResult.Success(result.user!!)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Registration failed", e)
        }
    }

    /** Google Sign-In via Credential Manager (modern API) */
    suspend fun signInWithGoogle(
        context: Context,
        webClientId: String
    ): FirebaseResult<FirebaseUser> {
        return try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

            val authResult = auth.signInWithCredential(firebaseCredential).await()
            FirebaseResult.Success(authResult.user!!)
        } catch (e: GetCredentialException) {
            FirebaseResult.Error(e.message ?: "Google sign-in failed", e)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Google sign-in failed", e)
        }
    }

    /** Password reset email */
    suspend fun sendPasswordResetEmail(email: String): FirebaseResult<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to send reset email", e)
        }
    }

    /** Sign out and clear Credential Manager state */
    suspend fun signOut(context: Context) {
        auth.signOut()
        try {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) { /* ignore */ }
    }

    /**
     * Send SMS verification code to phone number
     * @param activity Required for PhoneAuth
     * @param phoneNumber Phone number in E.164 format (+233XXXXXXXXX)
     * @param onCodeSent Callback when code is sent successfully
     * @param onVerificationFailed Callback when verification fails
     */
    fun sendVerificationCode(
        activity: Activity,
        phoneNumber: String,
        onCodeSent: (String) -> Unit,
        onVerificationFailed: (String) -> Unit
    ) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-retrieval or instant verification
                verificationId = null
            }

            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                onVerificationFailed(e.message ?: "Verification failed")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                this@AuthRepository.verificationId = verificationId
                onCodeSent(verificationId)
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /**
     * Verify the SMS code and sign in
     * @param verificationId The verification ID from onCodeSent
     * @param code The SMS code entered by user
     */
    suspend fun verifyPhoneCode(verificationId: String, code: String): FirebaseResult<FirebaseUser> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            val result = auth.signInWithCredential(credential).await()
            FirebaseResult.Success(result.user!!)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Verification failed", e)
        }
    }
}
