package com.nkwabyte.medilert.ui.screens.chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.nkwabyte.medilert.model.ChatConversation
import com.nkwabyte.medilert.model.ChatMessage
import com.nkwabyte.medilert.model.MessageStatus
import com.nkwabyte.medilert.model.User
import com.nkwabyte.medilert.model.UserRole
import com.nkwabyte.medilert.ui.theme.Background
import com.nkwabyte.medilert.ui.theme.BorderLight
import com.nkwabyte.medilert.ui.theme.GhanaRed
import com.nkwabyte.medilert.ui.theme.Poppins
import com.nkwabyte.medilert.ui.theme.PrimaryGreen
import com.nkwabyte.medilert.ui.theme.TextPrimary
import com.nkwabyte.medilert.ui.theme.TextSecondary
import com.nkwabyte.medilert.util.HapticFeedback
import com.nkwabyte.medilert.viewmodel.AppViewModel
import com.nkwabyte.medilert.viewmodel.ChatViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun ChatScreen(
    appViewModel: AppViewModel = viewModel { AppViewModel() },
    chatViewModel: ChatViewModel = viewModel { ChatViewModel() },
    isCaregiver: Boolean = false
) {
    val currentUser by appViewModel.currentUser.collectAsState()
    val conversations by chatViewModel.conversations.collectAsState()
    val activeConversation by chatViewModel.activeConversation.collectAsState()
    val activeMessages by chatViewModel.activeMessages.collectAsState()
    val contacts by chatViewModel.contacts.collectAsState()
    val assignedCaregiverId by chatViewModel.assignedCaregiverId.collectAsState()
    val recipientPresence by chatViewModel.activeRecipientPresence.collectAsState()
    val recipientProfile by chatViewModel.activeRecipientProfile.collectAsState()
    val isAiChatActive by chatViewModel.isAiChatActive.collectAsState()
    val aiMessages by chatViewModel.aiMessages.collectAsState()
    val isAiTyping by chatViewModel.isAiTyping.collectAsState()
    val isLoading by chatViewModel.isLoading.collectAsState()

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var showContactPicker by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser.id) {
        if (currentUser.id.isNotBlank()) {
            chatViewModel.setCurrentUserId(currentUser.id)
            chatViewModel.loadContacts(currentUser)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            }
    ) {
        AnimatedContent(
            targetState = if (isAiChatActive) "ai" else (activeConversation?.id ?: "list"),
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "chat_screen_transition"
        ) { target ->
            when {
                target == "ai" -> {
                    MedGemmaChatRoomView(
                        currentUser = currentUser,
                        messages = aiMessages,
                        isAiTyping = isAiTyping,
                        onBackClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            chatViewModel.closeAiChat()
                        },
                        onClearChat = {
                            chatViewModel.clearAiChat(currentUser)
                        },
                        onSendMessage = { text ->
                            chatViewModel.sendAiMessage(currentUser, text)
                        }
                    )
                }
                activeConversation != null -> {
                    val conversation = activeConversation!!
                    ChatRoomView(
                        currentUser = currentUser,
                        conversation = conversation,
                        messages = activeMessages,
                        contacts = contacts,
                        recipientProfile = recipientProfile,
                        recipientPresence = recipientPresence,
                        onBackClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            chatViewModel.closeActiveConversation()
                        },
                        onDeleteChat = { convId, forEveryone ->
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            chatViewModel.deleteConversation(convId, forEveryone)
                        },
                        onSendMessage = { text, recipientId, recipientName ->
                            chatViewModel.sendMessage(
                                currentUser = currentUser,
                                recipientId = recipientId,
                                recipientName = recipientName,
                                text = text
                            )
                        }
                    )
                }
                else -> {
                    ConversationsListView(
                        currentUser = currentUser,
                        conversations = conversations,
                        contacts = contacts,
                        isLoading = isLoading,
                        onSelectConversation = { chatViewModel.selectConversation(it) },
                        onDeleteConversation = { convId, forEveryone ->
                            chatViewModel.deleteConversation(convId, forEveryone)
                        },
                        onNewChatClick = {
                            chatViewModel.loadContacts(currentUser)
                            showContactPicker = true
                        },
                        onOpenAiChat = {
                            chatViewModel.openAiChat(currentUser)
                        }
                    )
                }
            }
        }

        if (showContactPicker) {
            ContactPickerSheet(
                contacts = contacts,
                assignedDoctorId = assignedCaregiverId.ifBlank { currentUser.caregiverId },
                isLoading = isLoading,
                isCaregiver = isCaregiver,
                onOpenAiChat = {
                    showContactPicker = false
                    chatViewModel.openAiChat(currentUser)
                },
                onSelectContact = { contact ->
                    showContactPicker = false
                    chatViewModel.startChatWithUser(currentUser, contact)
                },
                onDismiss = { showContactPicker = false }
            )
        }
    }
}

