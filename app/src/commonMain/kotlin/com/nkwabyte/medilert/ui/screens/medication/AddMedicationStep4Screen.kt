package com.nkwabyte.medilert.ui.screens.medication

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.model.UserRole
import com.nkwabyte.medilert.navigation.AppDestination
import com.nkwabyte.medilert.navigation.CareGiverDashboard
import com.nkwabyte.medilert.navigation.Dashboard
import com.nkwabyte.medilert.ui.theme.Background
import com.nkwabyte.medilert.ui.theme.BorderLight
import com.nkwabyte.medilert.ui.theme.Divider
import com.nkwabyte.medilert.ui.theme.MediumGreen
import com.nkwabyte.medilert.ui.theme.Poppins
import com.nkwabyte.medilert.ui.theme.PrimaryGreen
import com.nkwabyte.medilert.ui.theme.Surface
import com.nkwabyte.medilert.ui.theme.TextHint
import com.nkwabyte.medilert.ui.theme.TextPrimary
import com.nkwabyte.medilert.viewmodel.AppViewModel
import com.nkwabyte.medilert.viewmodel.MedicationViewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationStep4Screen(
    navViewModel: NavViewModel = viewModel(),
    medicationViewModel: MedicationViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel()
) {
    val draftMedication by medicationViewModel.draftMedication.collectAsState()
    val userRole by appViewModel.userRole.collectAsState()
    var totalPills by remember { mutableStateOf(draftMedication.currentInventory.toString()) }
    var sideEffects by remember { mutableStateOf(draftMedication.sideEffects) }
    var notes by remember { mutableStateOf(draftMedication.notes) }
    var startDate by remember { mutableStateOf(draftMedication.startDate) }
    var endDate by remember { mutableStateOf(draftMedication.endDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    var datePickerTarget by remember { mutableStateOf("start") }

    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDate = datePickerState.selectedDateMillis?.let {
                            Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                        }
                        if (selectedDate != null) {
                            if (datePickerTarget == "start") {
                                startDate = selectedDate
                            } else {
                                endDate = selectedDate
                            }
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
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

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 52.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .width(if (it == 3) 32.dp else 6.dp)
                                .height(6.dp)
                                .background(PrimaryGreen, RoundedCornerShape(50.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.size(40.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 120.dp)
            ) {
                Text(
                    "Additional Details",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 24.dp)
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Start Date",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(Surface, RoundedCornerShape(20.dp))
                            .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
                            .clickable {
                                datePickerTarget = "start"
                                showDatePicker = true
                            }
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = if (startDate.isNotEmpty()) startDate else "Select start date",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = if (startDate.isNotEmpty()) TextPrimary else TextHint
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "End Date (Optional)",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(Surface, RoundedCornerShape(20.dp))
                            .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
                            .clickable {
                                datePickerTarget = "end"
                                showDatePicker = true
                            }
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = if (endDate.isNotEmpty()) endDate else "Select end date",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = if (endDate.isNotEmpty()) TextPrimary else TextHint
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Current Supply (pills)",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Surface, RoundedCornerShape(20.dp))
                            .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    PrimaryGreen.copy(alpha = 0.1f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    if (totalPills.toIntOrNull()?.let { it > 1 } == true)
                                        totalPills = (totalPills.toInt() - 1).toString()
                                }, contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = null,
                                tint = PrimaryGreen
                            )
                        }
                        Text(
                            totalPills,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(PrimaryGreen, RoundedCornerShape(12.dp))
                                .clickable { totalPills = (totalPills.toInt() + 1).toString() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Known Side Effects",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = sideEffects, onValueChange = { sideEffects = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = {
                            Text(
                                "E.g. nausea, dizziness",
                                fontFamily = Poppins,
                                color = TextHint,
                                fontSize = 14.sp
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Surface,
                            focusedContainerColor = Surface,
                            unfocusedBorderColor = BorderLight,
                            focusedBorderColor = PrimaryGreen
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Notes",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = notes, onValueChange = { notes = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = {
                            Text(
                                "Any additional notes...",
                                fontFamily = Poppins,
                                color = TextHint,
                                fontSize = 14.sp
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Surface,
                            focusedContainerColor = Surface,
                            unfocusedBorderColor = BorderLight,
                            focusedBorderColor = PrimaryGreen
                        )
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        val updatedMedication = draftMedication.copy(
                            currentInventory = totalPills.toInt(),
                            sideEffects = sideEffects,
                            notes = notes,
                            startDate = startDate,
                            endDate = endDate
                        )
                        medicationViewModel.updateDraftMedication(updatedMedication)
                        medicationViewModel.saveDraftMedication()
                        val dest: AppDestination =
                            if (userRole == UserRole.PATIENT || userRole == UserRole.GUARDIAN) Dashboard else CareGiverDashboard
                        navViewModel.navigateAndClearStack(dest)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        PrimaryGreen,
                                        MediumGreen
                                    )
                                ), shape = RoundedCornerShape(50.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Save Medication",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }
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
}
