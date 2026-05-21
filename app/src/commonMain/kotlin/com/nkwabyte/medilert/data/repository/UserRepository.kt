package com.nkwabyte.medilert.data.repository

import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.model.Medication
import com.nkwabyte.medilert.model.User
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore

class UserRepository {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val uid get() = auth.currentUser?.uid ?: error("No authenticated user")

    private fun userDoc(uid: String) = firestore.collection("users").document(uid)
    private fun medsCollection(uid: String) = userDoc(uid).collection("medications")

    suspend fun createUserProfile(user: User): FirebaseResult<Unit> {
        return try {
            userDoc(uid).set(user)
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to create profile", e)
        }
    }

    suspend fun saveUser(user: User): FirebaseResult<Unit> {
        return try {
            val docRef = if (user.id.isNotEmpty()) userDoc(user.id) else userDoc(uid)
            docRef.set(user)
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to save user", e)
        }
    }

    suspend fun getUserProfile(): FirebaseResult<User> {
        return try {
            val snapshot = userDoc(uid).get()
            val user = if (snapshot.exists) snapshot.data<User>() else null
            if (user != null) FirebaseResult.Success(user)
            else FirebaseResult.Error("User not found")
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to fetch profile", e)
        }
    }

    suspend fun updateUserProfile(fields: Map<String, Any>): FirebaseResult<Unit> {
        return try {
            userDoc(uid).set(fields, merge = true)
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to update profile", e)
        }
    }

    suspend fun addMedication(medication: Medication): FirebaseResult<Unit> {
        return try {
            medsCollection(uid).document(medication.id).set(medication)
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to add medication", e)
        }
    }

    suspend fun getMedications(): FirebaseResult<List<Medication>> {
        return try {
            val snapshot = medsCollection(uid).get()
            val meds = snapshot.documents.mapNotNull {
                try { it.data<Medication>() } catch (_: Exception) { null }
            }
            FirebaseResult.Success(meds)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to fetch medications", e)
        }
    }

    suspend fun updateMedication(medication: Medication): FirebaseResult<Unit> {
        return try {
            medsCollection(uid).document(medication.id).set(medication)
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to update medication", e)
        }
    }

    suspend fun deleteMedication(medicationId: String): FirebaseResult<Unit> {
        return try {
            medsCollection(uid).document(medicationId).delete()
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to delete medication", e)
        }
    }
}
