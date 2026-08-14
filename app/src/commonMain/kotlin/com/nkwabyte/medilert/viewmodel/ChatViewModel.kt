package com.nkwabyte.medilert.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.data.repository.CareRelationshipRepository
import com.nkwabyte.medilert.data.service.ChatService
import com.nkwabyte.medilert.model.ChatConversation
import com.nkwabyte.medilert.model.ChatMessage
import com.nkwabyte.medilert.model.User
import com.nkwabyte.medilert.model.UserRole
import kotlinx.datetime.Clock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
    private val chatService: ChatService = ChatService(),
    private val careRelationshipRepository: CareRelationshipRepository = CareRelationshipRepository()
) : ViewModel() {

    private val _currentUserId = MutableStateFlow("")
    
    val conversations: StateFlow<List<ChatConversation>> = _currentUserId
        .flatMapLatest { uid ->
            if (uid.isBlank()) flowOf(emptyList())
            else chatService.getUserConversationsFlow(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _activeConversation = MutableStateFlow<ChatConversation?>(null)
    val activeConversation: StateFlow<ChatConversation?> = _activeConversation.asStateFlow()

    val activeMessages: StateFlow<List<ChatMessage>> = _activeConversation
        .flatMapLatest { conv ->
            if (conv == null || conv.id.isBlank()) flowOf(emptyList())
            else chatService.getMessagesFlow(conv.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _contacts = MutableStateFlow<List<User>>(emptyList())
    val contacts: StateFlow<List<User>> = _contacts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun setCurrentUserId(uid: String) {
        if (_currentUserId.value != uid) {
            _currentUserId.value = uid
        }
    }

    fun loadContacts(currentUser: User) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (currentUser.role == UserRole.DOCTOR || currentUser.role == UserRole.PHARMACIST || currentUser.role == UserRole.GUARDIAN) {
                    // Doctor/Caregiver view: load assigned patients
                    careRelationshipRepository.allPatientsFlow().collect { patients ->
                        _contacts.value = patients
                        _isLoading.value = false
                    }
                } else {
                    // Patient view: load assigned doctor/caregiver if any, or available doctors
                    if (currentUser.caregiverId.isNotBlank()) {
                        when (val result = careRelationshipRepository.getCaregiverProfile(currentUser.caregiverId)) {
                            is FirebaseResult.Success -> _contacts.value = listOf(result.data)
                            else -> _contacts.value = emptyList()
                        }
                    } else {
                        // Fallback: list assigned caregivers from all care assignments or patient contacts
                        _contacts.value = emptyList()
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun selectConversation(conversation: ChatConversation) {
        _activeConversation.value = conversation
    }

    fun closeActiveConversation() {
        _activeConversation.value = null
    }

    fun startChatWithUser(currentUser: User, recipient: User) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = chatService.getOrCreateConversation(currentUser, recipient)) {
                is FirebaseResult.Success -> {
                    _activeConversation.value = result.data
                    _isLoading.value = false
                }
                is FirebaseResult.Error -> {
                    _error.value = result.message
                    _isLoading.value = false
                }
                is FirebaseResult.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

    fun sendMessage(currentUser: User, recipientId: String, recipientName: String, text: String) {
        val conv = _activeConversation.value ?: return
        if (text.isBlank()) return

        val messageId = "msg_${Clock.System.now().toEpochMilliseconds()}_${(1000..9999).random()}"
        val message = ChatMessage(
            id = messageId,
            conversationId = conv.id,
            senderId = currentUser.id,
            senderName = currentUser.name.ifBlank { "User" },
            senderRole = currentUser.role,
            recipientId = recipientId,
            recipientName = recipientName,
            text = text.trim(),
            timestamp = Clock.System.now().toEpochMilliseconds()
        )

        viewModelScope.launch {
            chatService.sendMessage(message)
        }
    }

    fun clearError() {
        _error.value = null
    }
}
