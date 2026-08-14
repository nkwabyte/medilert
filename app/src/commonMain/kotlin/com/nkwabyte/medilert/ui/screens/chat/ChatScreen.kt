package com.nkwabyte.medilert.ui.screens.chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.nkwabyte.medilert.model.ChatConversation
import com.nkwabyte.medilert.model.ChatMessage
import com.nkwabyte.medilert.model.User
import com.nkwabyte.medilert.model.UserRole
import com.nkwabyte.medilert.ui.theme.Background
import com.nkwabyte.medilert.ui.theme.BorderLight
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
    val isLoading by chatViewModel.isLoading.collectAsState()

    var showContactPicker by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser.id) {
        if (currentUser.id.isNotBlank()) {
            chatViewModel.setCurrentUserId(currentUser.id)
            chatViewModel.loadContacts(currentUser)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        AnimatedContent(
            targetState = activeConversation,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "chat_screen_transition"
        ) { conversation ->
            if (conversation != null) {
                ChatRoomView(
                    currentUser = currentUser,
                    conversation = conversation,
                    messages = activeMessages,
                    onBackClick = { chatViewModel.closeActiveConversation() },
                    onSendMessage = { text, recipientId, recipientName ->
                        chatViewModel.sendMessage(
                            currentUser = currentUser,
                            recipientId = recipientId,
                            recipientName = recipientName,
                            text = text
                        )
                    }
                )
            } else {
                ConversationsListView(
                    currentUser = currentUser,
                    conversations = conversations,
                    isLoading = isLoading,
                    onSelectConversation = { chatViewModel.selectConversation(it) },
                    onNewChatClick = {
                        chatViewModel.loadContacts(currentUser)
                        showContactPicker = true
                    }
                )
            }
        }

        if (showContactPicker) {
            ContactPickerSheet(
                contacts = contacts,
                isCaregiver = isCaregiver,
                onSelectContact = { contact ->
                    showContactPicker = false
                    chatViewModel.startChatWithUser(currentUser, contact)
                },
                onDismiss = { showContactPicker = false }
            )
        }
    }
}

// ── Conversation List View ──────────────────────────────────────────────────

@Composable
private fun ConversationsListView(
    currentUser: User,
    conversations: List<ChatConversation>,
    isLoading: Boolean,
    onSelectConversation: (ChatConversation) -> Unit,
    onNewChatClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredConversations = remember(conversations, searchQuery, currentUser.id) {
        if (searchQuery.isBlank()) conversations
        else conversations.filter { conv ->
            val otherName = getOtherParticipantName(conv, currentUser.id)
            otherName.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(top = 16.dp)) {
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

        Spacer(modifier = Modifier.height(12.dp))

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
                        onClick = { onSelectConversation(conv) }
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
    onClick: () -> Unit
) {
    val recipientId = remember(conversation, currentUser.id) {
        conversation.participantIds.firstOrNull { it != currentUser.id } ?: ""
    }
    val recipientName = remember(conversation, recipientId) {
        conversation.participantNames[recipientId] ?: "User"
    }
    val recipientPhoto = remember(conversation, recipientId) {
        conversation.participantPhotos[recipientId] ?: ""
    }
    val recipientRole = remember(conversation, recipientId) {
        conversation.participantRoles[recipientId] ?: ""
    }

    val formattedTime = remember(conversation.lastMessageTimestamp) {
        if (conversation.lastMessageTimestamp > 0) formatChatTime(conversation.lastMessageTimestamp)
        else ""
    }

    Surface(
        onClick = onClick,
        color = com.nkwabyte.medilert.ui.theme.Surface,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(modifier = Modifier.size(52.dp)) {
                if (recipientPhoto.isNotBlank()) {
                    AsyncImage(
                        model = recipientPhoto,
                        contentDescription = recipientName,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().background(PrimaryGreen.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = recipientName,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
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
                    color = if (conversation.lastMessageText.isNotBlank()) TextSecondary else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (formattedTime.isNotBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formattedTime,
                    fontFamily = Poppins,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
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
    onBackClick: () -> Unit,
    onSendMessage: (text: String, recipientId: String, recipientName: String) -> Unit
) {
    val recipientId = remember(conversation, currentUser.id) {
        conversation.participantIds.firstOrNull { it != currentUser.id } ?: ""
    }
    val recipientName = remember(conversation, recipientId) {
        conversation.participantNames[recipientId] ?: "User"
    }
    val recipientPhoto = remember(conversation, recipientId) {
        conversation.participantPhotos[recipientId] ?: ""
    }
    val recipientRole = remember(conversation, recipientId) {
        conversation.participantRoles[recipientId] ?: ""
    }

    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        Surface(
            color = com.nkwabyte.medilert.ui.theme.Surface,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
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

                Box(modifier = Modifier.size(40.dp)) {
                    if (recipientPhoto.isNotBlank()) {
                        AsyncImage(
                            model = recipientPhoto,
                            contentDescription = recipientName,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(PrimaryGreen.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = recipientName,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
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
                    Text(
                        text = "Active now",
                        fontFamily = Poppins,
                        fontSize = 11.sp,
                        color = PrimaryGreen
                    )
                }
            }
        }

        // Messages Stream
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
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

        // Input Bar
        Surface(
            color = com.nkwabyte.medilert.ui.theme.Surface,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
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
            Text(
                text = message.text,
                fontFamily = Poppins,
                fontSize = 14.sp,
                color = textColor
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = formattedTime,
            fontFamily = Poppins,
            fontSize = 10.sp,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

// ── Contact Picker Sheet ────────────────────────────────────────────────────

@Composable
private fun ContactPickerSheet(
    contacts: List<User>,
    isCaregiver: Boolean,
    onSelectContact: (User) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.nkwabyte.medilert.ui.theme.Overlay)
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false, onClick = {}),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = com.nkwabyte.medilert.ui.theme.Surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCaregiver) "Select Patient to Chat" else "Select Caregiver / Doctor",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (contacts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isCaregiver) "No assigned patients found." else "No assigned caregiver found.",
                            fontFamily = Poppins,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(contacts, key = { it.id }) { user ->
                            Surface(
                                onClick = { onSelectContact(user) },
                                color = Background,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(44.dp)) {
                                        if (user.photoUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = user.photoUrl,
                                                contentDescription = user.name,
                                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier.fillMaxSize().background(PrimaryGreen.copy(alpha = 0.15f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = PrimaryGreen,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = user.name.ifBlank { "User" },
                                            fontFamily = Poppins,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = user.email,
                                            fontFamily = Poppins,
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    RoleBadge(role = user.role.name)
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
