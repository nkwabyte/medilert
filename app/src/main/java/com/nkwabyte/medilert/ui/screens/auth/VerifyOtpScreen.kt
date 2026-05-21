package com.nkwabyte.medilert.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.navigation.AppDestination
import com.nkwabyte.medilert.navigation.Language
import com.nkwabyte.medilert.navigation.NewPassword
import com.nkwabyte.medilert.navigation.ResetPin
import com.nkwabyte.medilert.ui.theme.Background
import com.nkwabyte.medilert.ui.theme.BorderLight
import com.nkwabyte.medilert.ui.theme.BorderMedium
import com.nkwabyte.medilert.ui.theme.Divider
import com.nkwabyte.medilert.ui.theme.GhanaRed
import com.nkwabyte.medilert.ui.theme.MediumGreen
import com.nkwabyte.medilert.ui.theme.Poppins
import com.nkwabyte.medilert.ui.theme.PrimaryGreen
import com.nkwabyte.medilert.ui.theme.Surface
import com.nkwabyte.medilert.ui.theme.TextPrimary
import com.nkwabyte.medilert.ui.theme.TextSecondary
import com.nkwabyte.medilert.viewmodel.AuthViewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel

@Composable
fun VerifyOtpScreen(
    navViewModel: NavViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    source: String = "signup"
) {
    val uiState by authViewModel.uiState.collectAsState()
    var otpValue by remember { mutableStateOf("") }

    val destination: AppDestination = when (source) {
        "forgot" -> NewPassword
        "pin" -> ResetPin
        else -> Language
    }

    // Navigate on successful verification
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess && source == "signup") {
            navViewModel.navigateTo(destination)
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Background)) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = 80.dp, y = (-60).dp)
                .background(
                    brush = Brush.radialGradient(
                        listOf(
                            PrimaryGreen.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    ), shape = CircleShape
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 52.dp, bottom = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Surface, CircleShape)
                        .border(1.dp, BorderLight, CircleShape)
                        .clickable { navViewModel.popBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Verify Code",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Enter the 6-digit code we sent to your phone",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // OTP Input boxes (changed to 6 digits for SMS codes)
                OtpInputRow(
                    otpValue = otpValue,
                    onValueChange = { if (it.length <= 6) otpValue = it })

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Didn't receive a code? ",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Text(
                        "Resend",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PrimaryGreen,
                        modifier = Modifier
                            .clickable { }
                            .padding(vertical = 8.dp, horizontal = 8.dp)
                    )
                }

                // Show error messages
                uiState.errorMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        msg,
                        color = GhanaRed,
                        fontFamily = Poppins,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = {
                        if (source == "signup" && uiState.verificationId != null) {
                            authViewModel.verifyPhoneCode(uiState.verificationId!!, otpValue)
                        } else {
                            // For other sources (forgot password, etc.), navigate directly
                            navViewModel.navigateTo(destination)
                        }
                    },
                    enabled = !uiState.isLoading && otpValue.length == 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        PrimaryGreen,
                                        MediumGreen
                                    )
                                ), shape = RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Verify",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
                .width(140.dp)
                .height(5.dp)
                .background(Divider, RoundedCornerShape(50.dp))
        )
    }
}

@Composable
fun OtpInputRow(otpValue: String, onValueChange: (String) -> Unit, digitCount: Int = 6) {
    BasicTextField(
        value = otpValue,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(digitCount) { index ->
                    val char = otpValue.getOrNull(index)?.toString() ?: ""
                    val isFocused = index == otpValue.length
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(Surface, RoundedCornerShape(12.dp))
                            .border(
                                2.dp,
                                if (isFocused) PrimaryGreen else if (char.isNotEmpty()) PrimaryGreen.copy(
                                    alpha = 0.5f
                                ) else BorderMedium,
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            char,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    )
}

