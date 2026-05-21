package com.nkwabyte.medilert.model

import kotlin.time.Clock
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
