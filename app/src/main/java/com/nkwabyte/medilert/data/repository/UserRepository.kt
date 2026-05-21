package com.nkwabyte.medilert.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.model.Medication
import com.nkwabyte.medilert.model.User
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val uid get() = auth.currentUser?.uid
        ?: error("No authenticated user")

    private fun userDoc(uid: String) = firestore.collection("users").document(uid)
    private fun medsCollection(uid: String) = userDoc(uid).collection("medications")

    // ── User Profile ──────────────────────────────────────────────────────────

    suspend fun createUserProfile(user: User): FirebaseResult<Unit> {
        return try {
            userDoc(uid).set(user).await()
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to create profile", e)
        }
    }

    /**
     * Save user (can be used for both create and update)
     * Uses the user.id as document ID instead of current auth user
     */
    suspend fun saveUser(user: User): FirebaseResult<Unit> {
        return try {
            val docRef = if (user.id.isNotEmpty()) {
                userDoc(user.id)
            } else {
                userDoc(uid)
            }
            docRef.set(user).await()
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to save user", e)
        }
    }

    suspend fun getUserProfile(): FirebaseResult<User> {
        return try {
            val snapshot = userDoc(uid).get().await()
            val user = snapshot.toObject(User::class.java)
                ?: return FirebaseResult.Error("User not found")
            FirebaseResult.Success(user)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to fetch profile", e)
        }
    }

    suspend fun updateUserProfile(fields: Map<String, Any>): FirebaseResult<Unit> {
        return try {
            userDoc(uid).set(fields, SetOptions.merge()).await()
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to update profile", e)
        }
    }

    // ── Medications ───────────────────────────────────────────────────────────

    suspend fun addMedication(medication: Medication): FirebaseResult<Unit> {
        return try {
            medsCollection(uid).document(medication.id).set(medication).await()
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to add medication", e)
        }
    }

    suspend fun getMedications(): FirebaseResult<List<Medication>> {
        return try {
            val snapshot = medsCollection(uid).get().await()
            val meds = snapshot.documents.mapNotNull { it.toObject(Medication::class.java) }
            FirebaseResult.Success(meds)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to fetch medications", e)
        }
    }

    suspend fun updateMedication(medication: Medication): FirebaseResult<Unit> {
        return try {
            medsCollection(uid).document(medication.id).set(medication).await()
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to update medication", e)
        }
    }

    suspend fun deleteMedication(medicationId: String): FirebaseResult<Unit> {
        return try {
            medsCollection(uid).document(medicationId).delete().await()
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to delete medication", e)
        }
    }
}
