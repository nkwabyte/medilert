package com.nkwabyte.medilert.data.platform

actual fun sendPhoneVerificationCode(
    phoneNumber: String,
    onCodeSent: (String) -> Unit,
    onVerificationFailed: (String) -> Unit
) {
    onVerificationFailed("Phone verification is not supported on iOS in this build")
}
