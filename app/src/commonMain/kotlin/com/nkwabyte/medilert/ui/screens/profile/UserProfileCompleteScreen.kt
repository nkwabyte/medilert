package com.nkwabyte.medilert.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.nkwabyte.medilert.generated.resources.img_auth_signup
import com.nkwabyte.medilert.model.UserRole
import com.nkwabyte.medilert.navigation.AppDestination
import com.nkwabyte.medilert.navigation.CareGiverDashboard
import com.nkwabyte.medilert.navigation.Dashboard
import com.nkwabyte.medilert.ui.components.AuthScreenShell
import com.nkwabyte.medilert.ui.theme.GhanaRed
import com.nkwabyte.medilert.ui.theme.MediumGreen
import com.nkwabyte.medilert.ui.theme.Poppins
import com.nkwabyte.medilert.ui.theme.PrimaryGreen
import com.nkwabyte.medilert.ui.theme.TextPrimary
import com.nkwabyte.medilert.ui.theme.TextSecondary
import com.nkwabyte.medilert.viewmodel.AppViewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel
import com.nkwabyte.medilert.viewmodel.SaveState
import com.nkwabyte.medilert.viewmodel.SignupViewModel

@Composable
fun UserProfileCompleteScreen(
    navViewModel: NavViewModel = viewModel { NavViewModel() },
    appViewModel: AppViewModel = viewModel { AppViewModel() },
    signupViewModel: SignupViewModel = viewModel { SignupViewModel() }
) {
    val userRole by appViewModel.userRole.collectAsState()
    val saveState by signupViewModel.saveState.collectAsState()
    val isCaregiver = userRole != UserRole.PATIENT

    LaunchedEffect(Unit) { signupViewModel.saveUserProfile() }

    LaunchedEffect(saveState) {
        if (saveState is SaveState.Success) {
            val dest: AppDestination = if (isCaregiver) CareGiverDashboard else Dashboard
            navViewModel.navigateAndClearStack(dest)
        }
    }

    // Reuse the signup image — celebration feels right here
    AuthScreenShell(
        imageRes = Res.drawable.img_auth_signup,
        imageHeight = 240.dp
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Success icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(PrimaryGreen, CircleShape)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null,
                tint = Color.White, modifier = Modifier.size(46.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "You're All Set!",
            fontFamily = Poppins, fontWeight = FontWeight.Bold,
            fontSize = 30.sp, color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            "Your ${if (isCaregiver) "caregiver" else "patient"} profile has been created. Let's get started!",
            fontFamily = Poppins, fontWeight = FontWeight.Normal,
            fontSize = 15.sp, color = TextSecondary,
            textAlign = TextAlign.Center, lineHeight = 23.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(36.dp))

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
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).background(PrimaryGreen, CircleShape))
                Spacer(modifier = Modifier.width(12.dp))
                Text(feature, fontFamily = Poppins, fontWeight = FontWeight.Medium,
                    fontSize = 15.sp, color = TextPrimary)
            }
        }

        if (saveState is SaveState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text((saveState as SaveState.Error).message, color = GhanaRed,
                fontFamily = Poppins, fontSize = 14.sp,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(36.dp))

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
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(Brush.horizontalGradient(listOf(PrimaryGreen, MediumGreen)), RoundedCornerShape(50.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (saveState is SaveState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        if (saveState is SaveState.Error) "Retry" else "Go to Dashboard",
                        fontFamily = Poppins, fontWeight = FontWeight.Bold,
                        fontSize = 18.sp, color = Color.White
                    )
                }
            }
        }
    }
}
