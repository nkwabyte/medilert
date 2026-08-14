package com.nkwabyte.medilert.data.service

import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.data.repository.ChatRepository
import com.nkwabyte.medilert.model.ChatConversation
import com.nkwabyte.medilert.model.ChatMessage
import com.nkwabyte.medilert.model.User
import kotlinx.coroutines.flow.Flow

class ChatService(
    private val chatRepository: ChatRepository = ChatRepository()
) {
    suspend fun getOrCreateConversation(currentUser: User, recipientUser: User): FirebaseResult<ChatConversation> =
        chatRepository.getOrCreateConversation(currentUser, recipientUser)

    fun getUserConversationsFlow(userId: String): Flow<List<ChatConversation>> =
        chatRepository.userConversationsFlow(userId)

    fun getMessagesFlow(conversationId: String): Flow<List<ChatMessage>> =
        chatRepository.messagesFlow(conversationId)

    suspend fun sendMessage(message: ChatMessage): FirebaseResult<Unit> =
        chatRepository.sendMessage(message)
}
