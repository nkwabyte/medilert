package com.nkwabyte.medilert.ui.screens.setup

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.generated.resources.Res
import com.nkwabyte.medilert.generated.resources.img_auth_setup
import com.nkwabyte.medilert.navigation.RecoveryOtp
import com.nkwabyte.medilert.ui.components.AuthScreenShell
import com.nkwabyte.medilert.ui.screens.auth.AuthInputField
import com.nkwabyte.medilert.ui.theme.GhanaRed
import com.nkwabyte.medilert.ui.theme.MediumGreen
import com.nkwabyte.medilert.ui.theme.Poppins
import com.nkwabyte.medilert.ui.theme.PrimaryGreen
import com.nkwabyte.medilert.ui.theme.Surface
import com.nkwabyte.medilert.ui.theme.TextPrimary
import com.nkwabyte.medilert.ui.theme.TextSecondary
import com.nkwabyte.medilert.util.PhoneUtils
import com.nkwabyte.medilert.viewmodel.AuthViewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel
import kotlinx.coroutines.launch

@Composable
fun VerifyPhoneScreen(
    navViewModel: NavViewModel = viewModel { NavViewModel() },
    authViewModel: AuthViewModel = viewModel { AuthViewModel() }
) {
    val uiState by authViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var phone by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSuccess, uiState.verificationId) {
        if (uiState.isSuccess && uiState.verificationId != null) {
            navViewModel.navigateTo(RecoveryOtp("signup"))
        }
    }

    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(message = error, duration = SnackbarDuration.Short)
            authViewModel.clearError()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Surface,
                    contentColor = TextPrimary,
                    actionColor = GhanaRed,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    ) { paddingValues ->
        AuthScreenShell(
            imageRes = Res.drawable.img_auth_setup,
            imageHeight = 220.dp,
            onBack = { navViewModel.popBack() },
            formModifier = Modifier.padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text("Verify Phone", fontFamily = Poppins, fontWeight = FontWeight.Bold,
                fontSize = 28.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("We'll send a verification code to your phone number",
                fontFamily = Poppins, fontWeight = FontWeight.Normal,
                fontSize = 15.sp, color = TextSecondary, lineHeight = 23.sp)

            Spacer(modifier = Modifier.height(32.dp))

            AuthInputField(
                label = "Phone Number", value = phone, onValueChange = { phone = it },
                placeholder = "0XXXXXXXXX or 9 digits",
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TextSecondary) },
                keyboardType = KeyboardType.Phone
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val validationError = PhoneUtils.validateGhanaPhoneNumber(phone)
                    if (validationError != null) {
                        scope.launch {
                            snackbarHostState.showSnackbar(message = validationError, duration = SnackbarDuration.Short)
                        }
                    } else {
                        val formattedPhone = PhoneUtils.formatGhanaPhoneNumber(phone)
                        if (formattedPhone != null) {
                            authViewModel.sendPhoneVerificationCode(formattedPhone)
                        }
                    }
                },
                enabled = !uiState.isLoading && phone.isNotBlank(),
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
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Send Code", fontFamily = Poppins, fontWeight = FontWeight.Bold,
                            fontSize = 18.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
