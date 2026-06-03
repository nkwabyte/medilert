package com.nkwabyte.medilert.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.data.service.UserService
import com.nkwabyte.medilert.generated.resources.Res
import com.nkwabyte.medilert.generated.resources.ic_google
import com.nkwabyte.medilert.generated.resources.img_auth_login
import com.nkwabyte.medilert.generated.resources.logo
import com.nkwabyte.medilert.model.UserRole
import com.nkwabyte.medilert.navigation.CareGiverDashboard
import com.nkwabyte.medilert.navigation.Dashboard
import com.nkwabyte.medilert.navigation.ForgotPassword
import com.nkwabyte.medilert.navigation.SignUp
import com.nkwabyte.medilert.ui.components.AuthScreenShell
import com.nkwabyte.medilert.ui.theme.BorderLight
import com.nkwabyte.medilert.ui.theme.BorderMedium
import com.nkwabyte.medilert.ui.theme.DarkGreen
import com.nkwabyte.medilert.ui.theme.GhanaRed
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
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

private fun friendlyError(raw: String): String = when {
    raw.contains("no user record", ignoreCase = true) ||
    raw.contains("user-not-found", ignoreCase = true) ||
    raw.contains("EMAIL_NOT_FOUND", ignoreCase = true) ->
        "No account found with this email address."
    raw.contains("password is invalid", ignoreCase = true) ||
    raw.contains("INVALID_PASSWORD", ignoreCase = true) ||
    raw.contains("wrong-password", ignoreCase = true) ->
        "Incorrect password. Please try again."
    raw.contains("badly formatted", ignoreCase = true) ||
    raw.contains("invalid-email", ignoreCase = true) ->
        "Please enter a valid email address."
    raw.contains("too-many-requests", ignoreCase = true) ||
    raw.contains("too many", ignoreCase = true) ->
        "Too many failed attempts. Please wait a moment and try again."
    raw.contains("network", ignoreCase = true) ||
    raw.contains("NETWORK_ERROR", ignoreCase = true) ->
        "Network error. Please check your connection."
    raw.contains("user-disabled", ignoreCase = true) ->
        "This account has been disabled. Contact support."
    raw.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
    raw.contains("invalid-credential", ignoreCase = true) ->
        "Incorrect email or password. Please try again."
    else -> raw.take(120)
}

