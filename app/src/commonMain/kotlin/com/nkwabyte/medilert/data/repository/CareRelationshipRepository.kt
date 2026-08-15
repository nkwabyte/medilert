package com.nkwabyte.medilert.data.repository

import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.model.CareAssignment
import com.nkwabyte.medilert.model.DoseStatus
import com.nkwabyte.medilert.model.Medication
import com.nkwabyte.medilert.model.MedicationSchedule
import com.nkwabyte.medilert.model.User
import com.nkwabyte.medilert.model.UserRole
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.firestore
import kotlinx.datetime.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class CareRelationshipRepository {
    private val firestore get() = Firebase.firestore
    private val auth get() = Firebase.auth

    private val uid get() = auth.currentUser?.uid ?: ""

    private val assignments get() = firestore.collection("careAssignments")

    private fun userDoc(id: String) = firestore.collection("users").document(id)
    private fun assignmentId(caregiverId: String, patientId: String) = "${caregiverId}_${patientId}"

    private fun parseUser(doc: DocumentSnapshot): User? {
        if (!doc.exists) return null
        // 1. Try standard kotlinx deserialization first
        try {
            val user = doc.data<User>()
            val resolvedId = if (user.id.isNotBlank()) user.id else doc.id
            return user.copy(id = resolvedId)
        } catch (_: Exception) { }

        // 2. Fallback: extract fields manually if document has custom/missing properties
        return try {
            val name = try { doc.get<String>("name") } catch (_: Exception) { "" }
            val email = try { doc.get<String>("email") } catch (_: Exception) { "" }
            val phone = try { doc.get<String>("phone") } catch (_: Exception) { "" }
            val gender = try { doc.get<String>("gender") } catch (_: Exception) { "" }
            val dateOfBirth = try { doc.get<String>("dateOfBirth") } catch (_: Exception) { "" }
            val specialty = try { doc.get<String>("specialty") } catch (_: Exception) { "" }
            val photoUrl = try { doc.get<String>("photoUrl") } catch (_: Exception) {
                try { doc.get<String>("photoURL") } catch (_: Exception) {
                    try { doc.get<String>("avatarUrl") } catch (_: Exception) {
                        try { doc.get<String>("profileImage") } catch (_: Exception) { "" }
                    }
                }
            }
            val caregiverId = try { doc.get<String>("caregiverId") } catch (_: Exception) { "" }
            val lastActiveAt = try { doc.get<Long>("lastActiveAt") } catch (_: Exception) {
                try { doc.get<Double>("lastActiveAt").toLong() } catch (_: Exception) { 0L }
            }
            val roleStr = try {
                doc.get<String>("role")
            } catch (_: Exception) {
                try {
                    doc.get<UserRole>("role").name
                } catch (_: Exception) {
                    ""
                }
            }

            val role = when (roleStr.uppercase().trim()) {
                "DOCTOR" -> UserRole.DOCTOR
                "PHARMACIST" -> UserRole.PHARMACIST
                "GUARDIAN", "CAREGIVER" -> UserRole.GUARDIAN
                else -> UserRole.PATIENT
            }

            User(
                id = doc.id,
                name = name,
                email = email,
                phone = phone,
                gender = gender,
                dateOfBirth = dateOfBirth,
                role = role,
                specialty = specialty,
                photoUrl = photoUrl,
                caregiverId = caregiverId,
                lastActiveAt = lastActiveAt
            )
        } catch (_: Exception) {
            null
        }
    }

    fun userProfileFlow(userId: String): Flow<User?> {
        if (userId.isBlank()) return kotlinx.coroutines.flow.flowOf(null)
        return userDoc(userId).snapshots
            .map { snap -> if (snap.exists) parseUser(snap) else null }
            .catch { emit(null) }
    }

    fun observeUserPresenceFlow(userId: String): Flow<Long> {
        if (userId.isBlank()) return kotlinx.coroutines.flow.flowOf(0L)
        return userDoc(userId).snapshots
            .map { snap ->
                if (!snap.exists) 0L
                else {
                    try { snap.get<Long>("lastActiveAt") } catch (_: Exception) {
                        try { snap.get<Double>("lastActiveAt").toLong() } catch (_: Exception) { 0L }
                    }
                }
            }
            .catch { emit(0L) }
    }

    suspend fun updateUserPresence(userId: String): FirebaseResult<Unit> {
        if (userId.isBlank()) return FirebaseResult.Success(Unit)
        return try {
            userDoc(userId).set(
                mapOf("lastActiveAt" to Clock.System.now().toEpochMilliseconds()),
                merge = true
            )
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to update presence", e)
        }
    }

    suspend fun assignPatient(
        caregiverId: String,
        patientId: String,
        caregiverName: String,
        patientName: String
    ): FirebaseResult<Unit> {
        return try {
            val assignment = CareAssignment(
                id = assignmentId(caregiverId, patientId),
                patientId = patientId,
                caregiverId = caregiverId,
                caregiverName = caregiverName,
                patientName = patientName,
                assignedAt = Clock.System.now().toEpochMilliseconds()
            )
            val batch = firestore.batch()
            batch.set(assignments.document(assignment.id), assignment)
            batch.update(userDoc(patientId), "caregiverId" to caregiverId)
            batch.commit()
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to assign patient", e)
        }
    }

    suspend fun unassignPatient(caregiverId: String, patientId: String): FirebaseResult<Unit> {
        return try {
            val batch = firestore.batch()
            batch.delete(assignments.document(assignmentId(caregiverId, patientId)))
            batch.update(userDoc(patientId), "caregiverId" to "")
            batch.commit()
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to unassign patient", e)
        }
    }

    fun assignedPatientsFlow(caregiverId: String): Flow<List<CareAssignment>> =
        assignments.where { "caregiverId" equalTo caregiverId }.snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull {
                    try { it.data<CareAssignment>() } catch (_: Exception) { null }
                }
            }
            .catch { emit(emptyList()) }

    fun assignedPatientProfilesFlow(caregiverId: String): Flow<List<User>> =
        kotlinx.coroutines.flow.combine(
            assignments.where { "caregiverId" equalTo caregiverId }.snapshots
                .map { snap -> snap.documents.mapNotNull { try { it.data<CareAssignment>().patientId } catch (_: Exception) { null } } }
                .catch { emit(emptyList()) },
            firestore.collection("users").snapshots
                .map { snap -> snap.documents.mapNotNull { parseUser(it) } }
                .catch { emit(emptyList()) }
        ) { assignedIds, allUsers ->
            val idSet = assignedIds.toSet()
            allUsers.filter { user ->
                user.role == UserRole.PATIENT && (user.id in idSet || user.caregiverId == caregiverId)
            }.distinctBy { it.id }.sortedBy { it.name.lowercase() }
        }

    fun patientAssignedCaregiverIdFlow(patientId: String): Flow<String?> =
        assignments.where { "patientId" equalTo patientId }.snapshots
            .map { snap ->
                snap.documents.firstOrNull()?.let {
                    try { it.data<CareAssignment>().caregiverId } catch (_: Exception) { null }
                }
            }
            .catch { emit(null) }

    fun allPatientsFlow(): Flow<List<User>> =
        firestore.collection("users").snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val user = parseUser(doc)
                    if (user != null && user.id != uid && user.role == UserRole.PATIENT) {
                        user
                    } else null
                }.sortedBy { it.name.lowercase() }
            }
            .catch { emit(emptyList()) }

    fun allDoctorsAndCaregiversFlow(): Flow<List<User>> =
        firestore.collection("users").snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val user = parseUser(doc)
                    if (user != null && user.id != uid && (user.role == UserRole.DOCTOR || user.role == UserRole.PHARMACIST || user.role == UserRole.GUARDIAN)) {
                        user
                    } else null
                }.sortedBy { it.name.lowercase() }
            }
            .catch { emit(emptyList()) }

    suspend fun getPatientProfile(patientId: String): FirebaseResult<User> {
        return try {
            val snapshot = userDoc(patientId).get()
            val user = parseUser(snapshot)
            if (user != null) FirebaseResult.Success(user)
            else FirebaseResult.Error("Patient not found")
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to load patient profile", e)
        }
    }

    suspend fun getCaregiverProfile(caregiverId: String): FirebaseResult<User> {
        return try {
            val snapshot = userDoc(caregiverId).get()
            val user = parseUser(snapshot)
            if (user != null) FirebaseResult.Success(user)
            else FirebaseResult.Error("Caregiver not found")
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to load caregiver profile", e)
        }
    }

    fun patientDoseRecordsFlow(patientId: String): Flow<List<MedicationSchedule>> =
        userDoc(patientId).collection("schedules").snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull {
                    try { it.data<MedicationSchedule>() } catch (_: Exception) { null }
                }.filter { it.status != DoseStatus.UPCOMING }
            }
            .catch { emit(emptyList()) }

    fun patientMedicationsFlow(patientId: String): Flow<List<Medication>> =
        userDoc(patientId).collection("medications").snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull {
                    try { it.data<Medication>() } catch (_: Exception) { null }
                }
            }
            .catch { emit(emptyList()) }
}
