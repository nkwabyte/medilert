package com.nkwabyte.medilert.data.service

import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.data.repository.CareRelationshipRepository
import com.nkwabyte.medilert.model.CareAssignment
import com.nkwabyte.medilert.model.Medication
import com.nkwabyte.medilert.model.User
import com.nkwabyte.medilert.model.UserRole
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map

class CareRelationshipService(
    private val repository: CareRelationshipRepository = CareRelationshipRepository(),
    private val userService: UserService = UserService()
) {
    private val uid get() = Firebase.auth.currentUser?.uid ?: error("No authenticated user")

    fun assignedPatientsFlow(): Flow<List<CareAssignment>> =
        repository.assignedPatientsFlow(uid)

    fun assignedPatientProfilesFlow(): Flow<List<User>> =
        assignedPatientsFlow().map { assignments ->
            assignments.mapNotNull { assignment ->
                when (val result = repository.getPatientProfile(assignment.patientId)) {
                    is FirebaseResult.Success -> result.data
                    else -> null
                }
            }
        }

    fun allPatientProfilesFlow(callerRole: UserRole): Flow<List<User>> {
        if (callerRole !in listOf(UserRole.DOCTOR, UserRole.PHARMACIST, UserRole.GUARDIAN)) {
            return emptyFlow()
        }
        return repository.allPatientsFlow()
    }

    fun patientMedicationsFlow(patientId: String, callerRole: UserRole): Flow<List<Medication>> {
        if (callerRole !in listOf(UserRole.DOCTOR, UserRole.PHARMACIST, UserRole.GUARDIAN)) {
            return emptyFlow()
        }
        return repository.patientMedicationsFlow(patientId)
    }

    suspend fun assignPatient(
        patientId: String,
        callerRole: UserRole,
        caregiverName: String,
        patientName: String
    ): FirebaseResult<Unit> {
        if (callerRole !in listOf(UserRole.DOCTOR, UserRole.PHARMACIST, UserRole.GUARDIAN)) {
            return FirebaseResult.Error("Only caregivers can assign patients")
        }
        return repository.assignPatient(
            caregiverId = uid,
            patientId = patientId,
            caregiverName = caregiverName,
            patientName = patientName
        )
    }

    suspend fun unassignPatient(patientId: String, callerRole: UserRole): FirebaseResult<Unit> {
        if (callerRole !in listOf(UserRole.DOCTOR, UserRole.PHARMACIST, UserRole.GUARDIAN)) {
            return FirebaseResult.Error("Only caregivers can remove patient assignments")
        }
        return repository.unassignPatient(caregiverId = uid, patientId = patientId)
    }

    suspend fun getAssignedCaregiverProfile(caregiverId: String): FirebaseResult<User> {
        if (caregiverId.isBlank()) return FirebaseResult.Error("No caregiver assigned")
        return repository.getCaregiverProfile(caregiverId)
    }

    suspend fun getPatientProfile(patientId: String, callerRole: UserRole): FirebaseResult<User> {
        if (callerRole !in listOf(UserRole.DOCTOR, UserRole.PHARMACIST, UserRole.GUARDIAN)) {
            return FirebaseResult.Error("Only caregivers can view patient profiles")
        }
        return repository.getPatientProfile(patientId)
    }
}
