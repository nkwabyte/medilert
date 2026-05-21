package com.nkwabyte.medilert.data.platform

import com.nkwabyte.medilert.data.FirebaseResult

expect suspend fun getGoogleIdToken(): FirebaseResult<String>

expect suspend fun clearAuthCredentials()
