package com.nkwabyte.medilert.data.platform

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.nkwabyte.medilert.BuildConfig
import com.nkwabyte.medilert.data.FirebaseResult

actual suspend fun getGoogleIdToken(): FirebaseResult<String> {
    val activity = AndroidActivityHolder.activity
        ?: return FirebaseResult.Error("No activity available", null)
    return try {
        val credentialManager = CredentialManager.create(activity)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        val result = credentialManager.getCredential(activity, request)
        val token = GoogleIdTokenCredential.createFrom(result.credential.data).idToken
        FirebaseResult.Success(token)
    } catch (e: GetCredentialException) {
        FirebaseResult.Error(e.message ?: "Google sign-in failed", e)
    } catch (e: Exception) {
        FirebaseResult.Error(e.message ?: "Google sign-in failed", e)
    }
}

actual suspend fun clearAuthCredentials() {
    val activity = AndroidActivityHolder.activity ?: return
    try {
        CredentialManager.create(activity).clearCredentialState(ClearCredentialStateRequest())
    } catch (_: Exception) { }
}
