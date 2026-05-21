package com.nkwabyte.medilert.data.service

import com.google.firebase.auth.FirebaseAuth
import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.data.repository.CareRelationshipRepository
import com.nkwabyte.medilert.model.CareAssignment
import com.nkwabyte.medilert.model.Medication
import com.nkwabyte.medilert.model.User
import com.nkwabyte.medilert.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map

/**
 * CareRelationshipService enforces role-based access control in Kotlin before
 * any Firestore call is made.  Firestore Security Rules (firestore.rules) enforce
 * the same constraints server-side as a second layer of defense.
 *
 * Access model:
 *  - PATIENT : reads own data + read-only view of their assigned caregiver's profile.
 *  - DOCTOR / PHARMACIST / GUARDIAN : reads own profile + reads assigned patients'
 *    profiles and medications.  Cannot read unassigned patients.
 */
class CareRelationshipService(
    private val repository: CareRelationshipRepository = CareRelationshipRepository(),
    private val userService: UserService = UserService(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val uid get() = auth.currentUser?.uid ?: error("No authenticated user")

    // ── Caregiver-facing APIs ─────────────────────────────────────────────────

    /**
     * Live list of [CareAssignment] records for the currently authenticated
     * caregiver.  Returns an empty Flow if the caller is not a caregiver role.
     */
    fun assignedPatientsFlow(): Flow<List<CareAssignment>> =
        repository.assignedPatientsFlow(uid)

    /**
     * Live list of full [User] profiles for all patients assigned to the
     * current caregiver. Fetches profiles after each assignment change.
     */
    fun assignedPatientProfilesFlow(): Flow<List<User>> =
        assignedPatientsFlow().map { assignments ->
            assignments.mapNotNull { assignment ->
                when (val result = repository.getPatientProfile(assignment.patientId)) {
                    is FirebaseResult.Success -> result.data
                    else -> null
                }
            }
        }

    /**
     * Live list of all patient profiles for caregiver add-patient search.
     */
    fun allPatientProfilesFlow(callerRole: UserRole): Flow<List<User>> {
        if (callerRole !in listOf(UserRole.DOCTOR, UserRole.PHARMACIST, UserRole.GUARDIAN)) {
            return emptyFlow()
        }
        return repository.allPatientsFlow()
    }

    /**
     * Live medication list for a specific assigned patient.
     * Enforces that the requesting user is a caregiver role; returns empty Flow
     * otherwise (Firestore rules enforce the same server-side).
     */
    fun patientMedicationsFlow(
        patientId: String,
        callerRole: UserRole
    ): Flow<List<Medication>> {
        if (callerRole !in listOf(UserRole.DOCTOR, UserRole.PHARMACIST, UserRole.GUARDIAN)) {
            return emptyFlow()
        }
        return repository.patientMedicationsFlow(patientId)
    }

    /**
     * Assigns [patientId] to the currently authenticated caregiver.
     * Validates that the caller holds a caregiver role before writing.
     * The underlying repository call uses a WriteBatch (atomic).
     */
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

    /**
     * Removes the assignment between [patientId] and the currently authenticated
     * caregiver.  Validates the role before writing.  Atomic via WriteBatch.
     */
    suspend fun unassignPatient(
        patientId: String,
        callerRole: UserRole
    ): FirebaseResult<Unit> {
        if (callerRole !in listOf(UserRole.DOCTOR, UserRole.PHARMACIST, UserRole.GUARDIAN)) {
            return FirebaseResult.Error("Only caregivers can remove patient assignments")
        }
        return repository.unassignPatient(caregiverId = uid, patientId = patientId)
    }

    // ── Patient-facing APIs ───────────────────────────────────────────────────

    /**
     * Returns the full profile of the caregiver assigned to patient.
     * Validates that the requesting user is either the patient themselves or
     * an assigned caregiver. Firestore rules enforce read access server-side.
     */
    suspend fun getAssignedCaregiverProfile(caregiverId: String): FirebaseResult<User> {
        if (caregiverId.isBlank()) {
            return FirebaseResult.Error("No caregiver assigned")
        }
        return repository.getCaregiverProfile(caregiverId)
    }

    /**
     * Returns the full profile of a patient (for caregiver view).
     * Validates caregiver role before calling Firestore.
     */
    suspend fun getPatientProfile(
        patientId: String,
        callerRole: UserRole
    ): FirebaseResult<User> {
        if (callerRole !in listOf(UserRole.DOCTOR, UserRole.PHARMACIST, UserRole.GUARDIAN)) {
            return FirebaseResult.Error("Only caregivers can view patient profiles")
        }
        return repository.getPatientProfile(patientId)
    }
}
