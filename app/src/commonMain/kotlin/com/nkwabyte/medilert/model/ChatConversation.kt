package com.nkwabyte.medilert.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatConversation(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val participantPhotos: Map<String, String> = emptyMap(),
    val participantRoles: Map<String, String> = emptyMap(),
    val lastMessageText: String = "",
    val lastMessageTimestamp: Long = 0L,
    val lastMessageSenderId: String = "",
    val unreadCount: Map<String, Int> = emptyMap(),
    val updatedAt: Long = 0L
)
