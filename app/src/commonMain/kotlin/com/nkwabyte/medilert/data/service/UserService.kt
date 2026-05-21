@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.nkwabyte.medilert.data.service

import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.data.repository.UserRepository
import com.nkwabyte.medilert.model.User
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class UserService(
    private val userRepository: UserRepository = UserRepository()
) {
    val userProfileFlow: Flow<User?> = Firebase.auth.authStateChanged
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(null)
            } else {
                Firebase.firestore.collection("users").document(user.uid).snapshots
                    .map { snapshot ->
                        try {
                            if (snapshot.exists) snapshot.data<User>() else null
                        } catch (_: Exception) { null }
                    }
            }
        }

    suspend fun getProfile(): FirebaseResult<User> = userRepository.getUserProfile()

    suspend fun createProfile(user: User): FirebaseResult<Unit> =
        userRepository.createUserProfile(user)

    suspend fun updateProfile(fields: Map<String, Any>): FirebaseResult<Unit> =
        userRepository.updateUserProfile(fields)

    suspend fun ensureProfileExists(firebaseUser: FirebaseUser): FirebaseResult<Unit> {
        return try {
            val ref = Firebase.firestore.collection("users").document(firebaseUser.uid)
            Firebase.firestore.runTransaction { transaction ->
                val snapshot = transaction.get(ref)
                if (!snapshot.exists) {
                    val newUser = User(
                        id = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "",
                        email = firebaseUser.email ?: "",
                        photoUrl = firebaseUser.photoURL ?: ""
                    )
                    transaction.set(ref, newUser)
                }
            }
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to ensure profile exists", e)
        }
    }
}
