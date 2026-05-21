package com.nkwabyte.medilert.ui.screens.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.navigation.Login
import com.nkwabyte.medilert.navigation.Onboarding2
import com.nkwabyte.medilert.ui.components.TopBarStripe
import com.nkwabyte.medilert.ui.theme.Background
import com.nkwabyte.medilert.ui.theme.GhanaYellow
import com.nkwabyte.medilert.ui.theme.Poppins
import com.nkwabyte.medilert.ui.theme.PrimaryGreen
import com.nkwabyte.medilert.viewmodel.NavViewModel

@Composable
fun OnboardingScreen1(navViewModel: NavViewModel = viewModel()) {
    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        TopBarStripe(modifier = Modifier.align(Alignment.TopCenter))

        Box(modifier = Modifier.size(300.dp).align(Alignment.TopEnd).background(Brush.radialGradient(listOf(PrimaryGreen.copy(alpha = 0.05f), Color.Transparent)), CircleShape))
        Box(modifier = Modifier.size(250.dp).align(Alignment.BottomStart).background(Brush.radialGradient(listOf(GhanaYellow.copy(alpha = 0.08f), Color.Transparent)), CircleShape))

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Box(
                modifier = Modifier.size(200.dp).background(PrimaryGreen.copy(alpha = 0.08f), RoundedCornerShape(40.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(100.dp).background(PrimaryGreen.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(60.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(text = "Never Miss a Dose", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.Black, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Set smart reminders for your medications and get notified at the right time, every time.", fontFamily = Poppins, fontWeight = FontWeight.Normal, fontSize = 16.sp, color = Color(0xFF6B7280), textAlign = TextAlign.Center, lineHeight = 26.sp)

            Spacer(modifier = Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(24.dp).height(8.dp).background(PrimaryGreen, RoundedCornerShape(50.dp)))
                Spacer(modifier = Modifier.width(6.dp))
                Box(modifier = Modifier.size(8.dp).background(Color(0xFFE5E7EB), CircleShape))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = { navViewModel.navigateTo(Onboarding2) }, modifier = Modifier.fillMaxWidth().height(60.dp), shape = RoundedCornerShape(50.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)) {
                Text(text = "Next", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navViewModel.navigateAndClearStack(Login) }) {
                Text(text = "Skip", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = PrimaryGreen)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)) {
            Box(modifier = Modifier.width(140.dp).height(5.dp).background(Color(0xFFE4E4E4), RoundedCornerShape(50.dp)))
        }
    }
}
