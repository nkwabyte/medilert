package com.nkwabyte.medilert.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.data.PreferencesManager
import com.nkwabyte.medilert.data.service.UserService
import com.nkwabyte.medilert.model.UserRole
import com.nkwabyte.medilert.navigation.CareGiverDashboard
import com.nkwabyte.medilert.navigation.Dashboard
import com.nkwabyte.medilert.navigation.Login
import com.nkwabyte.medilert.navigation.Onboarding1
import com.nkwabyte.medilert.viewmodel.NavViewModel
import com.nkwabyte.medilert.ui.components.TopBarStripe
import com.nkwabyte.medilert.ui.theme.Background
import com.nkwabyte.medilert.ui.theme.Divider
import com.nkwabyte.medilert.ui.theme.GhanaRed
import com.nkwabyte.medilert.ui.theme.GhanaYellow
import com.nkwabyte.medilert.ui.theme.PrimaryGreen
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import com.nkwabyte.medilert.generated.resources.Res
import com.nkwabyte.medilert.generated.resources.logo
import androidx.compose.foundation.Image

@Composable
fun SplashScreen(
    navViewModel: NavViewModel = viewModel { NavViewModel() },
    userService: UserService = UserService()
) {
    val prefsManager = remember { PreferencesManager.instance }
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.85f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(600))
        scale.animateTo(1f, animationSpec = tween(600))
        delay(2000)

        val currentUser = Firebase.auth.currentUser
        val hasCompletedOnboarding = prefsManager.hasCompletedOnboarding()
        val isSessionExpired = prefsManager.isSessionExpired()

        when {
            currentUser == null -> {
                if (hasCompletedOnboarding) {
                    navViewModel.navigateAndClearStack(Login)
                } else {
                    navViewModel.navigateAndClearStack(Onboarding1)
                }
            }
            isSessionExpired -> {
                Firebase.auth.signOut()
                prefsManager.clearSession()
                navViewModel.navigateAndClearStack(Login)
            }
            else -> {
                prefsManager.updateLastActivityTime()

                when (val result = userService.getProfile()) {
                    is FirebaseResult.Success -> {
                        val user = result.data
                        val destination = when (user.role) {
                            UserRole.PATIENT -> Dashboard
                            UserRole.DOCTOR, UserRole.PHARMACIST, UserRole.GUARDIAN -> CareGiverDashboard
                        }
                        navViewModel.navigateAndClearStack(destination)
                    }
                    is FirebaseResult.Error -> {
                        navViewModel.navigateAndClearStack(Login)
                    }
                    else -> {
                        navViewModel.navigateAndClearStack(Login)
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopBarStripe(modifier = Modifier.align(Alignment.TopCenter))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha.value)
                .scale(scale.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.logo),
                contentDescription = "Medilert Logo",
                modifier = Modifier.size(144.dp),
                contentScale = ContentScale.Fit
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.size(10.dp).background(GhanaRed, CircleShape))
            Box(modifier = Modifier.size(10.dp).background(GhanaYellow, CircleShape))
            Box(modifier = Modifier.size(10.dp).background(PrimaryGreen, CircleShape))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(5.dp)
                    .background(Divider, RoundedCornerShape(50.dp))
            )
        }
    }
}
