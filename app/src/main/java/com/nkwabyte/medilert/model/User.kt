package com.nkwabyte.medilert.model

data class User(
    val id: String = "",  // Will be set to Firebase UID
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val gender: String = "",
    val dateOfBirth: String = "",
    val role: UserRole = UserRole.PATIENT,
    val pin: String = "",
    val photoUrl: String = "",
    val specialty: String = "",  // For DOCTOR/PHARMACIST
    val emergencyContact: String = "",
    val preferences: UserPreferences = UserPreferences(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    /**
     * For PATIENT accounts: the UID of their assigned caregiver.
     * Empty string when unassigned. Mirrors the careAssignments collection
     * so the patient can look up their caregiver in a single document read.
     */
    val caregiverId: String = ""
)

data class UserPreferences(
    val language: String = "en",  // en, fr, tw (Twi), etc.
    val theme: String = "system",  // light, dark, system
    val voiceEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val fontSize: String = "medium"  // small, medium, large
)

enum class UserRole {
    PATIENT, DOCTOR, PHARMACIST, GUARDIAN
}
