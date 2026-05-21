package com.nkwabyte.medilert.ui.screens.medication

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.model.Medication
import com.nkwabyte.medilert.navigation.*
import com.nkwabyte.medilert.ui.screens.auth.AuthInputField
import com.nkwabyte.medilert.ui.theme.*
import com.nkwabyte.medilert.viewmodel.MedicationViewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel

private val medicationIcons = listOf<ImageVector>(
    Icons.Default.MedicalServices, Icons.Default.Medication, Icons.Default.Science, Icons.Default.Vaccines
)
private val unitOptions = listOf("Tablet(s)", "Capsule(s)", "ml", "mg", "IU", "Drop(s)")

@Composable
fun AddMedicationStep1Screen(
    navViewModel: NavViewModel = viewModel(),
    medicationViewModel: MedicationViewModel = viewModel()
) {
    val draftMedication by medicationViewModel.draftMedication.collectAsState()
    var medName by remember { mutableStateOf(draftMedication.name) }
    var selectedUnit by remember { mutableStateOf(draftMedication.unit) }
    var selectedIcon by remember { mutableStateOf(draftMedication.icon) }
    var showUnitPicker by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().height(300.dp).background(brush = Brush.verticalGradient(listOf(PrimaryGreen.copy(alpha = 0.05f), Color.Transparent))))

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 52.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).background(Surface, CircleShape).border(1.dp, BorderLight, CircleShape).clickable { navViewModel.popBack() },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = TextPrimary) }

                // Step indicator
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.width(32.dp).height(6.dp).background(PrimaryGreen, RoundedCornerShape(50.dp)))
                    repeat(3) { Box(modifier = Modifier.size(6.dp, 6.dp).background(BorderMedium, CircleShape)) }
                }

                Spacer(modifier = Modifier.size(40.dp))
            }

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 120.dp)
            ) {
                Text(
                    "What medication do you want to add?",
                    fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextPrimary,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 32.dp)
                )

                AuthInputField(
                    label = "Medication Name", value = medName, onValueChange = { medName = it },
                    placeholder = "Enter medication name",
                    leadingIcon = { Icon(Icons.Default.MedicalServices, contentDescription = null, tint = TextSecondary) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Unit Type", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().height(64.dp)
                            .background(Surface, RoundedCornerShape(20.dp)).border(1.dp, BorderLight, RoundedCornerShape(20.dp))
                            .clickable { showUnitPicker = !showUnitPicker }.padding(horizontal = 24.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(selectedUnit, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = TextPrimary)
                            Icon(if (showUnitPicker) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextSecondary)
                        }
                    }
                    if (showUnitPicker) {
                        Column(modifier = Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(20.dp)).border(1.dp, BorderLight, RoundedCornerShape(20.dp))) {
                            unitOptions.forEach { unit ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { selectedUnit = unit; showUnitPicker = false }.padding(horizontal = 24.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(unit, fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = TextPrimary)
                                    if (selectedUnit == unit) Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Choose an Icon", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        medicationIcons.forEachIndexed { idx, icon ->
                            Box(
                                modifier = Modifier.weight(1f).aspectRatio(1f)
                                    .background(if (selectedIcon == idx) PrimaryGreen else Surface, RoundedCornerShape(20.dp))
                                    .border(1.dp, if (selectedIcon == idx) PrimaryGreen else BorderLight, RoundedCornerShape(20.dp))
                                    .clickable { selectedIcon = idx },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = null, tint = if (selectedIcon == idx) Color.White else TextSecondary, modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        val updatedMedication = draftMedication.copy(
                            name = medName,
                            unit = selectedUnit,
                            icon = selectedIcon
                        )
                        medicationViewModel.updateDraftMedication(updatedMedication)
                        navViewModel.navigateTo(AddMedication2)
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaYellow)
                ) { Text("Continue", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = TextPrimary) }
            }
        }
    }
}
