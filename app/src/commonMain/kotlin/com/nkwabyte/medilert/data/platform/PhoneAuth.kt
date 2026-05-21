package com.nkwabyte.medilert.data.platform

expect fun sendPhoneVerificationCode(
    phoneNumber: String,
    onCodeSent: (String) -> Unit,
    onVerificationFailed: (String) -> Unit
)
