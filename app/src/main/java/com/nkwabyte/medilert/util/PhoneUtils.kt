package com.nkwabyte.medilert.util

/**
 * Utility functions for Ghana phone number validation and formatting
 */
object PhoneUtils {
    /**
     * Formats a Ghana phone number to E.164 format (+233XXXXXXXXX)
     * @param phone The phone number to format
     * @return Formatted phone number or null if invalid
     */
    fun formatGhanaPhoneNumber(phone: String): String? {
        // Remove all non-digit characters
        val digitsOnly = phone.replace(Regex("[^0-9]"), "")

        return when (// 10 digits starting with 0 (e.g., 0241234567)
            digitsOnly.length) {
            10 if digitsOnly.startsWith("0") -> {
                "+233${digitsOnly.substring(1)}"
            }
            // 9 digits not starting with 0 (e.g., 241234567)
            9 if !digitsOnly.startsWith("0") -> {
                "+233$digitsOnly"
            }
            // Already in international format with +233
            12 if phone.startsWith("+233") -> {
                "+233${digitsOnly.substring(3)}"
            }
            // Already in international format without +
            12 if digitsOnly.startsWith("233") -> {
                "+$digitsOnly"
            }

            else -> null
        }
    }

    /**
     * Validates if a phone number can be formatted as a Ghana number
     * @param phone The phone number to validate
     * @return Error message if invalid, null if valid
     */
    fun validateGhanaPhoneNumber(phone: String): String? {
        val digitsOnly = phone.replace(Regex("[^0-9]"), "")

        return when {
            digitsOnly.isEmpty() -> "Phone number is required"
            digitsOnly.length == 10 && digitsOnly.startsWith("0") -> null // Valid
            digitsOnly.length == 9 && !digitsOnly.startsWith("0") -> null // Valid
            digitsOnly.length == 12 && (phone.startsWith("+233") || digitsOnly.startsWith("233")) -> null // Valid
            digitsOnly.length < 9 -> "Phone number is too short"
            digitsOnly.length > 12 -> "Phone number is too long"
            digitsOnly.length == 10 && !digitsOnly.startsWith("0") -> "10-digit number must start with 0"
            else -> "Invalid Ghana phone number format. Use 10 digits (0XXXXXXXXX) or 9 digits (XXXXXXXXX)"
        }
    }

    /**
     * Checks if the input looks like an email address
     * @param input The input to check
     * @return true if it looks like an email
     */
    fun isEmail(input: String): Boolean {
        return input.contains("@") && input.contains(".")
    }
}

