package com.nkwabyte.medilert.ui.screens.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.navigation.*
import com.nkwabyte.medilert.ui.screens.auth.AuthInputField
import com.nkwabyte.medilert.ui.theme.*
import com.nkwabyte.medilert.viewmodel.NavViewModel

@Composable
fun ForgetPinScreen(navViewModel: NavViewModel = viewModel { NavViewModel() }) {
    var phone by remember { mutableStateOf("") }
    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 52.dp, bottom = 24.dp)) {
                Box(
                    modifier = Modifier.size(40.dp).background(Surface, CircleShape).border(1.dp, BorderLight, CircleShape).clickable { navViewModel.popBack() },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = TextPrimary) }
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(80.dp).background(GhanaRed.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.LockOpen, contentDescription = null, tint = GhanaRed, modifier = Modifier.size(40.dp)) }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Forgot PIN?", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = TextPrimary, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Enter your phone number to receive a recovery code", fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = TextSecondary, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(40.dp))
                AuthInputField(
                    label = "Phone Number", value = phone, onValueChange = { phone = it },
                    placeholder = "Enter registered phone",
                    leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null, tint = TextSecondary) },
                    keyboardType = KeyboardType.Phone
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { navViewModel.navigateTo(RecoveryOtp("pin")) },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(brush = Brush.horizontalGradient(listOf(PrimaryGreen, MediumGreen)), shape = RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) { Text("Send Recovery Code", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White) }
                }
            }
        }
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp).width(140.dp).height(5.dp).background(Divider, RoundedCornerShape(50.dp)))
    }
}
