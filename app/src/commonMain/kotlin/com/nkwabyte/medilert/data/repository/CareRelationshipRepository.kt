package com.nkwabyte.medilert.data.repository

import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.model.CareAssignment
import com.nkwabyte.medilert.model.DoseStatus
import com.nkwabyte.medilert.model.Medication
import com.nkwabyte.medilert.model.MedicationSchedule
import com.nkwabyte.medilert.model.User
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class CareRelationshipRepository {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val uid get() = auth.currentUser?.uid ?: error("No authenticated user")

    private val assignments = firestore.collection("careAssignments")
    private fun userDoc(id: String) = firestore.collection("users").document(id)
    private fun assignmentId(caregiverId: String, patientId: String) = "${caregiverId}_${patientId}"

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

    fun allPatientsFlow(): Flow<List<User>> =
        firestore.collection("users").where { "role" equalTo "PATIENT" }.snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull {
                    try { it.data<User>() } catch (_: Exception) { null }
                }.filter { it.id != uid }
                 .sortedBy { it.name.lowercase() }
            }
            .catch { emit(emptyList()) }

    suspend fun getPatientProfile(patientId: String): FirebaseResult<User> {
        return try {
            val snapshot = userDoc(patientId).get()
            val user = if (snapshot.exists) snapshot.data<User>() else null
            if (user != null) FirebaseResult.Success(user)
            else FirebaseResult.Error("Patient not found")
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to load patient profile", e)
        }
    }

    suspend fun getCaregiverProfile(caregiverId: String): FirebaseResult<User> {
        return try {
            val snapshot = userDoc(caregiverId).get()
            val user = if (snapshot.exists) snapshot.data<User>() else null
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