// ── Delete Chat Dialog ──────────────────────────────────────────────────────

@Composable
private fun DeleteChatDialog(
    recipientName: String,
    onDismiss: () -> Unit,
    onDeleteForMe: () -> Unit,
    onDeleteForEveryone: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Chat",
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = TextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Do you want to delete this chat with $recipientName only for yourself, or permanently for both of you?",
                    fontFamily = Poppins,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Delete for everyone option
                Surface(
                    onClick = {
                        onDismiss()
                        onDeleteForEveryone()
                    },
                    color = Color(0xFFEF4444).copy(alpha = 0.08f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Delete for Everyone",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFFEF4444)
                            )
                            Text(
                                text = "Permanently deletes chat for both participants",
                                fontFamily = Poppins,
                                fontSize = 11.sp,
                                color = Color(0xFFEF4444).copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Delete for me option
                Surface(
                    onClick = {
                        onDismiss()
                        onDeleteForMe()
                    },
                    color = com.nkwabyte.medilert.ui.theme.Surface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(PrimaryGreen.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Delete for Me",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Clears chat history only on your device",
                                fontFamily = Poppins,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        },
        containerColor = com.nkwabyte.medilert.ui.theme.Surface,
        shape = RoundedCornerShape(24.dp)
    )
}

// ── Conversation List View ──────────────────────────────────────────────────

@Composable
private fun ConversationsListView(
    currentUser: User,
    conversations: List<ChatConversation>,
    contacts: List<User> = emptyList(),
    isLoading: Boolean,
    onSelectConversation: (ChatConversation) -> Unit,
    onDeleteConversation: (String, Boolean) -> Unit,
    onNewChatClick: () -> Unit,
    onOpenAiChat: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredConversations = remember(conversations, searchQuery, currentUser.id) {
        if (searchQuery.isBlank()) conversations
        else conversations.filter { conv ->
            val otherName = getOtherParticipantName(conv, currentUser.id)
            otherName.contains(searchQuery, ignoreCase = true)
        }
    }

    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val effectiveTopPadding = if (topInset > 0.dp) topInset + 8.dp else 56.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = effectiveTopPadding)
            .padding(horizontal = 20.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Live Chat",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = TextPrimary
                )
                Text(
                    text = if (currentUser.role == UserRole.DOCTOR) "Connect with your patients" else "Chat with your care team",
                    fontFamily = Poppins,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            IconButton(
                onClick = onNewChatClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(PrimaryGreen, shape = RoundedCornerShape(14.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.AddComment,
                    contentDescription = "New Chat",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // MedGemma Medical AI Assistant Card
        MedGemmaBannerCard(onClick = onOpenAiChat)

        Spacer(modifier = Modifier.height(14.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search messages...", fontFamily = Poppins, fontSize = 14.sp, color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = com.nkwabyte.medilert.ui.theme.Surface,
                unfocusedContainerColor = com.nkwabyte.medilert.ui.theme.Surface,
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = BorderLight
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading && conversations.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else if (filteredConversations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(72.dp).background(PrimaryGreen.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "No chats",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Conversations Yet",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (currentUser.role == UserRole.DOCTOR)
                            "Start a direct conversation with one of your assigned patients."
                        else
                            "Start a live chat with your doctor or caregiver.",
                        fontFamily = Poppins,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 32.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Surface(
                        onClick = onNewChatClick,
                        color = PrimaryGreen,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = "Start New Chat",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredConversations, key = { it.id }) { conv ->
                    ConversationItemCard(
                        currentUser = currentUser,
                        conversation = conv,
                        contacts = contacts,
                        onClick = { onSelectConversation(conv) },
                        onDelete = { forEveryone -> onDeleteConversation(conv.id, forEveryone) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationItemCard(
    currentUser: User,
    conversation: ChatConversation,
    contacts: List<User> = emptyList(),
    onClick: () -> Unit,
    onDelete: (forEveryone: Boolean) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val recipientId = remember(conversation, currentUser.id) {
        conversation.participantIds.firstOrNull { it != currentUser.id } ?: ""
    }
    val matchedContact = remember(contacts, recipientId) {
        contacts.firstOrNull { it.id == recipientId }
    }
    val recipientName = remember(conversation, recipientId, matchedContact) {
        matchedContact?.name?.ifBlank { null } ?: conversation.participantNames[recipientId] ?: "User"
    }
    val recipientPhoto = remember(conversation, recipientId, matchedContact) {
        matchedContact?.photoUrl?.ifBlank { null } ?: conversation.participantPhotos[recipientId] ?: ""
    }
    val recipientRole = remember(conversation, recipientId, matchedContact) {
        matchedContact?.role?.name?.ifBlank { null } ?: conversation.participantRoles[recipientId] ?: ""
    }

    val formattedTime = remember(conversation.lastMessageTimestamp) {
        if (conversation.lastMessageTimestamp > 0) formatChatTime(conversation.lastMessageTimestamp)
        else ""
    }

    if (showDeleteDialog) {
        DeleteChatDialog(
            recipientName = recipientName,
            onDismiss = { showDeleteDialog = false },
            onDeleteForMe = { onDelete(false) },
            onDeleteForEveryone = { onDelete(true) }
        )
    }

    Surface(
        onClick = onClick,
        color = com.nkwabyte.medilert.ui.theme.Surface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, BorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar on the left side (shows profile image or default user icon)
            UserAvatar(
                photoUrl = recipientPhoto,
                name = recipientName,
                size = 52.dp,
                iconSize = 28.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Details (Name, Role badge, message preview)
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = recipientName,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (recipientRole.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        RoleBadge(role = recipientRole)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (conversation.lastMessageText.isNotBlank()) conversation.lastMessageText else "Tap to start conversation",
                    fontFamily = Poppins,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                if (formattedTime.isNotBlank()) {
                    Text(
                        text = formattedTime,
                        fontFamily = Poppins,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Chat",
                        tint = TextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ── Chat Room View ──────────────────────────────────────────────────────────

@Composable
private fun ChatRoomView(
    currentUser: User,
    conversation: ChatConversation,
    messages: List<ChatMessage>,
    contacts: List<User> = emptyList(),
    recipientProfile: User? = null,
    recipientPresence: Long = 0L,
    onBackClick: () -> Unit,
    onDeleteChat: (String, Boolean) -> Unit,
    onSendMessage: (text: String, recipientId: String, recipientName: String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    val recipientId = remember(conversation, currentUser.id) {
        conversation.participantIds.firstOrNull { it != currentUser.id } ?: ""
    }
    val matchedContact = remember(contacts, recipientId) {
        contacts.firstOrNull { it.id == recipientId }
    }
    val recipientName = remember(conversation, recipientId, matchedContact, recipientProfile) {
        recipientProfile?.name?.ifBlank { null }
            ?: matchedContact?.name?.ifBlank { null }
            ?: conversation.participantNames[recipientId]
            ?: "User"
    }
    val recipientPhoto = remember(conversation, recipientId, matchedContact, recipientProfile) {
        recipientProfile?.photoUrl?.ifBlank { null }
            ?: matchedContact?.photoUrl?.ifBlank { null }
            ?: conversation.participantPhotos[recipientId]
            ?: ""
    }
    val recipientRole = remember(conversation, recipientId, matchedContact, recipientProfile) {
        recipientProfile?.role?.name?.ifBlank { null }
            ?: matchedContact?.role?.name?.ifBlank { null }
            ?: conversation.participantRoles[recipientId]
            ?: ""
    }

    val presenceSubtitle = remember(recipientPresence) {
        if (recipientPresence <= 0L) {
            "Offline"
        } else {
            val now = Clock.System.now().toEpochMilliseconds()
            val diffMs = now - recipientPresence
            if (diffMs < 3 * 60 * 1000) { // Active within 3 minutes
                "Online"
            } else if (diffMs < 24 * 60 * 60 * 1000) {
                "Last seen ${formatChatTime(recipientPresence)}"
            } else {
                "Offline"
            }
        }
    }
    val isOnline = presenceSubtitle == "Online"

    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showDeleteDialog) {
        DeleteChatDialog(
            recipientName = recipientName,
            onDismiss = { showDeleteDialog = false },
            onDeleteForMe = { onDeleteChat(conversation.id, false) },
            onDeleteForEveryone = { onDeleteChat(conversation.id, true) }
        )
    }

    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val effectiveTopPadding = if (topInset > 0.dp) topInset + 6.dp else 48.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            }
    ) {
        // Top App Bar
        Surface(
            color = com.nkwabyte.medilert.ui.theme.Surface,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = effectiveTopPadding)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Avatar in Top Bar (shows profile image or default user icon)
                UserAvatar(
                    photoUrl = recipientPhoto,
                    name = recipientName,
                    size = 42.dp,
                    iconSize = 24.dp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = recipientName,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        if (recipientRole.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            RoleBadge(role = recipientRole)
                        }
                    }
                    Spacer(modifier = Modifier.height(1.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isOnline) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(PrimaryGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = presenceSubtitle,
                            fontFamily = Poppins,
                            fontSize = 11.sp,
                            color = if (isOnline) PrimaryGreen else TextSecondary
                        )
                    }
                }

                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Chat",
                        tint = TextSecondary
                    )
                }
            }
        }

        // Messages Stream
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
                }
        ) {
            if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Say hello to start the conversation!",
                        fontFamily = Poppins,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(messages, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            isSelf = msg.senderId == currentUser.id
                        )
                    }
                }
            }
        }

        // Input Bar (Docked to bottom edge)
        Surface(
            color = com.nkwabyte.medilert.ui.theme.Surface,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Type a message...", fontFamily = Poppins, fontSize = 14.sp, color = TextSecondary) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Background,
                        unfocusedContainerColor = Background,
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = BorderLight
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            val textToSend = messageText
                            messageText = ""
                            HapticFeedback.success()
                            onSendMessage(textToSend, recipientId, recipientName)
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    },
                    enabled = messageText.isNotBlank(),
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            if (messageText.isNotBlank()) PrimaryGreen else BorderLight,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageStatusTicks(status: MessageStatus, modifier: Modifier = Modifier) {
    val tickColor = when (status) {
        MessageStatus.READ -> Color(0xFF10B981) // WhatsApp Green
        MessageStatus.DELIVERED, MessageStatus.SENT -> TextSecondary.copy(alpha = 0.7f)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy((-6).dp)
    ) {
        when (status) {
            MessageStatus.SENT -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Sent",
                    tint = tickColor,
                    modifier = Modifier.size(13.dp)
                )
            }
            MessageStatus.DELIVERED, MessageStatus.READ -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = tickColor,
                    modifier = Modifier.size(13.dp)
                )
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = if (status == MessageStatus.READ) "Read" else "Delivered",
                    tint = tickColor,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage, isSelf: Boolean) {
    val bubbleColor = if (isSelf) PrimaryGreen else com.nkwabyte.medilert.ui.theme.Surface
    val textColor = if (isSelf) Color.White else TextPrimary
    val shape = if (isSelf) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)
    }

    val formattedTime = remember(message.timestamp) {
        formatChatTime(message.timestamp)
    }

    Column(
        horizontalAlignment = if (isSelf) Alignment.End else Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(bubbleColor)
                .border(if (!isSelf) 1.dp else 0.dp, BorderLight, shape)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            val formattedMarkdown = remember(message.text) {
                formatMarkdown(message.text)
            }
            Text(
                text = formattedMarkdown,
                fontFamily = Poppins,
                fontSize = 14.sp,
                color = textColor
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = formattedTime,
                fontFamily = Poppins,
                fontSize = 10.sp,
                color = TextSecondary
            )
            if (isSelf) {
                Spacer(modifier = Modifier.width(4.dp))
                MessageStatusTicks(status = message.status)
            }
        }
    }
}

// ── MedGemma AI Components ──────────────────────────────────────────────────

@Composable
private fun MedGemmaBannerCard(
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = PrimaryGreen.copy(alpha = 0.08f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, PrimaryGreen.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(PrimaryGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = "MedGemma AI",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "MedGemma AI",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(PrimaryGreen, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "MEDICAL AI",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Ask health questions, drug interactions & adherence tips",
                    fontFamily = Poppins,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .background(PrimaryGreen, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chat",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun MedGemmaChatRoomView(
    currentUser: User,
    messages: List<ChatMessage>,
    isAiTyping: Boolean,
    onBackClick: () -> Unit,
    onClearChat: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var showClearDialog by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isAiTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text("Clear AI Medical Conversation?", fontFamily = Poppins, fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Text(
                    "This will reset your conversation with MedGemma AI. You can start a new medical consultation anytime.",
                    fontFamily = Poppins,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    onClearChat()
                }) {
                    Text("Clear", color = GhanaRed, fontFamily = Poppins, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = TextSecondary, fontFamily = Poppins)
                }
            }
        )
    }

    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val effectiveTopPadding = if (topInset > 0.dp) topInset + 6.dp else 48.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            }
    ) {
        // Top App Bar
        Surface(
            color = com.nkwabyte.medilert.ui.theme.Surface,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = effectiveTopPadding)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(PrimaryGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = "MedGemma",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "MedGemma AI",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        RoleBadge(role = "MEDICAL AI")
                    }
                    Text(
                        text = "● Medical Assistant (Always Available)",
                        fontFamily = Poppins,
                        fontSize = 12.sp,
                        color = PrimaryGreen
                    )
                }

                IconButton(onClick = { showClearDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear Chat",
                        tint = TextSecondary
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Safety Disclaimer Card
            item {
                Surface(
                    color = PrimaryGreen.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "MedGemma is an AI medical assistant for educational and adherence guidance. In an emergency, always contact emergency services or your doctor immediately.",
                            fontFamily = Poppins,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            items(messages, key = { it.id }) { msg ->
                val isSelf = msg.senderId == currentUser.id
                MessageBubble(message = msg, isSelf = isSelf)
            }

            if (isAiTyping) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(com.nkwabyte.medilert.ui.theme.Surface)
                                .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    color = PrimaryGreen,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "MedGemma is analyzing...",
                                    fontFamily = Poppins,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Input Field
        Surface(
            color = com.nkwabyte.medilert.ui.theme.Surface,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = {
                        Text(
                            text = "Ask a medical question...",
                            fontFamily = Poppins,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Background,
                        unfocusedContainerColor = Background,
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = BorderLight
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            val textToSend = messageText
                            messageText = ""
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onSendMessage(textToSend)
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            color = if (messageText.isNotBlank()) PrimaryGreen else PrimaryGreen.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ── Contact Picker Sheet ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactPickerSheet(
    contacts: List<User>,
    assignedDoctorId: String = "",
    isLoading: Boolean = false,
    isCaregiver: Boolean,
    onOpenAiChat: () -> Unit,
    onSelectContact: (User) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = com.nkwabyte.medilert.ui.theme.Surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(BorderLight.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isCaregiver) "Start a Chat" else "Select Contact / AI",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isCaregiver) "Chat with your assigned patient or MedGemma AI" else "Chat with your doctor or MedGemma AI",
                        fontFamily = Poppins,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // AI Option
            Surface(
                onClick = onOpenAiChat,
                color = PrimaryGreen.copy(alpha = 0.08f),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryGreen.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(PrimaryGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = "MedGemma AI",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MedGemma Medical AI",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            RoleBadge(role = "AI ASSISTANT")
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Instant 24/7 medical & medication guidance",
                            fontFamily = Poppins,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(PrimaryGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Chat with AI",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isLoading && contacts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryGreen, modifier = Modifier.size(36.dp))
                }
            } else if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isCaregiver) "No Assigned Patients" else "No Doctors Found",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isCaregiver)
                                "You don't have assigned patients yet. You can still chat with MedGemma AI above."
                            else
                                "Registered medical professionals will appear here.",
                            fontFamily = Poppins,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(contacts, key = { it.id }) { user ->
                        val isAssigned = !isCaregiver && (assignedDoctorId.isNotBlank() && user.id == assignedDoctorId)

                        Surface(
                            onClick = { onSelectContact(user) },
                            color = if (isAssigned) PrimaryGreen.copy(alpha = 0.08f) else Background,
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                if (isAssigned) 1.5.dp else 1.dp,
                                if (isAssigned) PrimaryGreen else BorderLight
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(
                                    photoUrl = user.photoUrl,
                                    name = user.name,
                                    size = 48.dp,
                                    iconSize = 24.dp
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = user.name.ifBlank { "Medical Professional" },
                                            fontFamily = Poppins,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        RoleBadge(role = user.role.name)
                                    }

                                    if (isAssigned) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .background(PrimaryGreen, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (user.role == UserRole.DOCTOR) "YOUR DOCTOR" else "YOUR CAREGIVER",
                                                    fontFamily = Poppins,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (user.specialty.isNotBlank()) user.specialty else user.email.ifBlank { "Available for consultation" },
                                        fontFamily = Poppins,
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(
                                            if (isAssigned) PrimaryGreen else PrimaryGreen.copy(alpha = 0.12f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = "Chat",
                                        tint = if (isAssigned) Color.White else PrimaryGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Helpers ─────────────────────────────────────────────────────────────────

@Composable
private fun UserAvatar(
    photoUrl: String,
    name: String,
    size: androidx.compose.ui.unit.Dp = 50.dp,
    iconSize: androidx.compose.ui.unit.Dp = 26.dp,
    modifier: Modifier = Modifier
) {
    val cleanUrl = remember(photoUrl) { photoUrl.trim() }
    val isValidUrl = remember(cleanUrl) {
        cleanUrl.isNotBlank() && (
            cleanUrl.startsWith("http://") ||
            cleanUrl.startsWith("https://") ||
            cleanUrl.startsWith("content://") ||
            cleanUrl.startsWith("file://") ||
            cleanUrl.startsWith("data:")
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(PrimaryGreen.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        // Default person icon is always rendered in the background
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = name,
            tint = PrimaryGreen,
            modifier = Modifier.size(iconSize)
        )

        // Overlay async profile picture if a valid URL exists
        if (isValidUrl) {
            AsyncImage(
                model = cleanUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        }
    }
}

@Composable
private fun RoleBadge(role: String) {
    val isDoctor = role.equals("DOCTOR", ignoreCase = true)
    val bg = if (isDoctor) PrimaryGreen.copy(alpha = 0.15f) else Color(0xFF3B82F6).copy(alpha = 0.15f)
    val fg = if (isDoctor) PrimaryGreen else Color(0xFF2563EB)
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = role.uppercase(),
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            color = fg
        )
    }
}

private fun getOtherParticipantName(conv: ChatConversation, currentUid: String): String {
    val otherUid = conv.participantIds.firstOrNull { it != currentUid } ?: return "User"
    return conv.participantNames[otherUid] ?: "User"
}

private fun formatChatTime(timestampMillis: Long): String {
    return try {
        val instant = Instant.fromEpochMilliseconds(timestampMillis)
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val hourStr = if (local.hour % 12 == 0) "12" else (local.hour % 12).toString()
        val minStr = local.minute.toString().padStart(2, '0')
        val amPm = if (local.hour < 12) "AM" else "PM"
        "$hourStr:$minStr $amPm"
    } catch (_: Exception) {
        ""
    }
}

private fun formatMarkdown(markdown: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = markdown.split("\n")
        lines.forEachIndexed { lineIdx, line ->
            var remaining = line
            val isHeader = remaining.startsWith("### ") || remaining.startsWith("## ") || remaining.startsWith("# ")
            if (isHeader) {
                val headerLevel = when {
                    remaining.startsWith("### ") -> 3
                    remaining.startsWith("## ") -> 2
                    else -> 1
                }
                remaining = remaining.removePrefix("#".repeat(headerLevel)).trimStart()
            }

            // Regex matches **bold**, *italic*, __bold__, _italic_
            val regex = Regex("""(\*\*(.+?)\*\*)|(\*(.+?)\*)|(__([^_\n]+)__)|(_([^_\n]+)_)""")
            var cursor = 0
            val matches = regex.findAll(remaining)

            for (match in matches) {
                val start = match.range.first
                val end = match.range.last + 1

                if (start > cursor) {
                    val plain = remaining.substring(cursor, start)
                    if (isHeader) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(plain)
                        }
                    } else {
                        append(plain)
                    }
                }

                val token = match.value
                when {
                    token.startsWith("**") && token.endsWith("**") -> {
                        val content = match.groups[2]?.value ?: token.removeSurrounding("**")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(content)
                        }
                    }
                    token.startsWith("__") && token.endsWith("__") -> {
                        val content = match.groups[6]?.value ?: token.removeSurrounding("__")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(content)
                        }
                    }
                    token.startsWith("*") && token.endsWith("*") -> {
                        val content = match.groups[4]?.value ?: token.removeSurrounding("*")
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(content)
                        }
                    }
                    token.startsWith("_") && token.endsWith("_") -> {
                        val content = match.groups[8]?.value ?: token.removeSurrounding("_")
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(content)
                        }
                    }
                    else -> append(token)
                }
                cursor = end
            }

            if (cursor < remaining.length) {
                val rest = remaining.substring(cursor)
                if (isHeader) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(rest)
                    }
                } else {
                    append(rest)
                }
            }

            if (lineIdx < lines.size - 1) {
                append("\n")
            }
        }
    }
}

