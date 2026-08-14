package com.nkwabyte.medilert.data.repository

import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.model.ChatConversation
import com.nkwabyte.medilert.model.ChatMessage
import com.nkwabyte.medilert.model.User
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.datetime.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class ChatRepository {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val uid get() = auth.currentUser?.uid ?: error("No authenticated user")
    private val conversationsCollection = firestore.collection("conversations")

    fun getConversationId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "${userId1}_${userId2}" else "${userId2}_${userId1}"
    }

    suspend fun getOrCreateConversation(currentUser: User, recipientUser: User): FirebaseResult<ChatConversation> {
        return try {
            val convId = getConversationId(currentUser.id, recipientUser.id)
            val docRef = conversationsCollection.document(convId)
            val snapshot = docRef.get()

            if (snapshot.exists) {
                val conv = snapshot.data<ChatConversation>()
                FirebaseResult.Success(conv)
            } else {
                val now = Clock.System.now().toEpochMilliseconds()
                val newConv = ChatConversation(
                    id = convId,
                    participantIds = listOf(currentUser.id, recipientUser.id),
                    participantNames = mapOf(
                        currentUser.id to currentUser.name.ifBlank { "User" },
                        recipientUser.id to recipientUser.name.ifBlank { "User" }
                    ),
                    participantPhotos = mapOf(
                        currentUser.id to currentUser.photoUrl,
                        recipientUser.id to recipientUser.photoUrl
                    ),
                    participantRoles = mapOf(
                        currentUser.id to currentUser.role.name,
                        recipientUser.id to recipientUser.role.name
                    ),
                    lastMessageText = "",
                    lastMessageTimestamp = now,
                    lastMessageSenderId = "",
                    unreadCount = mapOf(currentUser.id to 0, recipientUser.id to 0),
                    updatedAt = now
                )
                docRef.set(newConv)
                FirebaseResult.Success(newConv)
            }
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to get or create conversation", e)
        }
    }

    fun userConversationsFlow(userId: String): Flow<List<ChatConversation>> {
        return conversationsCollection.snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try { doc.data<ChatConversation>() } catch (_: Exception) { null }
                }.filter { it.participantIds.contains(userId) }
                 .sortedByDescending { it.lastMessageTimestamp }
            }
            .catch { emit(emptyList()) }
    }

    fun messagesFlow(conversationId: String): Flow<List<ChatMessage>> {
        if (conversationId.isBlank()) return kotlinx.coroutines.flow.flowOf(emptyList())
        return conversationsCollection.document(conversationId)
            .collection("messages").snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try { doc.data<ChatMessage>() } catch (_: Exception) { null }
                }.sortedBy { it.timestamp }
            }
            .catch { emit(emptyList()) }
    }

    suspend fun sendMessage(message: ChatMessage): FirebaseResult<Unit> {
        return try {
            val convDoc = conversationsCollection.document(message.conversationId)
            val msgDoc = convDoc.collection("messages").document(message.id)

            // Save message item
            msgDoc.set(message)

            // Update parent conversation summary
            convDoc.update(
                "lastMessageText" to message.text,
                "lastMessageTimestamp" to message.timestamp,
                "lastMessageSenderId" to message.senderId,
                "updatedAt" to message.timestamp
            )
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to send message", e)
        }
    }
}
