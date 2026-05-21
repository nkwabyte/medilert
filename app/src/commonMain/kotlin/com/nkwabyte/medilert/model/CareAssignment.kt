package com.nkwabyte.medilert.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class CareAssignment(
    val id: String = "",
    val patientId: String = "",
    val caregiverId: String = "",
    val patientName: String = "",
    val caregiverName: String = "",
    val assignedAt: Long = Clock.System.now().toEpochMilliseconds()
)
