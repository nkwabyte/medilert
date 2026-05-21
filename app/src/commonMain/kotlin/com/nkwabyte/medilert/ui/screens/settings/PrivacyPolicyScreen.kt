package com.nkwabyte.medilert.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel
import com.nkwabyte.medilert.ui.theme.*

@Composable
fun PrivacyPolicyScreen(navViewModel: NavViewModel = viewModel()) {
    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 52.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).background(Surface, CircleShape).border(1.dp, BorderLight, CircleShape).clickable { navViewModel.popBack() },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = TextPrimary) }
                Text("Privacy Policy", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                Spacer(modifier = Modifier.size(40.dp))
            }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 16.dp)) {
                PrivacySection("1. Information We Collect",
                    "Medilert collects personal information including your name, phone number, email address, and medication details to provide our service. We also collect usage data to improve your experience.")

                PrivacySection("2. How We Use Your Information",
                    "Your information is used to send medication reminders, track adherence, and provide health insights. We do not sell your personal data to third parties.")

                PrivacySection("3. Data Security",
                    "We implement industry-standard security measures to protect your data. All data is encrypted in transit and at rest. Your PIN and password are stored using secure hashing algorithms.")

                PrivacySection("4. Data Sharing",
                    "If you are a patient, your medication adherence data may be shared with your assigned caregiver or doctor. You can control this in Settings > My Care Team.")

                PrivacySection("5. Your Rights",
                    "You have the right to access, correct, or delete your personal data at any time. Contact us at privacy@medilert.com to exercise these rights.")

                PrivacySection("6. Contact Us",
                    "For any privacy-related concerns, contact our Data Protection Officer at: privacy@medilert.com\n\nLast updated: March 2026")
            }
        }
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp).width(140.dp).height(5.dp).background(Divider, RoundedCornerShape(50.dp)))
    }
}

@Composable
fun PrivacySection(title: String, content: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Text(title, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(content, fontFamily = Poppins, fontWeight = FontWeight.Normal, fontSize = 14.sp, color = TextSecondary, lineHeight = 22.sp)
    }
}
