package com.nkwabyte.medilert.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.model.CareAssignment
import com.nkwabyte.medilert.model.DoseStatus
import com.nkwabyte.medilert.model.Medication
import com.nkwabyte.medilert.model.MedicationSchedule
import com.nkwabyte.medilert.model.User
import com.nkwabyte.medilert.model.UserRole
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * CareRelationshipRepository manages the caregiver ↔ patient assignment relationship.
 *
 * ACID guarantees:
 * - Assign / unassign operations use a Firestore WriteBatch so that both the
 *   careAssignments document and the patient's caregiverId field are updated
 *   atomically (all succeed or all fail — Atomicity).
 * - Document IDs are deterministic ({caregiverId}_{patientId}), allowing
 *   idempotent writes and existence checks without extra reads (Consistency).
 * - All writes use .await() to confirm server durability before returning
 *   (Durability).
 */
class CareRelationshipRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val uid get() = auth.currentUser?.uid ?: error("No authenticated user")

    private val assignments = firestore.collection("careAssignments")
    private fun userDoc(id: String) = firestore.collection("users").document(id)
    private fun assignmentId(caregiverId: String, patientId: String) =
        "${caregiverId}_${patientId}"

    // ── Assign / Unassign ─────────────────────────────────────────────────────

    /**
     * Atomically creates a careAssignments document AND writes caregiverId onto
     * the patient's user document.  Uses a WriteBatch so both succeed or both fail.
     */
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
                assignedAt = System.currentTimeMillis()
            )
            val batch = firestore.batch()
            batch.set(assignments.document(assignment.id), assignment)
            batch.update(userDoc(patientId), "caregiverId", caregiverId)
            batch.commit().await()
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to assign patient", e)
        }
    }

    /**
     * Atomically deletes the careAssignments document AND clears caregiverId on
     * the patient's user document.
     */
    suspend fun unassignPatient(
        caregiverId: String,
        patientId: String
    ): FirebaseResult<Unit> {
        return try {
            val batch = firestore.batch()
            batch.delete(assignments.document(assignmentId(caregiverId, patientId)))
            batch.update(userDoc(patientId), "caregiverId", "")
            batch.commit().await()
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to unassign patient", e)
        }
    }

    // ── Live queries ──────────────────────────────────────────────────────────

    /** Real-time list of CareAssignments for a given caregiver. */
    fun assignedPatientsFlow(caregiverId: String): Flow<List<CareAssignment>> = callbackFlow {
        val reg = assignments
            .whereEqualTo("caregiverId", caregiverId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = snapshot?.documents
                    ?.mapNotNull { it.toObject(CareAssignment::class.java) }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    /** Real-time directory of patient profiles (used in caregiver add-patient search). */
    fun allPatientsFlow(): Flow<List<User>> = callbackFlow {
        android.util.Log.d("PatientDir", "=== allPatientsFlow: Starting FILTERED listener, current uid=$uid ===")
        // Use .whereEqualTo to filter at query level - this ensures Firestore only checks
        // permissions on PATIENT documents, not all users
        val reg = firestore.collection("users")
            .whereEqualTo("role", "PATIENT")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("PatientDir", "Firestore listener error: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                android.util.Log.d("PatientDir", "Snapshot received: ${snapshot?.documents?.size ?: 0} PATIENT documents")

                val patients = snapshot?.documents
                    ?.mapNotNull { doc ->
                        val user = doc.toObject(User::class.java)
                        android.util.Log.d("PatientDir", "  Doc[${doc.id}]: name=${doc.getString("name")}, user.id=${user?.id}")
                        user
                    }
                    ?.filter { user ->
                        val notSelf = user.id != uid
                        android.util.Log.d("PatientDir", "  Filter[${user.name}]: id=${user.id}, notSelf=$notSelf")
                        notSelf
                    }
                    ?.sortedBy { it.name.lowercase() }
                    ?: emptyList()

                android.util.Log.d("PatientDir", "=== Final result: ${patients.size} patients ===")
                patients.forEach { android.util.Log.d("PatientDir", "    ✓ ${it.name} (${it.id})") }

                trySend(patients)
            }
        awaitClose {
            android.util.Log.d("PatientDir", "Flow closed, removing listener")
            reg.remove()
        }
    }

    // ── Profile reads (cross-user) ────────────────────────────────────────────

    /**
     * Fetches a patient's full User profile.
     * Security rules on Firestore enforce that only assigned caregivers may call this.
     */
    suspend fun getPatientProfile(patientId: String): FirebaseResult<User> {
        return try {
            val snapshot = userDoc(patientId).get().await()
            val user = snapshot.toObject(User::class.java)
                ?: return FirebaseResult.Error("Patient not found")
            FirebaseResult.Success(user)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to load patient profile", e)
        }
    }

    /**
     * Fetches a caregiver's profile.
     * Security rules enforce that only that caregiver's assigned patients may call this.
     */
    suspend fun getCaregiverProfile(caregiverId: String): FirebaseResult<User> {
        return try {
            val snapshot = userDoc(caregiverId).get().await()
            val user = snapshot.toObject(User::class.java)
                ?: return FirebaseResult.Error("Caregiver not found")
            FirebaseResult.Success(user)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to load caregiver profile", e)
        }
    }

    /**
     * Live stream of actioned dose records for a given patient (TAKEN/MISSED/SKIPPED).
     * Used by the caregiver history view to overlay real actions onto generated slots.
     */
    fun patientDoseRecordsFlow(patientId: String): Flow<List<MedicationSchedule>> = callbackFlow {
        val reg = userDoc(patientId).collection("schedules")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val records = snapshot?.documents
                    ?.mapNotNull { it.toObject(MedicationSchedule::class.java) }
                    ?.filter { it.status != DoseStatus.UPCOMING }
                    ?: emptyList()
                trySend(records)
            }
        awaitClose { reg.remove() }
    }

    /**
     * Fetches a patient's medications (read-only, for caregiver view).
     * Firestore security rules only permit the assigned caregiver to read this.
     */
    fun patientMedicationsFlow(patientId: String): Flow<List<Medication>> = callbackFlow {
        val reg = userDoc(patientId).collection("medications")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val meds = snapshot?.documents
                    ?.mapNotNull { it.toObject(Medication::class.java) }
                    ?: emptyList()
                trySend(meds)
            }
        awaitClose { reg.remove() }
    }
}
