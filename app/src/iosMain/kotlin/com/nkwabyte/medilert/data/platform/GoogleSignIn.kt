package com.nkwabyte.medilert.data.platform

import com.nkwabyte.medilert.data.FirebaseResult

actual suspend fun getGoogleIdToken(): FirebaseResult<String> {
    return FirebaseResult.Error("Google Sign-In is not supported on iOS in this build", null)
}

actual suspend fun clearAuthCredentials() {
    // No-op on iOS
}
