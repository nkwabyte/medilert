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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.model.MedicationIntake
import com.nkwabyte.medilert.ui.screens.auth.AuthInputField
import com.nkwabyte.medilert.ui.theme.Background
import com.nkwabyte.medilert.ui.theme.BorderLight
import com.nkwabyte.medilert.ui.theme.Divider
import com.nkwabyte.medilert.ui.theme.GhanaRed
import com.nkwabyte.medilert.ui.theme.MediumGreen
import com.nkwabyte.medilert.ui.theme.Poppins
import com.nkwabyte.medilert.ui.theme.PrimaryGreen
import com.nkwabyte.medilert.ui.theme.Surface
import com.nkwabyte.medilert.ui.theme.TextHint
import com.nkwabyte.medilert.ui.theme.TextPrimary
import com.nkwabyte.medilert.ui.theme.TextSecondary
import com.nkwabyte.medilert.viewmodel.MedicationViewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel
import java.util.Calendar

private val frequencies = listOf(
    "Once daily",
    "Twice daily",
    "Three times daily",
    "Four times daily",
    "Every other day",
    "Weekly",
    "As needed"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMedicationScreen(
    medicationId: String,
    navViewModel: NavViewModel = viewModel(),
    medicationViewModel: MedicationViewModel = viewModel()
) {
    LaunchedEffect(medicationId) {
        medicationViewModel.startEditMedication(medicationId)
    }

    val draftMedication by medicationViewModel.draftMedication.collectAsState()

    var medName by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("Tablet(s)") }
    var frequency by remember { mutableStateOf("Once daily") }
    var notes by remember { mutableStateOf("") }
    var intakes by remember { mutableStateOf<List<MedicationIntake>>(emptyList()) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val showTimePicker = remember { mutableStateOf(false) }
    val selectedIntakeIndex = remember { mutableIntStateOf(-1) }
    var showDatePicker by remember { mutableStateOf(false) }
    var datePickerTarget by remember { mutableStateOf("start") } // "start" or "end"

    LaunchedEffect(draftMedication) {
        if (draftMedication.id == medicationId) {
            medName = draftMedication.name
            dose = draftMedication.dose.toString()
            unit = draftMedication.unit
            frequency = draftMedication.frequency
            notes = draftMedication.notes
            intakes = draftMedication.intakes
            startDate = draftMedication.startDate
            endDate = draftMedication.endDate
        }
    }

    LaunchedEffect(frequency) {
        val newIntakeCount = when {
            frequency.contains("Once") -> 1
            frequency.contains("Twice") -> 2
            frequency.contains("Three") -> 3
            frequency.contains("Four") -> 4
            else -> 1
        }

        if (newIntakeCount != intakes.size) {
            val newIntakes = mutableListOf<MedicationIntake>()
            for (i in 0 until newIntakeCount) {
                if (i < intakes.size) {
                    newIntakes.add(intakes[i])
                } else {
                    // Add new default intake
                    val defaultTime = when (i) {
                        0 -> "08:00 AM"
                        1 -> "12:00 PM"
                        2 -> "06:00 PM"
                        3 -> "10:00 PM"
                        else -> "08:00 AM"
                    }
                    val defaultTitle = when (i) {
                        0 -> "Morning"
                        1 -> "Afternoon"
                        2 -> "Evening"
                        3 -> "Night"
                        else -> ""
                    }
                    newIntakes.add(
                        MedicationIntake(
                            title = defaultTitle,
                            time = defaultTime,
                            dose = dose.toIntOrNull() ?: 1
                        )
                    )
                }
            }
            intakes = newIntakes
        }
    }


    val timePickerState = rememberTimePickerState()
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDate = datePickerState.selectedDateMillis?.let {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = it
                            android.text.format.DateFormat.format("yyyy-MM-dd", cal).toString()
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

    if (showTimePicker.value) {
        val intake = intakes.getOrNull(selectedIntakeIndex.value)
        if (intake != null) {
            EditReminderDialog(
                onCancel = { showTimePicker.value = false },
                onConfirm = { title, time ->
                    intakes = intakes.toMutableList().apply {
                        this[selectedIntakeIndex.value] =
                            this[selectedIntakeIndex.value].copy(title = title, time = time)
                    }
                    showTimePicker.value = false
                },
                initialTitle = intake.title,
                timePickerState = timePickerState
            )
        }
    }


    Box(modifier = Modifier
        .fillMaxSize()
        .background(Background)) {
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

                Text(
                    "Edit Medication",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(GhanaRed.copy(alpha = 0.1f), CircleShape)
                        .clickable { showDeleteDialog = true },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = GhanaRed) }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 120.dp, top = 16.dp)
            ) {
                AuthInputField(
                    label = "Medication Name",
                    value = medName,
                    onValueChange = { medName = it },
                    placeholder = "Medication name",
                    leadingIcon = {
                        Icon(
                            Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    })

                Spacer(modifier = Modifier.height(16.dp))

                // Dose
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Dose Amount",
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
                                    if (dose.toInt() > 1) dose = (dose.toInt() - 1).toString()
                                }, contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Remove, null, tint = PrimaryGreen)
                        }
                        Text(
                            "$dose $unit",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(PrimaryGreen, RoundedCornerShape(12.dp))
                                .clickable { dose = (dose.toInt() + 1).toString() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Frequency
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Frequency",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        frequencies.forEach { freq ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (frequency == freq) PrimaryGreen.copy(alpha = 0.05f) else Surface,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        2.dp,
                                        if (frequency == freq) PrimaryGreen else BorderLight,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { frequency = freq }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    freq,
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                if (frequency == freq) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .background(PrimaryGreen, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Start Date
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

                Spacer(modifier = Modifier.height(16.dp))

                // End Date
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

                Spacer(modifier = Modifier.height(16.dp))

                // Reminder times
                Text(
                    "Reminder Times",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                intakes.forEachIndexed { index, intake ->
                    TimeSlotCard(
                        label = intake.title.ifEmpty { "Reminder ${index + 1}" },
                        time = intake.time,
                        onClick = {
                            selectedIntakeIndex.value = index
                            showTimePicker.value = true
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }


                Spacer(modifier = Modifier.height(16.dp))

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
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = {
                            Text(
                                "Add notes...",
                                fontFamily = Poppins,
                                color = TextHint
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

                Spacer(modifier = Modifier.height(120.dp))
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Background,
                            Background
                        )
                    )
                )
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Button(
                onClick = {
                    val updatedMedication = draftMedication.copy(
                        name = medName,
                        dose = dose.toIntOrNull() ?: 1,
                        unit = unit,
                        frequency = frequency,
                        notes = notes,
                        intakes = intakes,
                        startDate = startDate,
                        endDate = endDate
                    )
                    medicationViewModel.updateMedication(updatedMedication) {
                        navViewModel.popBack()
                    }
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
                        "Save Changes",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Delete Medication",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete this medication? This action cannot be undone.",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        medicationViewModel.deleteMedication(medicationId)
                        showDeleteDialog = false
                        navViewModel.popBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaRed)
                ) {
                    Text("Delete", fontFamily = Poppins, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        "Cancel",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}
