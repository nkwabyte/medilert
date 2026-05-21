package com.nkwabyte.medilert.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nkwabyte.medilert.ui.theme.Poppins
import com.nkwabyte.medilert.ui.theme.Surface
import com.nkwabyte.medilert.ui.theme.TabActive
import com.nkwabyte.medilert.ui.theme.TabInactive

enum class DashboardTab { HOME, HISTORY, SETTINGS }

@Composable
fun BottomTabBar(
    activeTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(Surface, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabItem(icon = Icons.Default.Home, label = "Home", isActive = activeTab == DashboardTab.HOME) { onTabSelected(DashboardTab.HOME) }
            TabItem(icon = Icons.Default.History, label = "History", isActive = activeTab == DashboardTab.HISTORY) { onTabSelected(DashboardTab.HISTORY) }
            TabItem(icon = Icons.Default.Settings, label = "Settings", isActive = activeTab == DashboardTab.SETTINGS) { onTabSelected(DashboardTab.SETTINGS) }
        }
    }
}

@Composable
private fun TabItem(icon: ImageVector, label: String, isActive: Boolean, onClick: () -> Unit) {
    val color = if (isActive) TabActive else TabInactive
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(26.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontFamily = Poppins, fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium, fontSize = 12.sp, color = color)
    }
}
