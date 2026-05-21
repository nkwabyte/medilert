package com.nkwabyte.medilert.util

object PhoneUtils {
    fun formatGhanaPhoneNumber(phone: String): String? {
        val digitsOnly = phone.replace(Regex("[^0-9]"), "")

        return when (digitsOnly.length) {
            10 -> if (digitsOnly.startsWith("0")) "+233${digitsOnly.substring(1)}" else null
            9 -> if (!digitsOnly.startsWith("0")) "+233$digitsOnly" else null
            12 -> when {
                phone.startsWith("+233") -> "+233${digitsOnly.substring(3)}"
                digitsOnly.startsWith("233") -> "+$digitsOnly"
                else -> null
            }
            else -> null
        }
    }

    fun validateGhanaPhoneNumber(phone: String): String? {
        val digitsOnly = phone.replace(Regex("[^0-9]"), "")

        return when {
            digitsOnly.isEmpty() -> "Phone number is required"
            digitsOnly.length == 10 && digitsOnly.startsWith("0") -> null
            digitsOnly.length == 9 && !digitsOnly.startsWith("0") -> null
            digitsOnly.length == 12 && (phone.startsWith("+233") || digitsOnly.startsWith("233")) -> null
            digitsOnly.length < 9 -> "Phone number is too short"
            digitsOnly.length > 12 -> "Phone number is too long"
            digitsOnly.length == 10 && !digitsOnly.startsWith("0") -> "10-digit number must start with 0"
            else -> "Invalid Ghana phone number format. Use 10 digits (0XXXXXXXXX) or 9 digits (XXXXXXXXX)"
        }
    }

    fun isEmail(input: String): Boolean = input.contains("@") && input.contains(".")
}
