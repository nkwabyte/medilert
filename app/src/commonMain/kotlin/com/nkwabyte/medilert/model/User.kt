package com.nkwabyte.medilert.model

import kotlinx.datetime.Clock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

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
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val caregiverId: String = "",
    val lastActiveAt: Long = 0L
)

@Serializable
data class UserPreferences(
    val language: String = "en",
    val theme: String = "system",
    val voiceEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val fontSize: String = "medium",
    val missedAlertsEnabled: Boolean = true,
    val lowAdherenceEnabled: Boolean = true
)

@Serializable(with = UserRoleSerializer::class)
enum class UserRole {
    PATIENT, DOCTOR, PHARMACIST, GUARDIAN
}

object UserRoleSerializer : KSerializer<UserRole> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UserRole", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: UserRole) = encoder.encodeString(value.name)
    override fun deserialize(decoder: Decoder): UserRole {
        val str = try { decoder.decodeString().uppercase().trim() } catch (_: Exception) { "PATIENT" }
        return when (str) {
            "DOCTOR" -> UserRole.DOCTOR
            "PHARMACIST" -> UserRole.PHARMACIST
            "GUARDIAN", "CAREGIVER" -> UserRole.GUARDIAN
            else -> UserRole.PATIENT
        }
    }
}
