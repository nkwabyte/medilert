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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.navigation.Login
import com.nkwabyte.medilert.navigation.PersonalInfo
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
import com.nkwabyte.medilert.ui.theme.TextPrimary
import com.nkwabyte.medilert.ui.theme.TextSecondary
import com.nkwabyte.medilert.util.PhoneUtils
import com.nkwabyte.medilert.viewmodel.AuthViewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel
import com.nkwabyte.medilert.viewmodel.SignupViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import com.nkwabyte.medilert.generated.resources.Res
import com.nkwabyte.medilert.generated.resources.ic_google
import com.nkwabyte.medilert.generated.resources.logo

@Composable
fun SignUpScreen(
    navViewModel: NavViewModel = viewModel { NavViewModel() },
    authViewModel: AuthViewModel = viewModel { AuthViewModel() },
    signupViewModel: SignupViewModel = viewModel { SignupViewModel() }
) {
    val uiState by authViewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val handleRegister: () -> Unit = handler@{
        val phoneValidationError = PhoneUtils.validateGhanaPhoneNumber(phoneNumber)
        if (phoneValidationError != null) {
            scope.launch {
                snackBarHostState.showSnackbar(
                    message = phoneValidationError,
                    duration = SnackbarDuration.Short
                )
            }
            return@handler
        }

        if (!PhoneUtils.isEmail(email.trim())) {
            scope.launch {
                snackBarHostState.showSnackbar(
                    message = "Please enter a valid email address",
                    duration = SnackbarDuration.Short
                )
            }
            return@handler
        }

        authViewModel.registerWithEmail(email.trim(), password)
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            val formattedPhone = PhoneUtils.formatGhanaPhoneNumber(phoneNumber) ?: phoneNumber

            signupViewModel.setBasicInfo(fullName, email.trim())
            signupViewModel.setPhone(formattedPhone)

            if (uiState.incompleteRegistration) {
                snackBarHostState.showSnackbar(
                    message = "Welcome back! Let's complete your account setup.",
                    duration = SnackbarDuration.Short
                )
            }

            navViewModel.navigateTo(PersonalInfo)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { errorMsg ->
            snackBarHostState.showSnackbar(
                message = errorMsg,
                duration = SnackbarDuration.Long
            )
            authViewModel.clearError()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState) { data ->
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
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(Background)) {
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
                        ), shape = CircleShape
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
                        ), shape = CircleShape
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
                        "Create Account",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    AuthInputField(
                        label = "Full Name", value = fullName, onValueChange = { fullName = it },
                        placeholder = "Enter your full name",
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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
                        label = "Phone Number",
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        placeholder = "0XXXXXXXXX or 9 digits",
                        leadingIcon = {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        },
                        keyboardType = KeyboardType.Phone
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AuthInputField(
                        label = "Password", value = password, onValueChange = { password = it },
                        placeholder = "Create password",
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
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AuthInputField(
                        label = "Repeat Password",
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = "Confirm password",
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardType = KeyboardType.Password,
                        trailingIcon = {
                            IconButton(onClick = {
                                confirmPasswordVisible = !confirmPasswordVisible
                            }) {
                                Icon(
                                    if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = handleRegister,
                        enabled = !uiState.isLoading && fullName.isNotBlank() && email.isNotBlank()
                                && phoneNumber.isNotBlank() && password.isNotBlank() && password == confirmPassword,
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
                                    "Get Started",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderMedium)
                        Text(
                            "  Or sign up with  ",
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
                            Image(
                                painter = painterResource(Res.drawable.ic_google),
                                contentDescription = "Google",
                                modifier = Modifier.size(24.dp)
                            )
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

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Already have an account? ",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = TextSecondary
                        )
                        Text(
                            "Sign In",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PrimaryGreen,
                            modifier = Modifier
                                .clickable { navViewModel.navigateTo(Login) }
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
}
