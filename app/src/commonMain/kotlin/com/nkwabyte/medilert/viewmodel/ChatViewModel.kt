package com.nkwabyte.medilert.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.data.repository.CareRelationshipRepository
import com.nkwabyte.medilert.data.service.ChatService
import com.nkwabyte.medilert.data.service.MedGemmaService
import com.nkwabyte.medilert.model.ChatConversation
import com.nkwabyte.medilert.model.ChatMessage
import com.nkwabyte.medilert.model.MessageStatus
import com.nkwabyte.medilert.model.User
import com.nkwabyte.medilert.model.UserRole
import kotlinx.datetime.Clock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
    private val chatService: ChatService = ChatService(),
    private val careRelationshipRepository: CareRelationshipRepository = CareRelationshipRepository(),
    private val medGemmaService: MedGemmaService = MedGemmaService()
) : ViewModel() {

    private val _currentUserId = MutableStateFlow("")
    private val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()
    
    val conversations: StateFlow<List<ChatConversation>> = _currentUserId
        .flatMapLatest { uid ->
            if (uid.isBlank()) flowOf(emptyList())
            else chatService.getUserConversationsFlow(uid).onEach { convs ->
                for (c in convs) {
                    val unread = c.unreadCount[uid] ?: 0
                    if (unread > 0 && _activeConversation.value?.id != c.id) {
                        chatService.markMessagesAsDelivered(c.id, uid)
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _activeConversation = MutableStateFlow<ChatConversation?>(null)
    val activeConversation: StateFlow<ChatConversation?> = _activeConversation.asStateFlow()

    val activeMessages: StateFlow<List<ChatMessage>> = combine(
        _activeConversation,
        _currentUserId
    ) { conv, uid ->
        conv to uid
    }.flatMapLatest { (conv, uid) ->
        if (conv == null || conv.id.isBlank()) flowOf(emptyList())
        else chatService.getMessagesFlow(conv.id, uid).onEach { messages ->
            if (uid.isNotBlank() && messages.any { it.recipientId == uid && !it.read }) {
                chatService.markConversationAsRead(conv.id, uid)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val activeRecipientPresence: StateFlow<Long> = _activeConversation
        .flatMapLatest { conv ->
            val uid = _currentUserId.value
            val recipientId = conv?.participantIds?.firstOrNull { it != uid } ?: ""
            if (recipientId.isBlank()) flowOf(0L)
            else careRelationshipRepository.observeUserPresenceFlow(recipientId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0L)

    val activeRecipientProfile: StateFlow<User?> = _activeConversation
        .flatMapLatest { conv ->
            val uid = _currentUserId.value
            val recipientId = conv?.participantIds?.firstOrNull { it != uid } ?: ""
            if (recipientId.isBlank()) flowOf(null)
            else careRelationshipRepository.userProfileFlow(recipientId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    private val _contacts = MutableStateFlow<List<User>>(emptyList())
    val contacts: StateFlow<List<User>> = _contacts.asStateFlow()

    private val _assignedCaregiverId = MutableStateFlow("")
    val assignedCaregiverId: StateFlow<String> = _assignedCaregiverId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var loadContactsJob: Job? = null

    fun setCurrentUserId(uid: String) {
        if (_currentUserId.value != uid) {
            _currentUserId.value = uid
        }
        if (uid.isNotBlank()) {
            viewModelScope.launch {
                careRelationshipRepository.updateUserPresence(uid)
            }
        }
    }

    fun loadContacts(currentUser: User) {
        loadContactsJob?.cancel()
        loadContactsJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                if (currentUser.role == UserRole.DOCTOR || currentUser.role == UserRole.PHARMACIST || currentUser.role == UserRole.GUARDIAN) {
                    // Doctor/Caregiver view: load ONLY assigned patients
                    careRelationshipRepository.assignedPatientProfilesFlow(currentUser.id).collect { patients ->
                        _contacts.value = patients
                        _isLoading.value = false
                    }
                } else {
                    // Patient view: load all registered doctors and caregivers, sorting assigned doctor to the top
                    combine(
                        careRelationshipRepository.allDoctorsAndCaregiversFlow(),
                        careRelationshipRepository.patientAssignedCaregiverIdFlow(currentUser.id)
                    ) { doctors, assignedCaregiverIdFromDb ->
                        val effectiveCaregiverId = if (currentUser.caregiverId.isNotBlank()) {
                            currentUser.caregiverId
                        } else {
                            assignedCaregiverIdFromDb ?: ""
                        }
                        _assignedCaregiverId.value = effectiveCaregiverId
                        if (effectiveCaregiverId.isNotBlank()) {
                            doctors.sortedByDescending { it.id == effectiveCaregiverId }
                        } else {
                            doctors
                        }
                    }.collect { sorted ->
                        _contacts.value = sorted
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun selectConversation(conversation: ChatConversation) {
        _activeConversation.value = conversation
        val uid = _currentUserId.value
        if (conversation.id.isNotBlank() && uid.isNotBlank()) {
            viewModelScope.launch {
                chatService.markConversationAsRead(conversation.id, uid)
            }
        }
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

    fun deleteConversation(conversationId: String, forEveryone: Boolean = false) {
        val uid = _currentUserId.value
        if (conversationId.isBlank() || uid.isBlank()) return
        if (_activeConversation.value?.id == conversationId) {
            _activeConversation.value = null
        }
        viewModelScope.launch {
            if (forEveryone) {
                chatService.deleteConversationForEveryone(conversationId)
            } else {
                chatService.deleteConversationForUser(conversationId, uid)
            }
        }
    }

    private val _isAiChatActive = MutableStateFlow(false)
    val isAiChatActive: StateFlow<Boolean> = _isAiChatActive.asStateFlow()

    private val _aiMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val aiMessages: StateFlow<List<ChatMessage>> = _aiMessages.asStateFlow()

    private val _isAiTyping = MutableStateFlow(false)
    val isAiTyping: StateFlow<Boolean> = _isAiTyping.asStateFlow()

    fun openAiChat(currentUser: User) {
        _activeConversation.value = null
        _isAiChatActive.value = true
        if (_aiMessages.value.isEmpty()) {
            _aiMessages.value = listOf(medGemmaService.createInitialGreeting(currentUser.name))
        }
    }

    fun closeAiChat() {
        _isAiChatActive.value = false
    }

    fun clearAiChat(currentUser: User) {
        _aiMessages.value = listOf(medGemmaService.createInitialGreeting(currentUser.name))
    }

    fun sendAiMessage(currentUser: User, text: String) {
        if (text.isBlank()) return
        val userText = text.trim()
        val now = Clock.System.now().toEpochMilliseconds()

        val userMessage = ChatMessage(
            id = "user_${now}",
            conversationId = MedGemmaService.AI_BOT_ID,
            senderId = currentUser.id,
            senderName = currentUser.name.ifBlank { "User" },
            senderRole = currentUser.role,
            recipientId = MedGemmaService.AI_BOT_ID,
            recipientName = MedGemmaService.AI_BOT_NAME,
            text = userText,
            timestamp = now,
            status = MessageStatus.READ,
            read = true
        )

        _aiMessages.value = _aiMessages.value + userMessage
        _isAiTyping.value = true

        viewModelScope.launch {
            try {
                val responseText = medGemmaService.generateResponse(userText, _aiMessages.value, currentUser)
                val botNow = Clock.System.now().toEpochMilliseconds()
                val botMessage = ChatMessage(
                    id = "ai_${botNow}",
                    conversationId = MedGemmaService.AI_BOT_ID,
                    senderId = MedGemmaService.AI_BOT_ID,
                    senderName = MedGemmaService.AI_BOT_NAME,
                    recipientId = currentUser.id,
                    recipientName = currentUser.name.ifBlank { "User" },
                    text = responseText,
                    timestamp = botNow,
                    status = MessageStatus.READ,
                    read = true
                )
                _aiMessages.value = _aiMessages.value + botMessage
            } catch (e: Exception) {
                val errNow = Clock.System.now().toEpochMilliseconds()
                val errMsg = ChatMessage(
                    id = "ai_err_${errNow}",
                    conversationId = MedGemmaService.AI_BOT_ID,
                    senderId = MedGemmaService.AI_BOT_ID,
                    senderName = MedGemmaService.AI_BOT_NAME,
                    recipientId = currentUser.id,
                    recipientName = currentUser.name.ifBlank { "User" },
                    text = "I encountered a momentary issue processing your medical request. Please try again.",
                    timestamp = errNow,
                    status = MessageStatus.READ,
                    read = true
                )
                _aiMessages.value = _aiMessages.value + errMsg
            } finally {
                _isAiTyping.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
