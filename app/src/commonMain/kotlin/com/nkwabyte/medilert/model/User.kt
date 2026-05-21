package com.nkwabyte.medilert.model

import kotlin.time.Clock
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val gender: String = "",
    val dateOfBirth: String = "",
    val role: UserRole = UserRole.PATIENT,
    val pin: String = "",
    val photoUrl: String = "",
    val specialty: String = "",
    val emergencyContact: String = "",
    val preferences: UserPreferences = UserPreferences(),
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds(),
    val caregiverId: String = ""
)

@Serializable
data class UserPreferences(
    val language: String = "en",
    val theme: String = "system",
    val voiceEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val fontSize: String = "medium"
)

@Serializable
enum class UserRole {
    PATIENT, DOCTOR, PHARMACIST, GUARDIAN
}
