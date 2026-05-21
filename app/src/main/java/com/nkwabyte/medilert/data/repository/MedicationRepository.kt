package com.nkwabyte.medilert.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.model.DoseStatus
import com.nkwabyte.medilert.model.Medication
import com.nkwabyte.medilert.model.MedicationSchedule
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MedicationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val uid get() = auth.currentUser?.uid ?: error("No authenticated user")

    private fun medsCollection(forUid: String = uid) =
        firestore.collection("users").document(forUid).collection("medications")

    private fun doseCollection(forUid: String = uid) =
        firestore.collection("users").document(forUid).collection("schedules")

    // ── Real-time streams ─────────────────────────────────────────────────────

    /** Live stream of medication definitions. */
    fun medicationsFlow(): Flow<List<Medication>> = callbackFlow {
        val registration = medsCollection()
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val meds = snapshot?.documents
                    ?.mapNotNull { it.toObject(Medication::class.java) }
                    ?: emptyList()
                trySend(meds)
            }
        awaitClose { registration.remove() }
    }

    /**
     * Live stream of actioned dose records only (TAKEN / MISSED / SKIPPED).
     * UPCOMING entries are never written to Firestore — they are derived in-memory
     * from the medication definition, so we filter them out here defensively.
     */
    fun doseRecordsFlow(): Flow<List<MedicationSchedule>> = callbackFlow {
        val registration = doseCollection()
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val records = snapshot?.documents
                    ?.mapNotNull { it.toObject(MedicationSchedule::class.java) }
                    ?.filter { it.status != DoseStatus.UPCOMING }
                    ?.sortedByDescending { it.date }
                    ?: emptyList()
                trySend(records)
            }
        awaitClose { registration.remove() }
    }

    // ── Medication CRUD ───────────────────────────────────────────────────────

    /** Single atomic write — only the medication definition is stored. */
    suspend fun addMedication(medication: Medication): FirebaseResult<Unit> {
        return try {
            medsCollection().document(medication.id).set(medication).await()
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to add medication", e)
        }
    }

    /**
     * Single atomic write — updates the medication definition only.
     * Past dose records are untouched; future UPCOMING slots are re-derived
     * automatically from the updated definition on the next read.
     */
    suspend fun updateMedication(medication: Medication): FirebaseResult<Unit> {
        return try {
            medsCollection().document(medication.id).set(medication).await()
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to update medication", e)
        }
    }

    /**
     * Deletes the medication definition and all its dose records.
     * Dose records are far fewer than pre-generated schedules (only actioned doses
     * are ever written), so the chunk limit is rarely reached in practice.
     */
    suspend fun deleteMedication(medicationId: String): FirebaseResult<Unit> {
        return try {
            val doseRecords = doseCollection()
                .whereEqualTo("medicationId", medicationId)
                .get().await()

            val ops = mutableListOf<(WriteBatch) -> Unit>()
            ops.add { batch -> batch.delete(medsCollection().document(medicationId)) }
            doseRecords.documents.forEach { doc ->
                ops.add { batch -> batch.delete(doc.reference) }
            }
            commitInChunks(ops)
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to delete medication", e)
        }
    }

    suspend fun getMedicationById(medicationId: String): FirebaseResult<Medication> {
        return try {
            val snapshot = medsCollection().document(medicationId).get().await()
            val med = snapshot.toObject(Medication::class.java)
                ?: return FirebaseResult.Error("Medication not found")
            FirebaseResult.Success(med)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to fetch medication", e)
        }
    }

    // ── Dose recording ────────────────────────────────────────────────────────

    /**
     * Single atomic write per dose action. Uses the deterministic schedule ID
     * ("${medicationId}_${date}_${intakeIndex}") so that re-marking the same dose
     * is an idempotent upsert rather than creating duplicate records.
     */
    suspend fun recordDoseStatus(
        schedule: MedicationSchedule,
        status: DoseStatus,
        takenAt: String = ""
    ): FirebaseResult<Unit> {
        return try {
            val record = schedule.copy(status = status, takenAt = takenAt)
            doseCollection().document(record.id).set(record).await()
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to record dose", e)
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Commits batch operations in chunks of 500 to stay within Firestore's limit. */
    private suspend fun commitInChunks(operations: List<(WriteBatch) -> Unit>) {
        operations.chunked(500).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { op -> op(batch) }
            batch.commit().await()
        }
    }
}
