package com.nkwabyte.medilert.ui.screens.settings

import com.nkwabyte.medilert.util.tr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Shield

import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.model.UserRole
import com.nkwabyte.medilert.generated.resources.Res
import com.nkwabyte.medilert.generated.resources.img_auth_login
import com.nkwabyte.medilert.navigation.AppVersion
import com.nkwabyte.medilert.navigation.LanguageSettings
import org.jetbrains.compose.resources.painterResource
import com.nkwabyte.medilert.navigation.Login
import com.nkwabyte.medilert.navigation.PrivacyPolicy
import com.nkwabyte.medilert.navigation.ProfilePage
import com.nkwabyte.medilert.ui.theme.Background
import com.nkwabyte.medilert.ui.theme.BorderLight
import com.nkwabyte.medilert.ui.theme.BorderMedium
import com.nkwabyte.medilert.ui.theme.DarkGreen
import com.nkwabyte.medilert.ui.theme.Divider
import com.nkwabyte.medilert.ui.theme.GhanaRed
import com.nkwabyte.medilert.ui.theme.Poppins
import com.nkwabyte.medilert.ui.theme.PrimaryGreen
import com.nkwabyte.medilert.ui.theme.Surface
import com.nkwabyte.medilert.ui.theme.TextPrimary
import com.nkwabyte.medilert.ui.theme.TextSecondary
import com.nkwabyte.medilert.viewmodel.AppViewModel
import com.nkwabyte.medilert.viewmodel.AuthViewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel
import com.nkwabyte.medilert.util.SoundPlayer

