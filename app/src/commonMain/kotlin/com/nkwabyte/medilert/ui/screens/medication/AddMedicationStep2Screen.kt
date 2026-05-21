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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.navigation.*
import com.nkwabyte.medilert.ui.theme.*
import com.nkwabyte.medilert.viewmodel.MedicationViewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel

private val frequencies = listOf(
    "Once daily", "Twice daily", "Three times daily", "Four times daily", "Every other day", "Weekly", "As needed"
)

@Composable
fun AddMedicationStep2Screen(
    navViewModel: NavViewModel = viewModel(),
    medicationViewModel: MedicationViewModel = viewModel()
) {
    val draftMedication by medicationViewModel.draftMedication.collectAsState()
    var selectedFrequency by remember { mutableStateOf(draftMedication.frequency) }
    var dose by remember { mutableStateOf(draftMedication.dose.toString()) }

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

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(4) { i ->
                        if (i <= 1) Box(modifier = Modifier.width(if (i == 1) 32.dp else 6.dp).height(6.dp).background(PrimaryGreen, RoundedCornerShape(50.dp)))
                        else Box(modifier = Modifier.size(6.dp).background(BorderMedium, CircleShape))
                    }
                }

                Spacer(modifier = Modifier.size(40.dp))
            }

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 120.dp)
            ) {
                Text("How often do you take it?", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextPrimary,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 24.dp))

                Text("Dose Amount", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.padding(start = 4.dp, bottom = 12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(20.dp)).border(1.dp, BorderLight, RoundedCornerShape(20.dp)).padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(44.dp).background(PrimaryGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).clickable { if (dose.toInt() > 1) dose = (dose.toInt() - 1).toString() },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Remove, contentDescription = null, tint = PrimaryGreen) }

                    Text(dose, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = TextPrimary)

                    Box(
                        modifier = Modifier.size(44.dp).background(PrimaryGreen, RoundedCornerShape(12.dp)).clickable { dose = (dose.toInt() + 1).toString() },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Frequency", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.padding(start = 4.dp, bottom = 12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    frequencies.forEach { freq ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .background(if (selectedFrequency == freq) PrimaryGreen.copy(alpha = 0.05f) else Surface, RoundedCornerShape(16.dp))
                                .border(2.dp, if (selectedFrequency == freq) PrimaryGreen else BorderLight, RoundedCornerShape(16.dp))
                                .clickable { selectedFrequency = freq }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(freq, fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = TextPrimary)
                            if (selectedFrequency == freq) {
                                Box(modifier = Modifier.size(22.dp).background(PrimaryGreen, CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        val updatedMedication = draftMedication.copy(
                            frequency = selectedFrequency,
                            dose = dose.toInt()
                        )
                        medicationViewModel.updateDraftMedication(updatedMedication)
                        navViewModel.navigateTo(AddMedication3)
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaYellow)
                ) { Text("Continue", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = TextPrimary) }
            }
        }
    }
}