@Composable
fun LoginScreen(
    navViewModel: NavViewModel = viewModel { NavViewModel() },
    appViewModel: AppViewModel = viewModel { AppViewModel() },
    authViewModel: AuthViewModel = viewModel { AuthViewModel() },
    userService: UserService = UserService()
) {
    val uiState by authViewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var passwordHasError by remember { mutableStateOf(false) }
    var emailHasError by remember { mutableStateOf(false) }

    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(uiState.errorMessage) {
        val raw = uiState.errorMessage ?: return@LaunchedEffect
        val msg = friendlyError(raw)
        errorMessage = msg
        successMessage = null
        passwordHasError = msg.contains("password", ignoreCase = true) ||
                           msg.contains("Incorrect", ignoreCase = true) ||
                           msg.contains("credential", ignoreCase = true)
        emailHasError = msg.contains("email", ignoreCase = true) &&
                        !msg.contains("password", ignoreCase = true)
        repeat(4) {
            shakeOffset.animateTo(if (it % 2 == 0) 10f else -10f, tween(60))
        }
        shakeOffset.animateTo(0f, tween(60))
        authViewModel.clearError()
    }

    LaunchedEffect(email) {
        if (emailHasError) { errorMessage = null; emailHasError = false }
    }
    LaunchedEffect(password) {
        if (passwordHasError || errorMessage != null) {
            errorMessage = null; passwordHasError = false
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (!uiState.isSuccess) return@LaunchedEffect
        successMessage = "Login successful! Welcome back."
        errorMessage = null
        delay(800)
        when (val result = userService.getProfile()) {
            is FirebaseResult.Success -> {
                val role = result.data.role
                appViewModel.setUserRole(role)
                navViewModel.navigateAndClearStack(
                    if (role == UserRole.PATIENT) Dashboard else CareGiverDashboard
                )
            }
            else -> navViewModel.navigateAndClearStack(Dashboard)
        }
    }

    AuthScreenShell(
        imageRes = Res.drawable.img_auth_login,
        onBack = { navViewModel.popBack() },
        formModifier = Modifier.offset(x = shakeOffset.value.dp)
    ) {
        // Logo
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(Surface, RoundedCornerShape(28.dp))
                .border(1.dp, BorderLight, RoundedCornerShape(28.dp))
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(52.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Welcome Back",
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = TextPrimary
        )
        Text(
            "Sign in to continue",
            fontFamily = Poppins,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Email field
        AuthInputField(
            label = "Email",
            value = email,
            onValueChange = { email = it },
            placeholder = "Enter your email",
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null,
                    tint = if (emailHasError) GhanaRed else TextSecondary)
            },
            keyboardType = KeyboardType.Email,
            isError = emailHasError
        )
        AnimatedVisibility(
            visible = emailHasError && errorMessage != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                errorMessage ?: "",
                fontFamily = Poppins, fontWeight = FontWeight.Medium,
                fontSize = 12.sp, color = GhanaRed,
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Password field
        AuthInputField(
            label = "Password",
            value = password,
            onValueChange = { password = it },
            placeholder = "Enter password",
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null,
                    tint = if (passwordHasError) GhanaRed else TextSecondary)
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password,
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = if (passwordHasError) GhanaRed else TextSecondary
                    )
                }
            },
            isError = passwordHasError
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Remember me + Forgot password
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
                        .background(if (rememberMe) PrimaryGreen else Color.Transparent, RoundedCornerShape(6.dp))
                        .border(2.dp, if (rememberMe) PrimaryGreen else BorderMedium, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (rememberMe) {
                        Icon(Icons.Default.Check, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Remember Me", fontFamily = Poppins, fontWeight = FontWeight.Medium,
                    fontSize = 13.sp, color = TextSecondary)
            }
            Text(
                "Forgot Password?",
                fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp, color = GhanaRed,
                modifier = Modifier
                    .clickable { navViewModel.navigateTo(ForgotPassword) }
                    .padding(vertical = 4.dp, horizontal = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Error banner
        AnimatedVisibility(
            visible = errorMessage != null && !emailHasError,
            enter = fadeIn(tween(250)) + expandVertically(tween(250)),
            exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GhanaRed.copy(alpha = 0.09f), RoundedCornerShape(16.dp))
                    .border(1.dp, GhanaRed.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null,
                        tint = GhanaRed, modifier = Modifier.size(20.dp))
                    Text(errorMessage ?: "", fontFamily = Poppins, fontWeight = FontWeight.Medium,
                        fontSize = 13.sp, color = GhanaRed, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.Close, contentDescription = "Dismiss",
                        tint = GhanaRed.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                            .clickable { errorMessage = null; passwordHasError = false })
                }
            }
        }

        // Success banner
        AnimatedVisibility(
            visible = successMessage != null,
            enter = fadeIn(tween(250)) + expandVertically(tween(250)),
            exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryGreen.copy(alpha = 0.09f), RoundedCornerShape(16.dp))
                    .border(1.dp, PrimaryGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null,
                        tint = DarkGreen, modifier = Modifier.size(20.dp))
                    Text(successMessage ?: "", fontFamily = Poppins, fontWeight = FontWeight.Medium,
                        fontSize = 13.sp, color = DarkGreen)
                }
            }
        }

        Spacer(modifier = Modifier.height(if (errorMessage != null || successMessage != null) 16.dp else 8.dp))

        // Log In button
        Button(
            onClick = {
                when {
                    email.isBlank() -> { errorMessage = "Please enter your email address."; emailHasError = true }
                    !(email.trim().contains('@') && email.trim().contains('.')) -> {
                        errorMessage = "Please enter a valid email address."; emailHasError = true
                    }
                    password.isBlank() -> { errorMessage = "Please enter your password."; passwordHasError = true }
                    password.length < 6 -> { errorMessage = "Password must be at least 6 characters."; passwordHasError = true }
                    else -> {
                        errorMessage = null; emailHasError = false; passwordHasError = false
                        authViewModel.signInWithEmail(email.trim(), password, rememberMe)
                    }
                }
            },
            enabled = !uiState.isLoading && successMessage == null,
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
                when {
                    uiState.isLoading -> CircularProgressIndicator(color = Color.White,
                        modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    successMessage != null -> Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(20.dp))
                        Text("Logged In", fontFamily = Poppins, fontWeight = FontWeight.Bold,
                            fontSize = 18.sp, color = Color.White)
                    }
                    else -> Text("Log In", fontFamily = Poppins, fontWeight = FontWeight.Bold,
                        fontSize = 18.sp, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Divider
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = BorderMedium)
            Text("  Or sign in with  ", fontFamily = Poppins, fontWeight = FontWeight.Medium,
                fontSize = 13.sp, color = TextSecondary)
            HorizontalDivider(modifier = Modifier.weight(1f), color = BorderMedium)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Google Sign-In
        Button(
            onClick = { authViewModel.signInWithGoogle() },
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Surface, contentColor = TextPrimary),
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
                    .border(1.dp, BorderLight, RoundedCornerShape(24.dp))
                    .background(Surface, RoundedCornerShape(24.dp))
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(painter = painterResource(Res.drawable.ic_google),
                    contentDescription = "Google", modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Continue with Google", fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp, color = TextPrimary)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontFamily = Poppins, fontWeight = FontWeight.Medium,
                        fontSize = 15.sp, color = TextSecondary)) { append("Don't have an account?  ") }
                    withStyle(SpanStyle(fontFamily = Poppins, fontWeight = FontWeight.Bold,
                        fontSize = 15.sp, color = PrimaryGreen)) { append("Sign up") }
                },
                modifier = Modifier
                    .clickable { navViewModel.navigateTo(SignUp) }
                    .padding(vertical = 8.dp)
            )
        }
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
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            label,
            fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
            color = if (isError) GhanaRed else TextPrimary,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            placeholder = { Text(placeholder, fontFamily = Poppins, color = TextHint, fontSize = 15.sp) },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            isError = isError,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Surface,
                focusedContainerColor = Surface,
                errorContainerColor = GhanaRed.copy(alpha = 0.05f),
                unfocusedBorderColor = BorderLight,
                focusedBorderColor = PrimaryGreen,
                errorBorderColor = GhanaRed,
                errorCursorColor = GhanaRed,
                unfocusedTextColor = TextPrimary,
                focusedTextColor = TextPrimary,
                errorTextColor = TextPrimary
            ),
            textStyle = LocalTextStyle.current.copy(
                fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 15.sp
            )
        )
    }
}
