package com.nkwabyte.medilert.ui.screens.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.navigation.*
import com.nkwabyte.medilert.ui.components.TopBarStripe
import com.nkwabyte.medilert.ui.theme.*
import com.nkwabyte.medilert.viewmodel.AppViewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel

@Composable
fun VoiceAccessibilityScreen(
    navViewModel: NavViewModel = viewModel { NavViewModel() },
    appViewModel: AppViewModel = viewModel { AppViewModel() }
) {
    var voiceEnabled by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        TopBarStripe(modifier = Modifier.align(Alignment.TopCenter))
        Column(modifier = Modifier.fillMaxSize().padding(top = 6.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 46.dp, bottom = 16.dp)) {
                Box(
                    modifier = Modifier.size(40.dp).background(Surface, CircleShape).border(1.dp, BorderLight, CircleShape).clickable { navViewModel.popBack() },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = TextPrimary) }
            }
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                Box(
                    modifier = Modifier.size(120.dp).background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (voiceEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(56.dp)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text("Voice Accessibility", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = TextPrimary, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Enable voice alerts so Medilert can read your medication reminders aloud",
                    fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 15.sp,
                    color = TextSecondary, textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(48.dp))
                VoiceOptionCard(title = "Enable Voice Alerts", subtitle = "Reminders will be read aloud", isSelected = voiceEnabled) { voiceEnabled = true }
                Spacer(modifier = Modifier.height(12.dp))
                VoiceOptionCard(title = "Disable Voice Alerts", subtitle = "Silent notifications only", isSelected = !voiceEnabled) { voiceEnabled = false }
            }
            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                Button(
                    onClick = {
                        appViewModel.setVoiceEnabled(voiceEnabled)
                        navViewModel.navigateTo(PersonalInfo)
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaYellow)
                ) { Text("Continue", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = TextPrimary) }
            }
        }
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp).width(140.dp).height(5.dp).background(Divider, RoundedCornerShape(50.dp)))
    }
}

@Composable
fun VoiceOptionCard(title: String, subtitle: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Surface, RoundedCornerShape(20.dp))
            .border(2.dp, if (isSelected) PrimaryGreen else BorderLight, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary)
            Text(subtitle, fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = TextSecondary)
        }
        Box(
            modifier = Modifier.size(24.dp)
                .background(if (isSelected) PrimaryGreen else Color.Transparent, CircleShape)
                .border(2.dp, if (isSelected) PrimaryGreen else BorderMedium, CircleShape)
        )
    }
}
