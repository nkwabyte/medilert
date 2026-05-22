package com.nkwabyte.medilert.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
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
import com.nkwabyte.medilert.viewmodel.NavViewModel
import com.nkwabyte.medilert.ui.theme.*
import org.jetbrains.compose.resources.painterResource
import com.nkwabyte.medilert.generated.resources.Res
import com.nkwabyte.medilert.generated.resources.logo

@Composable
fun AppVersionScreen(navViewModel: NavViewModel = viewModel { NavViewModel() }) {
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
                Text("About Medilert", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                Spacer(modifier = Modifier.size(40.dp))
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(40.dp))

                Image(painter = painterResource(Res.drawable.logo), contentDescription = "Logo", modifier = Modifier.size(100.dp), contentScale = ContentScale.Fit)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Medilert", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = PrimaryGreen)
                Text("Your Medication Companion", fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = TextSecondary)

                Spacer(modifier = Modifier.height(32.dp))

                listOf(
                    "Version" to "1.0.0",
                    "Build" to "2026.03.14",
                    "Platform" to "Android",
                    "Developer" to "Nkwabyte Technologies",
                    "Contact" to "support@medilert.com"
                ).forEach { (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(16.dp)).border(1.dp, BorderLight, RoundedCornerShape(16.dp)).padding(horizontal = 20.dp, vertical = 12.dp).padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                        Text(value, fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("© 2026 Nkwabyte Technologies. All rights reserved.", fontFamily = Poppins, fontWeight = FontWeight.Normal, fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center)
            }
        }
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp).width(140.dp).height(5.dp).background(Divider, RoundedCornerShape(50.dp)))
    }
}
