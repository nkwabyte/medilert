package com.nkwabyte.medilert.model

data class MedicationSchedule(
    val id: String = java.util.UUID.randomUUID().toString(),
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

enum class DoseStatus {
    TAKEN, MISSED, UPCOMING, SKIPPED
}

data class Patient(
    val id: String = "",
    val name: String = "",
    val firstName: String = "",
    val condition: String = "",
    val adherence: Int = 0,
    val dosesTaken: Int = 0,
    val dosesTotal: Int = 0
)
