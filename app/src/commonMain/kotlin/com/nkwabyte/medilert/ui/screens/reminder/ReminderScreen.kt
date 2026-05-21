package com.nkwabyte.medilert.ui.screens.reminder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import com.nkwabyte.medilert.viewmodel.NavViewModel
import com.nkwabyte.medilert.ui.theme.*

@Composable
fun ReminderScreen(
    navViewModel: NavViewModel = viewModel(),
    type: String,
    time: String
) {
    val (bgGradient, timeLabel, dotColor) = when (type) {
        "morning" -> Triple(listOf(GhanaRed, GhanaRedLight), "Morning", GhanaRed)
        "afternoon" -> Triple(listOf(GhanaYellow, GhanaYellowDark), "Afternoon", GhanaYellow)
        else -> Triple(listOf(PrimaryGreen, MediumGreen), "Evening", PrimaryGreen)
    }
    val textColor = if (type == "afternoon") TextPrimary else Color.White

    Box(
        modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(bgGradient))
    ) {
        Box(
            modifier = Modifier.align(Alignment.TopStart).padding(start = 24.dp, top = 56.dp).size(40.dp)
                .background(textColor.copy(alpha = 0.2f), CircleShape).border(1.dp, textColor.copy(alpha = 0.3f), CircleShape).clickable { navViewModel.popBack() },
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = textColor) }

        Box(
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 24.dp, top = 56.dp).size(40.dp)
                .background(textColor.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Sound", tint = textColor) }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.background(textColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp)).padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text("$timeLabel • $time", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = textColor)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier.size(120.dp).background(textColor.copy(alpha = 0.15f), CircleShape).border(3.dp, textColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = textColor, modifier = Modifier.size(60.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Vitamin C", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 40.sp, color = textColor)
            Text("500 mg • 1 Tablet", fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 18.sp, color = textColor.copy(alpha = 0.85f))

            Spacer(modifier = Modifier.height(8.dp))

            Text("Everyday", fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = textColor.copy(alpha = 0.7f))

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(6) { i ->
                    Box(modifier = Modifier.size(14.dp).background(
                        if (i < 2) GhanaYellow.copy(alpha = if (type == "afternoon") 0.6f else 1f) else textColor.copy(alpha = 0.3f), CircleShape
                    ))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("4 pills left", fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = textColor.copy(alpha = 0.7f))

            if (type != "afternoon") {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Side effects: abdominal pain", fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = textColor.copy(alpha = 0.7f),
                    modifier = Modifier.background(textColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp))
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { navViewModel.popBack() },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = textColor, contentColor = bgGradient.first())
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mark as Taken", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            OutlinedButton(
                onClick = { navViewModel.popBack() },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor),
                border = androidx.compose.foundation.BorderStroke(2.dp, textColor.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Snooze, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Snooze 5 min", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            }
        }
    }
}
