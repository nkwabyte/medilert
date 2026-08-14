package com.nkwabyte.medilert.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderRole: UserRole = UserRole.PATIENT,
    val recipientId: String = "",
    val recipientName: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val read: Boolean = false
)
