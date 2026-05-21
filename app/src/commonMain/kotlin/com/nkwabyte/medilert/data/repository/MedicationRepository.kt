package com.nkwabyte.medilert.data.repository

import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.model.DoseStatus
import com.nkwabyte.medilert.model.Medication
import com.nkwabyte.medilert.model.MedicationSchedule
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MedicationRepository {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val uid get() = auth.currentUser?.uid ?: error("No authenticated user")

    private fun medsCollection(forUid: String = uid) =
        firestore.collection("users").document(forUid).collection("medications")

    private fun doseCollection(forUid: String = uid) =
        firestore.collection("users").document(forUid).collection("schedules")

    fun medicationsFlow(): Flow<List<Medication>> =
        medsCollection().snapshots.map { snapshot ->
            snapshot.documents.mapNotNull {
                try { it.data<Medication>() } catch (_: Exception) { null }
            }
        }

    fun doseRecordsFlow(): Flow<List<MedicationSchedule>> =
        doseCollection().snapshots.map { snapshot ->
            snapshot.documents.mapNotNull {
                try { it.data<MedicationSchedule>() } catch (_: Exception) { null }
            }.filter { it.status != DoseStatus.UPCOMING }
             .sortedByDescending { it.date }
        }

    suspend fun addMedication(medication: Medication): FirebaseResult<Unit> {
        return try {
            medsCollection().document(medication.id).set(medication)
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to add medication", e)
        }
    }

    suspend fun updateMedication(medication: Medication): FirebaseResult<Unit> {
        return try {
            medsCollection().document(medication.id).set(medication)
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to update medication", e)
        }
    }

    suspend fun deleteMedication(medicationId: String): FirebaseResult<Unit> {
        return try {
            val doseRecords = doseCollection()
                .where { "medicationId" equalTo medicationId }
                .get()

            val batch = firestore.batch()
            batch.delete(medsCollection().document(medicationId))
            doseRecords.documents.forEach { doc -> batch.delete(doc.reference) }
            batch.commit()
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to delete medication", e)
        }
    }

    suspend fun getMedicationById(medicationId: String): FirebaseResult<Medication> {
        return try {
            val snapshot = medsCollection().document(medicationId).get()
            val med = if (snapshot.exists) snapshot.data<Medication>() else null
            if (med != null) FirebaseResult.Success(med)
            else FirebaseResult.Error("Medication not found")
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to fetch medication", e)
        }
    }

    suspend fun recordDoseStatus(
        schedule: MedicationSchedule,
        status: DoseStatus,
        takenAt: String = ""
    ): FirebaseResult<Unit> {
        return try {
            val record = schedule.copy(status = status, takenAt = takenAt)
            doseCollection().document(record.id).set(record)
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to record dose", e)
        }
    }
}
