package com.nkwabyte.medilert.ui.screens.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.generated.resources.Res
import com.nkwabyte.medilert.generated.resources.img_auth_forgot
import com.nkwabyte.medilert.navigation.Login
import com.nkwabyte.medilert.ui.components.AuthScreenShell
import com.nkwabyte.medilert.ui.theme.MediumGreen
import com.nkwabyte.medilert.ui.theme.Poppins
import com.nkwabyte.medilert.ui.theme.PrimaryGreen
import com.nkwabyte.medilert.ui.theme.TextPrimary
import com.nkwabyte.medilert.ui.theme.TextSecondary
import com.nkwabyte.medilert.viewmodel.NavViewModel
import androidx.compose.foundation.background

@Composable
fun NewPasswordScreen(navViewModel: NavViewModel = viewModel { NavViewModel() }) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    AuthScreenShell(
        imageRes = Res.drawable.img_auth_forgot,
        imageHeight = 280.dp,
        onBack = { navViewModel.popBack() }
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Set New Password",
            fontFamily = Poppins, fontWeight = FontWeight.Bold,
            fontSize = 28.sp, color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Your new password must be different from previous passwords",
            fontFamily = Poppins, fontWeight = FontWeight.Normal,
            fontSize = 14.sp, color = TextSecondary, lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        AuthInputField(
            label = "New Password", value = newPassword, onValueChange = { newPassword = it },
            placeholder = "Enter new password",
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary) },
            visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password,
            trailingIcon = {
                IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                    Icon(if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null, tint = TextSecondary)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthInputField(
            label = "Confirm Password", value = confirmPassword, onValueChange = { confirmPassword = it },
            placeholder = "Confirm new password",
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary) },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password,
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null, tint = TextSecondary)
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navViewModel.navigateAndClearStack(Login) },
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
                Text("Reset Password", fontFamily = Poppins, fontWeight = FontWeight.Bold,
                    fontSize = 18.sp, color = Color.White)
            }
        }
    }
}
