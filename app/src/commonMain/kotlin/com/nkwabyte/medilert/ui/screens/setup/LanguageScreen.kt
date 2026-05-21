package com.nkwabyte.medilert.ui.screens.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.navigation.*
import com.nkwabyte.medilert.ui.components.TopBarStripe
import com.nkwabyte.medilert.ui.theme.*
import com.nkwabyte.medilert.viewmodel.AppViewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel

private val languages = listOf(
    Triple("en", "English", "English"),
    Triple("tw", "Akan / Twi", "Twi"),
    Triple("ga", "Ga", "Ga"),
    Triple("ee", "Ewe", "Ewe")
)

@Composable
fun LanguageScreen(
    navViewModel: NavViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel()
) {
    var selectedLang by remember { mutableStateOf("en") }
    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        TopBarStripe(modifier = Modifier.align(Alignment.TopCenter))
        Column(modifier = Modifier.fillMaxSize().padding(top = 6.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 46.dp, bottom = 16.dp)) {
                Box(
                    modifier = Modifier.size(40.dp).background(Surface, CircleShape).border(1.dp, BorderLight, CircleShape).clickable { navViewModel.popBack() },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = TextPrimary) }
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 24.dp)) {
                Text("Select Language", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Choose your preferred language", fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(32.dp))
                languages.forEach { (code, name, native) ->
                    LanguageOption(name = name, native = native, isSelected = selectedLang == code) {
                        selectedLang = code
                        appViewModel.setLanguage(name)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            Box(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp).fillMaxWidth()) {
                Button(
                    onClick = { navViewModel.navigateTo(VoiceAccessibility) },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaYellow),
                ) { Text("Continue", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = TextPrimary) }
            }
        }
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp).width(140.dp).height(5.dp).background(Divider, RoundedCornerShape(50.dp)))
    }
}

@Composable
fun LanguageOption(name: String, native: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Surface, RoundedCornerShape(20.dp))
            .border(2.dp, if (isSelected) PrimaryGreen else BorderLight, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(name, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary)
            Text(native, fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = TextSecondary)
        }
        if (isSelected) {
            Box(
                modifier = Modifier.size(28.dp).background(PrimaryGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)) }
        }
    }
}
