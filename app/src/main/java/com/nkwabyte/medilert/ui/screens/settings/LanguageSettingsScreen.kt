package com.nkwabyte.medilert.ui.screens.settings

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.ui.screens.setup.LanguageOption
import com.nkwabyte.medilert.ui.theme.*
import com.nkwabyte.medilert.viewmodel.AppViewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel

private val languages = listOf(Triple("en", "English", "English"), Triple("tw", "Akan / Twi", "Twi"), Triple("ga", "Ga", "Ga"), Triple("ee", "Ewe", "Ewe"))

@Composable
fun LanguageSettingsScreen(
    navViewModel: NavViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel()
) {
    val currentLang by appViewModel.selectedLanguage.collectAsState()
    var selectedLang by remember { mutableStateOf(languages.firstOrNull { it.second == currentLang }?.first ?: "en") }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(brush = Brush.verticalGradient(listOf(PrimaryGreen.copy(alpha = 0.05f), Color.Transparent))))

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 52.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).background(Surface, CircleShape).border(1.dp, BorderLight, CircleShape).clickable { navViewModel.popBack() },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = TextPrimary) }
                Text("Language", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                Spacer(modifier = Modifier.size(40.dp))
            }

            Column(modifier = Modifier.weight(1f).padding(horizontal = 24.dp).padding(top = 16.dp)) {
                Text("Select your language", fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(24.dp))
                languages.forEach { (code, name, native) ->
                    LanguageOption(name = name, native = native, isSelected = selectedLang == code) { selectedLang = code }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Box(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp).fillMaxWidth()) {
                Button(
                    onClick = {
                        val langName = languages.firstOrNull { it.first == selectedLang }?.second ?: "English"
                        appViewModel.setLanguage(langName)
                        navViewModel.popBack()
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(brush = Brush.horizontalGradient(listOf(PrimaryGreen, MediumGreen)), shape = RoundedCornerShape(50.dp)),
                        contentAlignment = Alignment.Center
                    ) { Text("Save", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White) }
                }
            }
        }
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp).width(140.dp).height(5.dp).background(Divider, RoundedCornerShape(50.dp)))
    }
}
