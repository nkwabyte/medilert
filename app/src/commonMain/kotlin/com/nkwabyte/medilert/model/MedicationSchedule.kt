@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.nkwabyte.medilert.model

import kotlinx.serialization.Serializable

@Serializable
data class MedicationSchedule(
    val id: String = kotlin.uuid.Uuid.random().toString(),
    val medicationId: String = "",
    val medicationName: String = "",
    val scheduledTime: String = "",
    val date: String = "",
    val status: DoseStatus = DoseStatus.UPCOMING,
    val takenAt: String = "",
    val dose: Int = 1,
    val unit: String = "tablet(s)",
    val sideEffects: String = ""
)

@Serializable
enum class DoseStatus {
    TAKEN, MISSED, UPCOMING, SKIPPED
}

@Serializable
data class Patient(
    val id: String = "",
    val name: String = "",
    val firstName: String = "",
    val condition: String = "",
    val adherence: Int = 0,
    val dosesTaken: Int = 0,
    val dosesTotal: Int = 0
)
