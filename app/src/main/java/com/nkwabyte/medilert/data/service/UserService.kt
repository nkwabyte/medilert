@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.nkwabyte.medilert.data.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.data.repository.UserRepository
import com.nkwabyte.medilert.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

/**
 * UserService owns the live Firestore stream for the current user's profile
 * and delegates write operations to UserRepository.
 */
class UserService(
    private val userRepository: UserRepository = UserRepository(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    /**
     * A live Flow that emits the current user's Firestore profile, re-subscribing
     * automatically whenever the Firebase auth state changes.
     *
     * The outer callbackFlow tracks auth state (never completes), and flatMapLatest
     * starts a new inner Firestore listener every time a different uid becomes active,
     * cancelling the previous listener. This guarantees the collect coroutine in
     * AppViewModel stays alive through sign-out / sign-in and registration flows.
     */
    val userProfileFlow: Flow<User?> = callbackFlow {
        val authListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.uid)
        }
        auth.addAuthStateListener(authListener)
        awaitClose { auth.removeAuthStateListener(authListener) }
    }.flatMapLatest { uid ->
        if (uid == null) {
            flowOf(null)
        } else {
            callbackFlow {
                val registration = firestore.collection("users").document(uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) { trySend(null); return@addSnapshotListener }
                        trySend(snapshot?.toObject(User::class.java))
                    }
                awaitClose { registration.remove() }
            }
        }
    }

    suspend fun getProfile(): FirebaseResult<User> =
        userRepository.getUserProfile()

    suspend fun createProfile(user: User): FirebaseResult<Unit> =
        userRepository.createUserProfile(user)

    suspend fun updateProfile(fields: Map<String, Any>): FirebaseResult<Unit> =
        userRepository.updateUserProfile(fields)

    /**
     * Ensures a Firestore user document exists after authentication.
     *
     * Uses a Firestore Transaction (read → conditional write) to eliminate the
     * TOCTOU race condition where two simultaneous calls (e.g., two devices)
     * could both observe "no document" and both try to create one.
     * The transaction is serialized and retried by Firestore automatically when
     * contention is detected (Isolation + Consistency).
     */
    suspend fun ensureProfileExists(firebaseUser: FirebaseUser): FirebaseResult<Unit> {
        return try {
            val ref = firestore.collection("users").document(firebaseUser.uid)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(ref)
                if (!snapshot.exists()) {
                    val newUser = User(
                        id = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "",
                        email = firebaseUser.email ?: "",
                        photoUrl = firebaseUser.photoUrl?.toString() ?: ""
                    )
                    transaction.set(ref, newUser)
                }
            }.await()
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to ensure profile exists", e)
        }
    }
}
