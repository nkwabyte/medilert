package com.nkwabyte.medilert.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.model.UserRole
import com.nkwabyte.medilert.navigation.*
import com.nkwabyte.medilert.ui.theme.*
import com.nkwabyte.medilert.viewmodel.AppViewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel
import com.nkwabyte.medilert.viewmodel.SaveState
import com.nkwabyte.medilert.viewmodel.SignupViewModel

@Composable
fun UserProfileCompleteScreen(
    navViewModel: NavViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel(),
    signupViewModel: SignupViewModel = viewModel()
) {
    val userRole by appViewModel.userRole.collectAsState()
    val saveState by signupViewModel.saveState.collectAsState()

    // Save user profile when screen is first displayed
    LaunchedEffect(Unit) {
        signupViewModel.saveUserProfile()
    }

    val isCaregiver = userRole != UserRole.PATIENT

    // Navigate to dashboard on successful save
    LaunchedEffect(saveState) {
        if (saveState is SaveState.Success) {
            val dest: AppDestination = if (isCaregiver) CareGiverDashboard else Dashboard
            navViewModel.navigateAndClearStack(dest)
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Background)) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            PrimaryGreen.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(PrimaryGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "You're All Set!",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Your ${if (isCaregiver) "caregiver" else "patient"} profile has been created. Let's get started!",
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Feature highlights
            val features = if (isCaregiver) listOf(
                "Monitor patient medication adherence",
                "Get notified when a dose is missed",
                "Manage patient medication plans"
            ) else listOf(
                "Track your medications daily",
                "Get timely reminders",
                "View adherence history"
            )
            features.forEach { feature ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier
                        .size(8.dp)
                        .background(PrimaryGreen, CircleShape))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        feature,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Show error if save failed
            if (saveState is SaveState.Error) {
                Text(
                    (saveState as SaveState.Error).message,
                    color = GhanaRed,
                    fontFamily = Poppins,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    if (saveState is SaveState.Error) {
                        signupViewModel.saveUserProfile()
                    } else {
                        val dest: AppDestination = if (isCaregiver) CareGiverDashboard else Dashboard
                        navViewModel.navigateAndClearStack(dest)
                    }
                },
                enabled = saveState !is SaveState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(50.dp),
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
                            ), shape = RoundedCornerShape(50.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (saveState is SaveState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            if (saveState is SaveState.Error) "Retry" else "Go to Dashboard",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
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
