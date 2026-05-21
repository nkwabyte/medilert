package com.nkwabyte.medilert.data

sealed class FirebaseResult<out T> {
    data class Success<T>(val data: T) : FirebaseResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : FirebaseResult<Nothing>()
    data object Loading : FirebaseResult<Nothing>()
}
