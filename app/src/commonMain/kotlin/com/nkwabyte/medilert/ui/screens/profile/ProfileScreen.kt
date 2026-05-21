package com.nkwabyte.medilert.ui.screens.profile

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.model.UserRole
import com.nkwabyte.medilert.navigation.*
import com.nkwabyte.medilert.ui.screens.auth.AuthInputField
import com.nkwabyte.medilert.ui.theme.*
import com.nkwabyte.medilert.viewmodel.AppViewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navViewModel: NavViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel()
) {
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
    var showDatePicker by remember { mutableStateOf(false) }

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

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    LaunchedEffect(currentUser) {
        fullName = currentUser.name
        phone = currentUser.phone
        email = currentUser.email
        dob = currentUser.dateOfBirth
        gender = currentUser.gender.ifBlank { "Male" }
        specialty = currentUser.specialty
        emergencyContact = currentUser.emergencyContact
    }

    val saveProfile = {
        isSaving = true
        if (isCaregiver) {
            appViewModel.updateProfileInfo(
                name = fullName,
                phone = phone,
                email = email,
                dateOfBirth = dob,
                gender = gender,
                specialty = specialty
            )
        } else {
            appViewModel.updateProfileInfo(
                name = fullName,
                phone = phone,
                email = email,
                dateOfBirth = dob,
                gender = gender,
                emergencyContact = emergencyContact
            )
        }
        navViewModel.popBack()
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().height(300.dp).background(brush = Brush.verticalGradient(listOf(PrimaryGreen.copy(alpha = 0.05f), Color.Transparent))))

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 52.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).background(Surface, CircleShape).border(1.dp, BorderLight, CircleShape).clickable { navViewModel.popBack() },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = TextPrimary) }
                Text("Edit Profile", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                Spacer(modifier = Modifier.size(40.dp))
            }

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(top = 16.dp, bottom = 40.dp)
            ) {
                Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Box(
                        modifier = Modifier.size(100.dp).background(PrimaryGreen.copy(alpha = 0.15f), CircleShape).border(3.dp, PrimaryGreen.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(52.dp)) }

                    Box(
                        modifier = Modifier.size(32.dp).align(Alignment.BottomEnd).background(PrimaryGreen, CircleShape).clickable { navViewModel.navigateTo(ProfilePhotoView) },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.CameraAlt, contentDescription = "Edit photo", tint = Color.White, modifier = Modifier.size(18.dp)) }
                }

                Spacer(modifier = Modifier.height(32.dp))

                AuthInputField(label = "Full Name", value = fullName, onValueChange = { fullName = it }, placeholder = "Full name",
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary) })
                Spacer(modifier = Modifier.height(16.dp))

                AuthInputField(label = "Phone", value = phone, onValueChange = { phone = it }, placeholder = "Phone number",
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TextSecondary) }, keyboardType = KeyboardType.Phone)
                Spacer(modifier = Modifier.height(16.dp))

                AuthInputField(label = "Email", value = email, onValueChange = { email = it }, placeholder = "Email address",
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary) }, keyboardType = KeyboardType.Email)
                Spacer(modifier = Modifier.height(16.dp))

                if (isCaregiver) {
                    AuthInputField(
                        label = specialtyLabel,
                        value = specialty,
                        onValueChange = { specialty = it },
                        placeholder = specialtyPlaceholder,
                        leadingIcon = {
                            Icon(Icons.Default.Work, contentDescription = null, tint = TextSecondary)
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Date of Birth",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(Surface, RoundedCornerShape(24.dp))
                            .border(1.dp, BorderLight, RoundedCornerShape(24.dp))
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = dob.ifBlank { "DD / MM / YYYY" },
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                color = if (dob.isBlank()) TextHint else TextPrimary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Gender", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
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
                    AuthInputField(
                        label = "Emergency Contact",
                        value = emergencyContact,
                        onValueChange = { emergencyContact = it },
                        placeholder = "Emergency contact number",
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = TextSecondary)
                        },
                        keyboardType = KeyboardType.Phone
                    )
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
                        modifier = Modifier.fillMaxSize().background(brush = Brush.horizontalGradient(listOf(PrimaryGreen, MediumGreen)), shape = RoundedCornerShape(50.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isSaving) "Saving..." else "Save Changes",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp).width(140.dp).height(5.dp).background(Divider, RoundedCornerShape(50.dp)))
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault()).date
                            dob = "${localDate.dayOfMonth.toString().padStart(2, '0')} / ${localDate.monthNumber.toString().padStart(2, '0')} / ${localDate.year}"
                        }
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("OK", fontFamily = Poppins, fontWeight = FontWeight.SemiBold)
                }
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
