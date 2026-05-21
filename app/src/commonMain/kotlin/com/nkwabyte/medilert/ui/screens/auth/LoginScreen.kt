package com.nkwabyte.medilert.ui.screens.auth

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.data.service.UserService
import com.nkwabyte.medilert.model.UserRole
import com.nkwabyte.medilert.navigation.CareGiverDashboard
import com.nkwabyte.medilert.navigation.Dashboard
import com.nkwabyte.medilert.navigation.ForgotPassword
import com.nkwabyte.medilert.navigation.SignUp
import com.nkwabyte.medilert.ui.theme.Background
import com.nkwabyte.medilert.ui.theme.BorderLight
import com.nkwabyte.medilert.ui.theme.BorderMedium
import com.nkwabyte.medilert.ui.theme.Divider
import com.nkwabyte.medilert.ui.theme.GhanaRed
import com.nkwabyte.medilert.ui.theme.GhanaYellow
import com.nkwabyte.medilert.ui.theme.MediumGreen
import com.nkwabyte.medilert.ui.theme.Poppins
import com.nkwabyte.medilert.ui.theme.PrimaryGreen
import com.nkwabyte.medilert.ui.theme.Surface
import com.nkwabyte.medilert.ui.theme.TextHint
import com.nkwabyte.medilert.ui.theme.TextPrimary
import com.nkwabyte.medilert.ui.theme.TextSecondary
import com.nkwabyte.medilert.viewmodel.AppViewModel
import com.nkwabyte.medilert.viewmodel.AuthViewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel
import org.jetbrains.compose.resources.painterResource
import com.nkwabyte.medilert.generated.resources.Res
import com.nkwabyte.medilert.generated.resources.logo

@Composable
fun LoginScreen(
    navViewModel: NavViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    userService: UserService = UserService()
) {
    val uiState by authViewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = 100.dp, y = (-80).dp)
                .background(
                    brush = Brush.radialGradient(
                        listOf(
                            PrimaryGreen.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = 500.dp)
                .background(
                    brush = Brush.radialGradient(
                        listOf(
                            GhanaYellow.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 52.dp, bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Surface, CircleShape)
                        .border(1.dp, BorderLight, CircleShape)
                        .clickable { navViewModel.popBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = TextPrimary)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Surface, RoundedCornerShape(28.dp))
                        .border(1.dp, BorderLight, RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(60.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Welcome Back",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(32.dp))

                AuthInputField(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Enter your email",
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    },
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthInputField(
                    label = "Password",
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Enter password",
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardType = KeyboardType.Password,
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null, tint = TextSecondary
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { rememberMe = !rememberMe }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(
                                    if (rememberMe) PrimaryGreen else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .border(
                                    2.dp,
                                    if (rememberMe) PrimaryGreen else BorderMedium,
                                    RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (rememberMe) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Remember Me",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = "Forgot Password?",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = GhanaRed,
                        modifier = Modifier
                            .clickable { navViewModel.navigateTo(ForgotPassword) }
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { authViewModel.signInWithEmail(email, password, rememberMe) },
                    enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank(),
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
                                brush = Brush.horizontalGradient(listOf(PrimaryGreen, MediumGreen)),
                                shape = RoundedCornerShape(24.dp)
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
                                "Log In",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                LaunchedEffect(uiState.isSuccess) {
                    if (uiState.isSuccess) {
                        when (val result = userService.getProfile()) {
                            is FirebaseResult.Success -> {
                                val role = result.data.role
                                appViewModel.setUserRole(role)
                                val dest = if (role == UserRole.PATIENT) Dashboard else CareGiverDashboard
                                navViewModel.navigateAndClearStack(dest)
                            }
                            else -> navViewModel.navigateAndClearStack(Dashboard)
                        }
                    }
                }

                uiState.errorMessage?.let { msg ->
                    LaunchedEffect(msg) {
                        authViewModel.clearError()
                    }
                    Text(
                        msg, color = GhanaRed, fontFamily = Poppins, fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = BorderMedium)
                    Text(
                        "  Or sign in with  ",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = BorderMedium)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { authViewModel.signInWithGoogle() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Surface,
                        contentColor = TextPrimary
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.dp, BorderLight, RoundedCornerShape(24.dp))
                            .background(Surface, RoundedCornerShape(24.dp))
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFF4285F4), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "G",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Continue with Google",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Don't have an account? ",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = TextSecondary
                    )
                    Text(
                        "Sign up",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PrimaryGreen,
                        modifier = Modifier
                            .clickable { navViewModel.navigateTo(SignUp) }
                            .padding(vertical = 8.dp, horizontal = 8.dp)
                    )
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
fun AuthInputField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            label,
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = TextPrimary,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            placeholder = {
                Text(
                    placeholder,
                    fontFamily = Poppins,
                    color = TextHint,
                    fontSize = 15.sp
                )
            },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Surface,
                focusedContainerColor = Surface,
                unfocusedBorderColor = BorderLight,
                focusedBorderColor = PrimaryGreen,
                unfocusedTextColor = TextPrimary,
                focusedTextColor = TextPrimary
            ),
            textStyle = LocalTextStyle.current.copy(
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
        )
    }
}
