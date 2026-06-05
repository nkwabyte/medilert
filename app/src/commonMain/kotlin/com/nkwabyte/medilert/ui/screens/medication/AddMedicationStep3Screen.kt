package com.nkwabyte.medilert.ui.screens.medication

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.generated.resources.Res
import com.nkwabyte.medilert.generated.resources.img_onboarding1
import com.nkwabyte.medilert.model.MedicationIntake
import com.nkwabyte.medilert.navigation.*
import com.nkwabyte.medilert.ui.theme.*
import com.nkwabyte.medilert.viewmodel.MedicationViewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationStep3Screen(
    navViewModel: NavViewModel = viewModel { NavViewModel() },
    medicationViewModel: MedicationViewModel = viewModel { MedicationViewModel() }
) {
    val draftMedication by medicationViewModel.draftMedication.collectAsState()
    val frequency by medicationViewModel.draftFrequency.collectAsState()
    var intakes by remember { mutableStateOf(draftMedication.intakes) }
    var withFood by remember { mutableStateOf(draftMedication.instructions.contains("with food")) }
    val showEditReminderDialog = remember { mutableStateOf(false) }
    val selectedIntakeIndex = remember { mutableStateOf(-1) }

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
                    val defaultTime = when (i) { 0 -> "08:00 AM"; 1 -> "12:00 PM"; 2 -> "06:00 PM"; else -> "10:00 PM" }
                    val defaultTitle = when (i) { 0 -> "Morning"; 1 -> "Afternoon"; 2 -> "Evening"; else -> "Night" }
                    newIntakes.add(MedicationIntake(title = defaultTitle, time = defaultTime, dose = draftMedication.dose))
                }
            }
            intakes = newIntakes
        }
    }

    val timePickerState = rememberTimePickerState()

    if (showEditReminderDialog.value) {
        val intake = intakes.getOrNull(selectedIntakeIndex.value)
        if (intake != null) {
            EditReminderDialog(
                onCancel = { showEditReminderDialog.value = false },
                onConfirm = { title, time ->
                    intakes = intakes.toMutableList().apply {
                        this[selectedIntakeIndex.value] = this[selectedIntakeIndex.value].copy(title = title, time = time)
                    }
                    showEditReminderDialog.value = false
                },
                initialTitle = intake.title,
                timePickerState = timePickerState
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {

        Column(modifier = Modifier.fillMaxSize()) {
            // Photo header
            Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                Image(
                    painter = painterResource(Res.drawable.img_onboarding1),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(0.35f to Color.Transparent, 1.00f to Background)
                    )
                )
            }

            // Scrollable form
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp).padding(bottom = 120.dp)
            ) {
                Text(
                    "Set reminder times",
                    fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextPrimary,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)
                )
                Text(
                    "Based on: $frequency",
                    fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextSecondary,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                )

                intakes.forEachIndexed { index, intake ->
                    TimeSlotCard(
                        label = intake.title.ifEmpty { "Reminder ${index + 1}" },
                        time = intake.time,
                        onClick = { selectedIntakeIndex.value = index; showEditReminderDialog.value = true }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Button(
                        onClick = { intakes = intakes + MedicationIntake(time = "08:00 AM", dose = draftMedication.dose) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = PrimaryGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Reminder", color = PrimaryGreen)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { if (intakes.size > 1) intakes = intakes.dropLast(1) },
                        colors = ButtonDefaults.buttonColors(containerColor = GhanaRed.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Remove", tint = GhanaRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Remove", color = GhanaRed)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(20.dp)).border(1.dp, BorderLight, RoundedCornerShape(20.dp)).padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Take with food", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                        Text("Medication should be taken with meals", fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = TextSecondary)
                    }
                    Switch(checked = withFood, onCheckedChange = { withFood = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryGreen))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        medicationViewModel.updateDraftMedication(
                            draftMedication.copy(intakes = intakes, instructions = if (withFood) "Take with food" else "")
                        )
                        navViewModel.navigateTo(AddMedication4)
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaYellow)
                ) { Text("Continue", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = TextPrimary) }
            }
        }

        // Floating nav row — back button + step indicator
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp)
                    .background(Color.Black.copy(alpha = 0.28f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                    .clickable { navViewModel.popBack() },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color.White) }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(4) { i ->
                    if (i <= 2) Box(modifier = Modifier.width(if (i == 2) 32.dp else 6.dp).height(6.dp).background(Color.White, RoundedCornerShape(50.dp)))
                    else Box(modifier = Modifier.size(6.dp).background(Color.White.copy(alpha = 0.45f), CircleShape))
                }
            }

            Spacer(modifier = Modifier.size(40.dp))
        }

        // Home indicator
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
                .width(140.dp).height(5.dp).background(Divider, RoundedCornerShape(50.dp))
        )
    }
}
