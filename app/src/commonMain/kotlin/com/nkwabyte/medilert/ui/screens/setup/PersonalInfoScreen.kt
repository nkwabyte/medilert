package com.nkwabyte.medilert.ui.screens.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.nkwabyte.medilert.navigation.SetPin
import com.nkwabyte.medilert.ui.components.TopBarStripe
import com.nkwabyte.medilert.ui.screens.auth.AuthInputField
import com.nkwabyte.medilert.ui.theme.Background
import com.nkwabyte.medilert.ui.theme.BorderLight
import com.nkwabyte.medilert.ui.theme.Divider
import com.nkwabyte.medilert.ui.theme.GhanaYellow
import com.nkwabyte.medilert.ui.theme.Poppins
import com.nkwabyte.medilert.ui.theme.PrimaryGreen
import com.nkwabyte.medilert.ui.theme.Surface
import com.nkwabyte.medilert.ui.theme.TextPrimary
import com.nkwabyte.medilert.ui.theme.TextSecondary
import com.nkwabyte.medilert.viewmodel.NavViewModel
import com.nkwabyte.medilert.viewmodel.SignupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(
    navViewModel: NavViewModel = viewModel(),
    signupViewModel: SignupViewModel = viewModel()
) {
    val signupData by signupViewModel.signupData.collectAsState()

    var fullName by remember { mutableStateOf(signupData.name) }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var emergencyContact by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val selectedDate by remember {
        derivedStateOf {
            datePickerState.selectedDateMillis?.let { millis ->
                val localDate = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault()).date
                "${localDate.dayOfMonth.toString().padStart(2, '0')}/${localDate.monthNumber.toString().padStart(2, '0')}/${localDate.year}"
            } ?: ""
        }
    }

    if (selectedDate.isNotEmpty() && selectedDate != dob) {
        dob = selectedDate
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Background)) {
        TopBarStripe(modifier = Modifier.align(Alignment.TopCenter))
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(top = 6.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 46.dp)
            ) {
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
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp, bottom = 32.dp)
            ) {
                Text(
                    "Personal Info",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Tell us a bit about yourself",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(32.dp))
                AuthInputField(
                    label = "Full Name", value = fullName, onValueChange = { fullName = it },
                    placeholder = "Enter your full name",
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Date of Birth",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = dob,
                        onValueChange = { },
                        readOnly = true,
                        placeholder = {
                            Text(
                                "DD/MM/YYYY",
                                fontFamily = Poppins,
                                fontSize = 15.sp,
                                color = TextSecondary.copy(alpha = 0.6f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = "Select date",
                                    tint = PrimaryGreen
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Surface,
                            unfocusedContainerColor = Surface,
                            disabledContainerColor = Surface,
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = BorderLight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = PrimaryGreen
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Gender",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("Male", "Female", "Other").forEach { option ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (gender == option) PrimaryGreen else Surface,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        2.dp,
                                        if (gender == option) PrimaryGreen else BorderLight,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { gender = option }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    option,
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = if (gender == option) Color.White else TextPrimary
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                AuthInputField(
                    label = "Emergency Contact",
                    value = emergencyContact,
                    onValueChange = { emergencyContact = it },
                    placeholder = "Emergency contact number",
                    leadingIcon = {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    },
                    keyboardType = KeyboardType.Phone
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        signupViewModel.setPersonalInfo(gender, dob, emergencyContact)
                        if (fullName != signupData.name) {
                            signupViewModel.setBasicInfo(fullName, signupData.email)
                        }
                        navViewModel.navigateTo(SetPin)
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaYellow)
                ) {
                    Text(
                        "Continue",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = TextPrimary
                    )
                }
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDatePicker = false
                        }
                    ) {
                        Text(
                            "OK",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryGreen
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDatePicker = false }
                    ) {
                        Text(
                            "Cancel",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                },
                colors = DatePickerDefaults.colors(
                    containerColor = Surface
                )
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = Surface,
                        selectedDayContainerColor = PrimaryGreen,
                        todayContentColor = PrimaryGreen,
                        todayDateBorderColor = PrimaryGreen,
                        selectedDayContentColor = Color.White,
                        headlineContentColor = TextPrimary,
                        weekdayContentColor = TextSecondary,
                        subheadContentColor = TextPrimary,
                        yearContentColor = TextPrimary,
                        currentYearContentColor = PrimaryGreen,
                        selectedYearContentColor = Color.White,
                        selectedYearContainerColor = PrimaryGreen,
                        dayContentColor = TextPrimary,
                        disabledDayContentColor = TextSecondary.copy(alpha = 0.3f),
                        disabledSelectedDayContainerColor = PrimaryGreen.copy(alpha = 0.3f),
                        disabledSelectedDayContentColor = Color.White.copy(alpha = 0.3f)
                    )
                )
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
}
