package com.nkwabyte.medilert.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MedicalServices
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
import com.nkwabyte.medilert.ui.components.BottomTabBar
import com.nkwabyte.medilert.ui.components.DashboardTab
import com.nkwabyte.medilert.ui.screens.settings.SettingsScreen
import com.nkwabyte.medilert.ui.theme.*
import com.nkwabyte.medilert.viewmodel.AppViewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel

@Composable
fun DashboardEmptyScreen(
    navViewModel: NavViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel()
) {
    var activeTab by remember { mutableStateOf(DashboardTab.HOME) }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        when (activeTab) {
            DashboardTab.HOME -> EmptyHomeContent(onAddMedication = { navViewModel.navigateTo(AddMedication1) })
            DashboardTab.HISTORY -> EmptyHistoryContent()
            DashboardTab.SETTINGS -> SettingsScreen(hideBackButton = true)
        }

        BottomTabBar(activeTab = activeTab, onTabSelected = { activeTab = it }, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun EmptyHomeContent(onAddMedication: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(start = 24.dp, end = 24.dp, bottom = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(120.dp).background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.MedicalServices, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(60.dp)) }

        Spacer(modifier = Modifier.height(32.dp))

        Text("No Medications Yet", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextPrimary, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Add your first medication to start tracking your doses and reminders", fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = TextSecondary, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onAddMedication,
            modifier = Modifier.height(60.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 32.dp).height(60.dp).background(brush = Brush.horizontalGradient(listOf(PrimaryGreen, MediumGreen)), shape = RoundedCornerShape(50.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Text("Add Medication", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun EmptyHistoryContent() {
    Column(
        modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(start = 24.dp, end = 24.dp, bottom = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.MedicalServices, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("No History Yet", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextPrimary, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Your medication history will appear here once you start tracking", fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = TextSecondary, textAlign = TextAlign.Center)
    }
}
