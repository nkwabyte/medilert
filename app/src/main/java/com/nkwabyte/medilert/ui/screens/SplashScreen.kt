package com.nkwabyte.medilert.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.nkwabyte.medilert.R
import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.data.PreferencesManager
import com.nkwabyte.medilert.data.service.UserService
import com.nkwabyte.medilert.model.UserRole
import com.nkwabyte.medilert.navigation.CareGiverDashboard
import com.nkwabyte.medilert.navigation.Dashboard
import com.nkwabyte.medilert.navigation.Login
import com.nkwabyte.medilert.navigation.Onboarding1
import com.nkwabyte.medilert.ui.theme.GhanaRed
import com.nkwabyte.medilert.ui.theme.GhanaYellow
import com.nkwabyte.medilert.ui.theme.Poppins
import com.nkwabyte.medilert.ui.theme.PrimaryGreen
import com.nkwabyte.medilert.viewmodel.NavViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navViewModel: NavViewModel = viewModel(),
    userService: UserService = UserService()
) {
    val context = LocalContext.current
    val prefsManager = remember { PreferencesManager.getInstance(context) }

    // Animation states
    val logoAlpha = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }

    // Auth state
    var authChecked by remember { mutableStateOf(false) }
    var showButtons by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Fade in logo first
        logoAlpha.animateTo(1f, animationSpec = tween(700))
        delay(300)
        // Fade in bottom content
        contentAlpha.animateTo(1f, animationSpec = tween(600))
        delay(400)

        // Auth check
        val currentUser = FirebaseAuth.getInstance().currentUser
        val isSessionExpired = prefsManager.isSessionExpired()

        when {
            currentUser == null -> {
                authChecked = true
                showButtons = true
            }
            isSessionExpired -> {
                FirebaseAuth.getInstance().signOut()
                prefsManager.clearSession()
                authChecked = true
                showButtons = true
            }
            else -> {
                prefsManager.updateLastActivityTime()
                when (val result = userService.getProfile()) {
                    is FirebaseResult.Success -> {
                        val destination = when (result.data.role) {
                            UserRole.PATIENT -> Dashboard
                            UserRole.DOCTOR, UserRole.PHARMACIST, UserRole.GUARDIAN -> CareGiverDashboard
                        }
                        navViewModel.navigateAndClearStack(destination)
                    }
                    else -> {
                        authChecked = true
                        showButtons = true
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Full-screen background photo
        Image(
            painter = painterResource(id = R.drawable.img_splash),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark gradient — light tint at top, near-opaque at bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0xFF071407).copy(alpha = 0.25f),
                        0.38f to Color(0xFF071407).copy(alpha = 0.55f),
                        0.65f to Color(0xFF071407).copy(alpha = 0.82f),
                        1.0f to Color(0xFF071407).copy(alpha = 0.97f)
                    )
                )
        )

        // ── Top: logo + app name ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 40.dp)
                .alpha(logoAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Medilert",
                modifier = Modifier.size(88.dp),
                contentScale = ContentScale.Fit
            )
        }

        // ── Ghana flag dots (centered) ────────────────────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 120.dp)
                .alpha(logoAlpha.value),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.size(8.dp).background(GhanaRed, CircleShape))
            Box(modifier = Modifier.size(8.dp).background(GhanaYellow, CircleShape))
            Box(modifier = Modifier.size(8.dp).background(PrimaryGreen, CircleShape))
        }

        // ── Bottom: tagline + CTA buttons ────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 28.dp)
                .padding(bottom = 40.dp)
                .alpha(contentAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Managing your health,\none reminder at a time.",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Stay on track with your medications and\nnever miss a dose again.",
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                lineHeight = 21.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            if (!authChecked) {
                // Show loading while checking auth
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                AnimatedVisibility(
                    visible = showButtons,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Get Started button — white filled
                        Button(
                            onClick = { navViewModel.navigateAndClearStack(Onboarding1) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                "GET STARTED",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0D3320),
                                letterSpacing = 1.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Log In link
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                navViewModel.navigateAndClearStack(Login)
                            }
                        ) {
                            Text(
                                buildAnnotatedString {
                                    withStyle(SpanStyle(color = Color.White.copy(alpha = 0.8f), fontFamily = Poppins, fontWeight = FontWeight.Normal, fontSize = 15.sp)) {
                                        append("Already have an account?  ")
                                    }
                                    withStyle(SpanStyle(color = Color.White, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 15.sp)) {
                                        append("Log In")
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Privacy note
                        Text(
                            "By proceeding, you agree to our Terms and Privacy Policy.",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.45f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
