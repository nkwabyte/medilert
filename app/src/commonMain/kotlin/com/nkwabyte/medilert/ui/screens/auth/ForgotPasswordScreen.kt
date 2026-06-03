package com.nkwabyte.medilert.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.generated.resources.Res
import com.nkwabyte.medilert.generated.resources.img_auth_forgot
import com.nkwabyte.medilert.ui.components.AuthScreenShell
import com.nkwabyte.medilert.ui.theme.DarkGreen
import com.nkwabyte.medilert.ui.theme.GhanaRed
import com.nkwabyte.medilert.ui.theme.MediumGreen
import com.nkwabyte.medilert.ui.theme.Poppins
import com.nkwabyte.medilert.ui.theme.PrimaryGreen
import com.nkwabyte.medilert.ui.theme.TextPrimary
import com.nkwabyte.medilert.ui.theme.TextSecondary
import com.nkwabyte.medilert.viewmodel.AuthViewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel

@Composable
fun ForgotPasswordScreen(navViewModel: NavViewModel = viewModel { NavViewModel() }) {
    var email by remember { mutableStateOf("") }
    val authViewModel: AuthViewModel = viewModel { AuthViewModel() }
    val uiState by authViewModel.uiState.collectAsState()
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            successMessage = "Reset link sent to $email. Check your inbox."
            authViewModel.clearError()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            errorMessage = it
            authViewModel.clearError()
        }
    }

    AuthScreenShell(
        imageRes = Res.drawable.img_auth_forgot,
        imageHeight = 280.dp,
        onBack = { navViewModel.popBack() }
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Forgot Password?",
            fontFamily = Poppins, fontWeight = FontWeight.Bold,
            fontSize = 28.sp, color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Enter your email and we'll send you a password reset link",
            fontFamily = Poppins, fontWeight = FontWeight.Normal,
            fontSize = 15.sp, color = TextSecondary, lineHeight = 23.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        AuthInputField(
            label = "Email",
            value = email,
            onValueChange = { email = it; successMessage = null; errorMessage = null },
            placeholder = "Enter your email",
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        successMessage?.let {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(PrimaryGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .border(1.dp, PrimaryGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(it, fontFamily = Poppins, fontWeight = FontWeight.Medium,
                    fontSize = 13.sp, color = DarkGreen)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        errorMessage?.let {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(GhanaRed.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .border(1.dp, GhanaRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(it, fontFamily = Poppins, fontWeight = FontWeight.Medium,
                    fontSize = 13.sp, color = GhanaRed)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = {
                if (email.isNotBlank()) {
                    successMessage = null; errorMessage = null
                    authViewModel.sendPasswordResetEmail(email.trim())
                } else {
                    errorMessage = "Please enter your email address"
                }
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(Brush.horizontalGradient(listOf(PrimaryGreen, MediumGreen)), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                } else {
                    Text("Send Reset Link", fontFamily = Poppins, fontWeight = FontWeight.Bold,
                        fontSize = 18.sp, color = Color.White)
                }
            }
        }
    }
}
