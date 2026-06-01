package com.nkwabyte.medilert.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nkwabyte.medilert.data.service.CloudinaryService
import com.nkwabyte.medilert.model.UserRole
import com.nkwabyte.medilert.navigation.*
import com.nkwabyte.medilert.ui.screens.auth.AuthInputField
import com.nkwabyte.medilert.ui.theme.*
import com.nkwabyte.medilert.util.HapticFeedback
import com.nkwabyte.medilert.viewmodel.AppViewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navViewModel: NavViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentUser by appViewModel.currentUser.collectAsState()
    val userRole by appViewModel.userRole.collectAsState()
    val isCaregiver = userRole != UserRole.PATIENT

    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var specialty by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var photoUploadError by remember { mutableStateOf(false) }
    var saveSuccess by remember { mutableStateOf(false) }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    val specialtyLabel = when (userRole) {
        UserRole.DOCTOR -> "Medical Specialty"
        UserRole.PHARMACIST -> "Institution / Pharmacy"
        UserRole.GUARDIAN -> "Relationship to Patient"
        UserRole.PATIENT -> ""
    }
    val specialtyPlaceholder = when (userRole) {
        UserRole.DOCTOR -> "e.g. Cardiologist, General Practitioner"
        UserRole.PHARMACIST -> "e.g. Korle Bu Hospital Pharmacy"
        UserRole.GUARDIAN -> "e.g. Parent, Spouse, Sibling"
        UserRole.PATIENT -> ""
    }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    // Separate state for the persisted remote URL (Cloudinary) vs local preview URI
    var remotePhotoUrl by remember { mutableStateOf("") }

    LaunchedEffect(currentUser) {
        fullName = currentUser.name
        phone = currentUser.phone
        email = currentUser.email
        dob = currentUser.dateOfBirth
        gender = currentUser.gender.ifBlank { "Male" }
        specialty = currentUser.specialty
        emergencyContact = currentUser.emergencyContact
        remotePhotoUrl = currentUser.photoUrl
    }

    // Photo picker: pick → upload to Cloudinary → save URL
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        selectedPhotoUri = uri          // show preview immediately
        isUploadingPhoto = true
        photoUploadError = false
        coroutineScope.launch {
            val url = CloudinaryService.uploadProfilePhoto(context, uri)
            isUploadingPhoto = false
            if (url != null) {
                HapticFeedback.success(context)
                appViewModel.updateProfileInfo(photoUrl = url)
            } else {
                photoUploadError = true
                HapticFeedback.error(context)
            }
        }
    }

    val saveProfile = {
        isSaving = true
        if (isCaregiver) {
            appViewModel.updateProfileInfo(
                name = fullName, phone = phone, email = email,
                dateOfBirth = dob, gender = gender, specialty = specialty
            )
        } else {
            appViewModel.updateProfileInfo(
                name = fullName, phone = phone, email = email,
                dateOfBirth = dob, gender = gender, emergencyContact = emergencyContact
            )
        }
        HapticFeedback.success(context)
        saveSuccess = true
        isSaving = false
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().height(300.dp)
            .background(brush = Brush.verticalGradient(listOf(PrimaryGreen.copy(alpha = 0.05f), Color.Transparent))))

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 52.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).background(Surface, CircleShape)
                        .border(1.dp, BorderLight, CircleShape).clickable { navViewModel.popBack() },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = TextPrimary) }
                Text("Edit Profile", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                Spacer(modifier = Modifier.size(40.dp))
            }

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp).padding(top = 16.dp, bottom = 40.dp)
            ) {
                // ── Avatar with photo picker ──────────────────────────────────
                Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Box(
                        modifier = Modifier.size(108.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen.copy(alpha = 0.15f))
                            .border(3.dp, PrimaryGreen.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val photoSource: Any? = selectedPhotoUri ?: remotePhotoUrl.takeIf { it.isNotBlank() }
                        if (photoSource != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(photoSource)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Icon(Icons.Default.Person, contentDescription = null,
                                tint = PrimaryGreen, modifier = Modifier.size(56.dp))
                        }
                    }

                    // Camera button
                    Box(
                        modifier = Modifier.size(36.dp).align(Alignment.BottomEnd)
                            .background(PrimaryGreen, CircleShape)
                            .border(2.dp, Surface, CircleShape)
                            .clickable { photoPickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.CameraAlt, contentDescription = "Change photo", tint = Color.White, modifier = Modifier.size(20.dp)) }
                }

                Spacer(modifier = Modifier.height(8.dp))
                when {
                    isUploadingPhoto -> Row(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = PrimaryGreen)
                        Text("Uploading photo...", fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = PrimaryGreen)
                    }
                    photoUploadError -> Text("Upload failed. Tap camera to retry.", fontFamily = Poppins,
                        fontWeight = FontWeight.Medium, fontSize = 12.sp, color = GhanaRed,
                        modifier = Modifier.align(Alignment.CenterHorizontally))
                    selectedPhotoUri != null -> Text("Photo uploaded ✓", fontFamily = Poppins,
                        fontWeight = FontWeight.Medium, fontSize = 12.sp, color = DarkGreen,
                        modifier = Modifier.align(Alignment.CenterHorizontally))
                    else -> Text("Tap the camera icon to add a photo", fontFamily = Poppins,
                        fontWeight = FontWeight.Medium, fontSize = 12.sp, color = TextSecondary,
                        modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Save success banner
                if (saveSuccess) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .background(PrimaryGreen.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                            .border(1.dp, PrimaryGreen.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(18.dp))
                            Text("Profile saved successfully!", fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = DarkGreen)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                AuthInputField(label = "Full Name", value = fullName, onValueChange = { fullName = it; saveSuccess = false },
                    placeholder = "Full name", leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary) })
                Spacer(modifier = Modifier.height(16.dp))

                AuthInputField(label = "Phone", value = phone, onValueChange = { phone = it },
                    placeholder = "Phone number", leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TextSecondary) },
                    keyboardType = KeyboardType.Phone)
                Spacer(modifier = Modifier.height(16.dp))

                AuthInputField(label = "Email", value = email, onValueChange = { email = it },
                    placeholder = "Email address", leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary) },
                    keyboardType = KeyboardType.Email)
                Spacer(modifier = Modifier.height(16.dp))

                if (isCaregiver) {
                    AuthInputField(label = specialtyLabel, value = specialty, onValueChange = { specialty = it },
                        placeholder = specialtyPlaceholder, leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = TextSecondary) })
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Date of Birth", fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp, color = TextPrimary, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().height(64.dp).background(Surface, RoundedCornerShape(24.dp))
                            .border(1.dp, BorderLight, RoundedCornerShape(24.dp))
                            .clickable { showDatePicker = true }.padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                            Text(dob.ifBlank { "DD / MM / YYYY" }, fontFamily = Poppins, fontWeight = FontWeight.Medium,
                                fontSize = 15.sp, color = if (dob.isBlank()) TextHint else TextPrimary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Gender", fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp, color = TextPrimary, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("Male", "Female", "Other").forEach { option ->
                            Box(
                                modifier = Modifier.weight(1f)
                                    .background(if (gender == option) PrimaryGreen else Surface, RoundedCornerShape(16.dp))
                                    .border(2.dp, if (gender == option) PrimaryGreen else BorderLight, RoundedCornerShape(16.dp))
                                    .clickable { gender = option }.padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) { Text(option, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = if (gender == option) Color.White else TextPrimary) }
                        }
                    }
                }

                if (!isCaregiver) {
                    Spacer(modifier = Modifier.height(16.dp))
                    AuthInputField(label = "Emergency Contact", value = emergencyContact, onValueChange = { emergencyContact = it },
                        placeholder = "Emergency contact number", leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TextSecondary) },
                        keyboardType = KeyboardType.Phone)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = saveProfile,
                    enabled = !isSaving && fullName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .background(brush = Brush.horizontalGradient(listOf(PrimaryGreen, MediumGreen)), shape = RoundedCornerShape(50.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isSaving) "Saving..." else "Save Changes",
                            fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
            .width(140.dp).height(5.dp).background(Divider, RoundedCornerShape(50.dp)))
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            dob = SimpleDateFormat("dd / MM / yyyy", Locale.getDefault()).format(Date(millis))
                        }
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) { Text("OK", fontFamily = Poppins, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", fontFamily = Poppins, fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = PrimaryGreen,
                    todayContentColor = PrimaryGreen,
                    todayDateBorderColor = PrimaryGreen
                )
            )
        }
    }
}
