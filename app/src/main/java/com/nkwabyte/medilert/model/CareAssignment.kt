package com.nkwabyte.medilert.model

/**
 * Represents a care relationship between a caregiver and a patient.
 *
 * Stored in Firestore under:  careAssignments/{caregiverId}_{patientId}
 *
 * Deterministic document IDs enable server-side security-rule existence checks
 * without requiring a collection-group query, which keeps rules simple and fast.
 */
data class CareAssignment(
    val id: String = "",
    val patientId: String = "",
    val caregiverId: String = "",
    val patientName: String = "",
    val caregiverName: String = "",
    val assignedAt: Long = System.currentTimeMillis()
)
