package com.nkwabyte.medilert.data.repository

import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.model.ChatConversation
import com.nkwabyte.medilert.model.ChatMessage
import com.nkwabyte.medilert.model.MessageStatus
import com.nkwabyte.medilert.model.User
import com.nkwabyte.medilert.model.UserRole
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.datetime.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

import dev.gitlive.firebase.firestore.DocumentSnapshot

class ChatRepository {
    private val firestore get() = Firebase.firestore
    private val auth get() = Firebase.auth

    private val uid get() = auth.currentUser?.uid ?: ""
    private val conversationsCollection get() = firestore.collection("conversations")

    fun getConversationId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "${userId1}_${userId2}" else "${userId2}_${userId1}"
    }

    private fun parseConversation(doc: DocumentSnapshot): ChatConversation? {
        if (!doc.exists) return null
        try {
            val conv = doc.data<ChatConversation>()
            val resolvedId = if (conv.id.isNotBlank()) conv.id else doc.id
            return conv.copy(id = resolvedId)
        } catch (_: Exception) { }

        return try {
            val pIds = try { doc.get<List<String>>("participantIds") } catch (_: Exception) { emptyList() }
            val pNames = try { doc.get<Map<String, String>>("participantNames") } catch (_: Exception) { emptyMap() }
            val pPhotos = try { doc.get<Map<String, String>>("participantPhotos") } catch (_: Exception) { emptyMap() }
            val pRoles = try { doc.get<Map<String, String>>("participantRoles") } catch (_: Exception) { emptyMap() }
            val lastText = try { doc.get<String>("lastMessageText") } catch (_: Exception) { "" }
            val lastTs = try { doc.get<Long>("lastMessageTimestamp") } catch (_: Exception) {
                try { doc.get<Double>("lastMessageTimestamp").toLong() } catch (_: Exception) { 0L }
            }
            val lastSender = try { doc.get<String>("lastMessageSenderId") } catch (_: Exception) { "" }
            val unread = try { doc.get<Map<String, Int>>("unreadCount") } catch (_: Exception) { emptyMap() }
            val deleted = try { doc.get<List<String>>("deletedForUserIds") } catch (_: Exception) { emptyList() }
            val clearTs = try { doc.get<Map<String, Long>>("clearHistoryTimestamps") } catch (_: Exception) { emptyMap() }
            val updated = try { doc.get<Long>("updatedAt") } catch (_: Exception) {
                try { doc.get<Double>("updatedAt").toLong() } catch (_: Exception) { 0L }
            }

            ChatConversation(
                id = doc.id,
                participantIds = pIds,
                participantNames = pNames,
                participantPhotos = pPhotos,
                participantRoles = pRoles,
                lastMessageText = lastText,
                lastMessageTimestamp = lastTs,
                lastMessageSenderId = lastSender,
                unreadCount = unread,
                deletedForUserIds = deleted,
                clearHistoryTimestamps = clearTs,
                updatedAt = updated
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseChatMessage(doc: DocumentSnapshot): ChatMessage? {
        if (!doc.exists) return null
        try {
            val msg = doc.data<ChatMessage>()
            val resolvedId = if (msg.id.isNotBlank()) msg.id else doc.id
            return msg.copy(id = resolvedId)
        } catch (_: Exception) { }

        return try {
            val convId = try { doc.get<String>("conversationId") } catch (_: Exception) { "" }
            val senderId = try { doc.get<String>("senderId") } catch (_: Exception) { "" }
            val senderName = try { doc.get<String>("senderName") } catch (_: Exception) { "User" }
            val roleStr = try { doc.get<String>("senderRole") } catch (_: Exception) { "PATIENT" }
            val recipientId = try { doc.get<String>("recipientId") } catch (_: Exception) { "" }
            val recipientName = try { doc.get<String>("recipientName") } catch (_: Exception) { "User" }
            val text = try { doc.get<String>("text") } catch (_: Exception) { "" }
            val timestamp = try { doc.get<Long>("timestamp") } catch (_: Exception) {
                try { doc.get<Double>("timestamp").toLong() } catch (_: Exception) { 0L }
            }
            val read = try { doc.get<Boolean>("read") } catch (_: Exception) { false }
            val statusStr = try { doc.get<String>("status") } catch (_: Exception) { "" }

            val status = when (statusStr.uppercase().trim()) {
                "READ" -> MessageStatus.READ
                "DELIVERED" -> MessageStatus.DELIVERED
                "SENT" -> MessageStatus.SENT
                else -> if (read) MessageStatus.READ else MessageStatus.SENT
            }

            val role = when (roleStr.uppercase().trim()) {
                "DOCTOR" -> UserRole.DOCTOR
                "PHARMACIST" -> UserRole.PHARMACIST
                "GUARDIAN", "CAREGIVER" -> UserRole.GUARDIAN
                else -> UserRole.PATIENT
            }

            ChatMessage(
                id = doc.id,
                conversationId = convId,
                senderId = senderId,
                senderName = senderName,
                senderRole = role,
                recipientId = recipientId,
                recipientName = recipientName,
                text = text,
                timestamp = timestamp,
                read = read || status == MessageStatus.READ,
                status = status
            )
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getOrCreateConversation(currentUser: User, recipientUser: User): FirebaseResult<ChatConversation> {
        return try {
            val convId = getConversationId(currentUser.id, recipientUser.id)
            val docRef = conversationsCollection.document(convId)
            val snapshot = docRef.get()

            if (snapshot.exists) {
                var conv = parseConversation(snapshot) ?: snapshot.data<ChatConversation>()
                val updatedPhotos = conv.participantPhotos.toMutableMap()
                var needsPhotoUpdate = false
                if (currentUser.photoUrl.isNotBlank() && updatedPhotos[currentUser.id] != currentUser.photoUrl) {
                    updatedPhotos[currentUser.id] = currentUser.photoUrl
                    needsPhotoUpdate = true
                }
                if (recipientUser.photoUrl.isNotBlank() && updatedPhotos[recipientUser.id] != recipientUser.photoUrl) {
                    updatedPhotos[recipientUser.id] = recipientUser.photoUrl
                    needsPhotoUpdate = true
                }
                if (needsPhotoUpdate) {
                    conv = conv.copy(participantPhotos = updatedPhotos)
                    docRef.set(mapOf("participantPhotos" to updatedPhotos), merge = true)
                }
                // If previously deleted by current user, unhide it
                if (conv.deletedForUserIds.contains(currentUser.id)) {
                    val updatedDeleted = conv.deletedForUserIds.filter { it != currentUser.id }
                    docRef.set(mapOf("deletedForUserIds" to updatedDeleted), merge = true)
                }
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
                    deletedForUserIds = emptyList(),
                    clearHistoryTimestamps = emptyMap(),
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
                    parseConversation(doc)
                }.filter { conv ->
                    conv.participantIds.contains(userId) &&
                    (!conv.deletedForUserIds.contains(userId) || conv.lastMessageTimestamp > (conv.clearHistoryTimestamps[userId] ?: 0L))
                }.sortedByDescending { it.lastMessageTimestamp }
            }
            .catch { emit(emptyList()) }
    }

    fun messagesFlow(conversationId: String, userId: String = ""): Flow<List<ChatMessage>> {
        if (conversationId.isBlank()) return kotlinx.coroutines.flow.flowOf(emptyList())
        return kotlinx.coroutines.flow.combine(
            conversationsCollection.document(conversationId)
                .collection("messages").snapshots
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        parseChatMessage(doc)
                    }.sortedBy { it.timestamp }
                }
                .catch { emit(emptyList()) },
            conversationsCollection.document(conversationId).snapshots
                .map { snap -> if (snap.exists) parseConversation(snap) else null }
                .catch { emit(null) }
        ) { messages, conv ->
            val clearTs = if (userId.isNotBlank()) conv?.clearHistoryTimestamps?.get(userId) ?: 0L else 0L
            if (clearTs > 0L) {
                messages.filter { it.timestamp > clearTs }
            } else {
                messages
            }
        }
    }

    suspend fun sendMessage(message: ChatMessage): FirebaseResult<Unit> {
        return try {
            val convDoc = conversationsCollection.document(message.conversationId)
            val msgDoc = convDoc.collection("messages").document(message.id)

            // Save message item
            val msgToSend = message.copy(status = MessageStatus.SENT, read = false)
            msgDoc.set(msgToSend)

            // Update unread count for recipient & unhide from deleted
            val convSnapshot = convDoc.get()
            val currentConv = if (convSnapshot.exists) parseConversation(convSnapshot) else null
            val currentUnread = currentConv?.unreadCount?.get(message.recipientId) ?: 0
            val updatedUnread = (currentConv?.unreadCount?.toMutableMap() ?: mutableMapOf()).apply {
                this[message.recipientId] = currentUnread + 1
                this[message.senderId] = 0
            }
            val updatedDeleted = (currentConv?.deletedForUserIds ?: emptyList()).filter {
                it != message.senderId && it != message.recipientId
            }

            // Update parent conversation summary
            convDoc.set(
                mapOf(
                    "lastMessageText" to message.text,
                    "lastMessageTimestamp" to message.timestamp,
                    "lastMessageSenderId" to message.senderId,
                    "unreadCount" to updatedUnread,
                    "deletedForUserIds" to updatedDeleted,
                    "updatedAt" to message.timestamp
                ),
                merge = true
            )
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to send message", e)
        }
    }

    suspend fun deleteConversationForUser(conversationId: String, userId: String): FirebaseResult<Unit> {
        if (conversationId.isBlank() || userId.isBlank()) return FirebaseResult.Success(Unit)
        return try {
            val convDoc = conversationsCollection.document(conversationId)
            val snap = convDoc.get()
            if (!snap.exists) return FirebaseResult.Success(Unit)
            val conv = parseConversation(snap)
            val now = Clock.System.now().toEpochMilliseconds()

            val updatedDeleted = ((conv?.deletedForUserIds ?: emptyList()) + userId).distinct()
            val updatedClearMap = (conv?.clearHistoryTimestamps ?: emptyMap()).toMutableMap().apply {
                this[userId] = now
            }
            val updatedUnread = (conv?.unreadCount ?: emptyMap()).toMutableMap().apply {
                this[userId] = 0
            }

            convDoc.set(
                mapOf(
                    "deletedForUserIds" to updatedDeleted,
                    "clearHistoryTimestamps" to updatedClearMap,
                    "unreadCount" to updatedUnread
                ),
                merge = true
            )
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to delete chat", e)
        }
    }

    suspend fun deleteConversationForEveryone(conversationId: String): FirebaseResult<Unit> {
        if (conversationId.isBlank()) return FirebaseResult.Success(Unit)
        return try {
            val convDoc = conversationsCollection.document(conversationId)
            val msgsSnap = convDoc.collection("messages").get()
            val batch = firestore.batch()
            for (doc in msgsSnap.documents) {
                batch.delete(doc.reference)
            }
            val now = Clock.System.now().toEpochMilliseconds()
            val convSnap = convDoc.get()
            val conv = if (convSnap.exists) parseConversation(convSnap) else null
            val participants = conv?.participantIds ?: emptyList()
            val clearMap = participants.associateWith { now }

            batch.set(
                convDoc,
                mapOf(
                    "lastMessageText" to "",
                    "lastMessageSenderId" to "",
                    "unreadCount" to participants.associateWith { 0 },
                    "deletedForUserIds" to participants,
                    "clearHistoryTimestamps" to clearMap,
                    "updatedAt" to now
                ),
                merge = true
            )
            batch.commit()
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to delete chat for everyone", e)
        }
    }

    suspend fun markConversationAsRead(conversationId: String, currentUserId: String): FirebaseResult<Unit> {
        if (conversationId.isBlank() || currentUserId.isBlank()) return FirebaseResult.Success(Unit)
        return try {
            val convDoc = conversationsCollection.document(conversationId)
            val messagesSnapshot = convDoc.collection("messages").get()
            
            val batch = firestore.batch()
            var unreadFound = false
            for (doc in messagesSnapshot.documents) {
                val recipientId = try { doc.get<String>("recipientId") } catch (_: Exception) { "" }
                val read = try { doc.get<Boolean>("read") } catch (_: Exception) { false }
                if (recipientId == currentUserId && !read) {
                    batch.set(doc.reference, mapOf("read" to true, "status" to MessageStatus.READ.name), merge = true)
                    unreadFound = true
                }
            }
            if (unreadFound) {
                val convSnapshot = convDoc.get()
                val currentConv = if (convSnapshot.exists) parseConversation(convSnapshot) else null
                val updatedUnread = (currentConv?.unreadCount?.toMutableMap() ?: mutableMapOf()).apply {
                    this[currentUserId] = 0
                }
                batch.set(convDoc, mapOf("unreadCount" to updatedUnread), merge = true)
                batch.commit()
            }
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to mark as read", e)
        }
    }

    suspend fun markMessagesAsDelivered(conversationId: String, currentUserId: String): FirebaseResult<Unit> {
        if (conversationId.isBlank() || currentUserId.isBlank()) return FirebaseResult.Success(Unit)
        return try {
            val convDoc = conversationsCollection.document(conversationId)
            val messagesSnapshot = convDoc.collection("messages").get()
            
            val batch = firestore.batch()
            var needCommit = false
            for (doc in messagesSnapshot.documents) {
                val recipientId = try { doc.get<String>("recipientId") } catch (_: Exception) { "" }
                val read = try { doc.get<Boolean>("read") } catch (_: Exception) { false }
                val statusStr = try { doc.get<String>("status") } catch (_: Exception) { "" }
                if (recipientId == currentUserId && !read && statusStr != MessageStatus.DELIVERED.name && statusStr != MessageStatus.READ.name) {
                    batch.set(doc.reference, mapOf("status" to MessageStatus.DELIVERED.name), merge = true)
                    needCommit = true
                }
            }
            if (needCommit) {
                batch.commit()
            }
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            FirebaseResult.Error(e.message ?: "Failed to mark as delivered", e)
        }
    }
}