@Composable
fun SettingsScreen(
    navViewModel: NavViewModel = viewModel { NavViewModel() },
    appViewModel: AppViewModel = viewModel { AppViewModel() },
    authViewModel: AuthViewModel = viewModel { AuthViewModel() },
    hideBackButton: Boolean = false,
    isCaregiver: Boolean = false
) {
    val currentUser by appViewModel.currentUser.collectAsState()
    val userRole by appViewModel.userRole.collectAsState()
    val photoBytes by appViewModel.profilePhotoBytes.collectAsState()
    val caregiver = isCaregiver || userRole == UserRole.DOCTOR || userRole == UserRole.PHARMACIST
    val remindersOn = currentUser.preferences.notificationsEnabled
    val soundAlerts = currentUser.preferences.soundEnabled
    val vibration = currentUser.preferences.vibrationEnabled
    val darkMode by appViewModel.isDarkMode.collectAsState()

    val missedAlerts = currentUser.preferences.missedAlertsEnabled
    val lowAdherence = currentUser.preferences.lowAdherenceEnabled
    val notificationTone by appViewModel.notificationTone.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showToneDialog by remember { mutableStateOf(false) }
    var selectedTone by remember(notificationTone) { mutableStateOf(notificationTone) }


    // Compute user display info
    val fullName = remember(currentUser.name) {
        currentUser.name.ifBlank { "User" }
    }

    // Always show phone number (required by spec: name, phone, time joined)
    val contactInfo = remember(currentUser.phone) {
        currentUser.phone.ifBlank { "No phone number" }
    }

    val roleBadgeLabel = remember(currentUser.role, caregiver) {
        if (caregiver) when (currentUser.role) {
            UserRole.DOCTOR -> "Doctor"
            UserRole.PHARMACIST -> "Pharmacist"
            UserRole.GUARDIAN -> "Caregiver"
            UserRole.PATIENT -> null
        } else null
    }

    val memberSinceLabel = "Member since".tr()
    val memberSinceMonthNum = remember(currentUser.createdAt) {
        if (currentUser.createdAt > 0) {
            Instant.fromEpochMilliseconds(currentUser.createdAt)
                .toLocalDateTime(TimeZone.currentSystemDefault()).monthNumber
        } else {
            0
        }
    }
    val memberSinceYear = remember(currentUser.createdAt) {
        if (currentUser.createdAt > 0) {
            Instant.fromEpochMilliseconds(currentUser.createdAt)
                .toLocalDateTime(TimeZone.currentSystemDefault()).year.toString()
        } else {
            "2026"
        }
    }
    
    val monthStr = when (memberSinceMonthNum) {
        1 -> "January"; 2 -> "February"; 3 -> "March"; 4 -> "April"
        5 -> "May"; 6 -> "June"; 7 -> "July"; 8 -> "August"
        9 -> "September"; 10 -> "October"; 11 -> "November"; 12 -> "December"
        else -> ""
    }
    
    val memberSince = if (monthStr.isNotEmpty()) {
        "$memberSinceLabel ${monthStr.tr()} $memberSinceYear"
    } else {
        "$memberSinceLabel $memberSinceYear"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            PrimaryGreen.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(bottom = if (hideBackButton) 90.dp else 0.dp),
            contentPadding = PaddingValues(top = 56.dp, bottom = 40.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!hideBackButton) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Surface, CircleShape)
                                .border(1.dp, BorderLight, CircleShape)
                                .clickable { navViewModel.popBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ChevronLeft,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(40.dp))
                    }
                    Text(
                        "Settings".tr(),
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.size(40.dp))
                }

                // Profile card — image with brand-tinted gradient overlay
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                ) {
                    // Background photo
                    Image(
                        painter = painterResource(Res.drawable.img_auth_login),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Brand gradient overlay — tinted but light enough to reveal the photo
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        PrimaryGreen.copy(alpha = 0.52f),
                                        DarkGreen.copy(alpha = 0.68f)
                                    )
                                )
                            )
                    )
                    // Card content (unchanged)
                    Box(modifier = Modifier.padding(24.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val avatarBitmap = photoBytes
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .background(
                                    Color.White.copy(alpha = 0.2f),
                                    RoundedCornerShape(24.dp)
                                )
                                .border(
                                    3.dp,
                                    Color.White.copy(alpha = 0.2f),
                                    RoundedCornerShape(24.dp)
                                )
                                .clip(RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarBitmap != null) {
                                coil3.compose.AsyncImage(
                                    model = avatarBitmap,
                                    contentDescription = "Profile photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                fullName,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                contactInfo,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                roleBadgeLabel?.let { label ->
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                Color.White.copy(alpha = 0.25f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            label,
                                            fontFamily = Poppins,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Color.Black.copy(alpha = 0.2f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        memberSince,
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                SettingsSectionHeader("PROFILE".tr())
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Default.Person,
                        title = "Edit Profile".tr(),
                        subtitle = "Update your personal details".tr(),
                        onClick = { navViewModel.navigateTo(ProfilePage) })
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SettingsSectionHeader(if (caregiver) "PATIENT ALERTS".tr() else "NOTIFICATIONS".tr())
                SettingsCard {
                    if (caregiver) {
                        SettingsRow(
                            icon = Icons.Default.Warning,
                            title = "Missed Medications".tr(),
                            subtitle = if (missedAlerts) "Enabled".tr() else "Disabled".tr(),
                            rightElement = {
                                MedSwitch(
                                    checked = missedAlerts,
                                    onCheckedChange = { appViewModel.updatePreferences(missedAlertsEnabled = it) })
                            })
                        SettingsDivider()
                        SettingsRow(
                            icon = Icons.Default.Favorite,
                            title = "Low Adherence Warning".tr(),
                            subtitle = if (lowAdherence) "Enabled".tr() else "Disabled".tr(),
                            rightElement = {
                                MedSwitch(
                                    checked = lowAdherence,
                                    onCheckedChange = { appViewModel.updatePreferences(lowAdherenceEnabled = it) })
                            })
                    } else {
                        SettingsRow(
                            icon = Icons.Default.Notifications,
                            title = "Medication Reminders".tr(),
                            subtitle = if (remindersOn) "Enabled".tr() else "Disabled".tr(),
                            rightElement = {
                                MedSwitch(
                                    checked = remindersOn,
                                    onCheckedChange = { appViewModel.updatePreferences(notificationsEnabled = it) })
                            })
                        SettingsDivider()
                    }
                    SettingsRow(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        title = "Sound Alerts".tr(),
                        subtitle = if (soundAlerts) "Enabled".tr() else "Disabled".tr(),
                        rightElement = {
                            MedSwitch(
                                checked = soundAlerts,
                                onCheckedChange = { appViewModel.updatePreferences(soundEnabled = it) })
                        })
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Default.PhoneAndroid,
                        title = "Vibration".tr(),
                        subtitle = if (vibration) "Enabled".tr() else "Disabled".tr(),
                        rightElement = {
                            MedSwitch(
                                checked = vibration,
                                onCheckedChange = { 
                                    appViewModel.updatePreferences(vibrationEnabled = it)
                                    if (it) {
                                        com.nkwabyte.medilert.util.HapticFeedback.success()
                                    }
                                })
                        })
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Default.GraphicEq,
                        title = "Notification Tone".tr(),
                        subtitle = selectedTone.tr(),
                        onClick = { showToneDialog = true })
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SettingsSectionHeader("APPEARANCE".tr())
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Default.DarkMode,
                        title = "Dark Mode".tr(),
                        subtitle = if (darkMode) "On".tr() else "Off".tr(),
                        rightElement = {
                            MedSwitch(
                                checked = darkMode,
                                onCheckedChange = { appViewModel.setDarkMode(it) })
                        })
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Default.Language, title = "Language".tr(),
                        subtitle = appViewModel.selectedLanguage.collectAsState().value,
                        onClick = { navViewModel.navigateTo(LanguageSettings) })
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SettingsSectionHeader("ABOUT".tr())
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = "App Version".tr(),
                        subtitle = "V1.0.0 (Build 2026.03)".tr(),
                        onClick = { navViewModel.navigateTo(AppVersion) })
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Default.Shield,
                        title = "Privacy Policy".tr(),
                        subtitle = "How we use your data".tr(),
                        onClick = { navViewModel.navigateTo(PrivacyPolicy) })
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SettingsSectionHeader("ACCOUNT".tr(), destructive = true)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .background(Surface, RoundedCornerShape(24.dp))
                        .border(
                            1.dp,
                            GhanaRed.copy(alpha = 0.1f),
                            RoundedCornerShape(24.dp)
                        )
                ) {
                    SettingsRow(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        title = "Log Out".tr(),
                        subtitle = "Sign out of your Medilert account".tr(),
                        isDestructive = true,
                        onClick = { showLogoutDialog = true })
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
                .width(140.dp)
                .height(5.dp)
                .background(Divider, RoundedCornerShape(50.dp))
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text("Log Out".tr(), fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Text(
                    "Are you sure you want to log out of Medilert?".tr(),
                    fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(onClick = {
                    showLogoutDialog = false
                    appViewModel.logout()
                    authViewModel.resetState()
                    navViewModel.navigateAndClearStack(Login)
                }, colors = ButtonDefaults.buttonColors(containerColor = GhanaRed)) {
                    Text("Log Out".tr(), fontFamily = Poppins, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel".tr(), fontFamily = Poppins, fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showToneDialog) {
        val tones = listOf("Happy Bells", "Bell", "Clear Tones", "Urgent Loop")
        AlertDialog(
            onDismissRequest = { 
                SoundPlayer.stopNotificationSound()
                selectedTone = notificationTone
                showToneDialog = false 
            },
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
            title = {
                Text("Notification Tone".tr(), fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                androidx.compose.foundation.layout.Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tones.forEach { tone ->
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (selectedTone == tone) PrimaryGreen.copy(alpha = 0.08f)
                                    else androidx.compose.ui.graphics.Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    if (selectedTone == tone) PrimaryGreen.copy(alpha = 0.3f)
                                    else BorderLight,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { 
                                    selectedTone = tone
                                    SoundPlayer.playNotificationSound(tone)
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                tone.tr(),
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            if (selectedTone == tone) {
                                Icon(
                                    androidx.compose.material.icons.Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { 
                    SoundPlayer.stopNotificationSound()
                    appViewModel.setNotificationTone(selectedTone)
                    showToneDialog = false 
                }) {
                    Text("OK".tr(), fontFamily = Poppins, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { 
                    SoundPlayer.stopNotificationSound()
                    selectedTone = notificationTone
                    showToneDialog = false 
                }) {
                    Text("Cancel".tr(), fontFamily = Poppins, fontWeight = FontWeight.Medium, color = TextSecondary)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String, destructive: Boolean = false) {
    Text(
        title, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 12.sp,
        color = if (destructive) GhanaRed else PrimaryGreen,
        modifier = Modifier
            .padding(horizontal = 28.dp)
            .padding(bottom = 10.dp),
        letterSpacing = 1.5.sp
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(24.dp))
            .border(1.dp, BorderLight, RoundedCornerShape(24.dp)),
        content = content
    )
    Spacer(modifier = Modifier.height(0.dp))
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    rightElement: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val iconBg = if (isDestructive) GhanaRed.copy(alpha = 0.1f) else PrimaryGreen.copy(alpha = 0.1f)
    val iconTint = if (isDestructive) GhanaRed else PrimaryGreen
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(iconBg, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = if (isDestructive) GhanaRed else TextPrimary
            )
            Text(
                subtitle,
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
        rightElement?.invoke() ?: Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = if (isDestructive) GhanaRed.copy(alpha = 0.5f) else BorderMedium,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 80.dp)
            .height(1.dp)
            .background(BorderLight)
    )
}

@Composable
fun MedSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked, onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = PrimaryGreen,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = BorderMedium
        )
    )
}
