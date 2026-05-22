package com.nkwabyte.medilert.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.ui.screens.SplashScreen
import com.nkwabyte.medilert.ui.screens.auth.ForgotPasswordScreen
import com.nkwabyte.medilert.ui.screens.auth.LoginScreen
import com.nkwabyte.medilert.ui.screens.auth.NewPasswordScreen
import com.nkwabyte.medilert.ui.screens.auth.SignUpScreen
import com.nkwabyte.medilert.ui.screens.auth.VerifyOtpScreen
import com.nkwabyte.medilert.ui.screens.dashboard.CareGiverDashboardScreen
import com.nkwabyte.medilert.ui.screens.dashboard.CaregiverAddPatientScreen
import com.nkwabyte.medilert.ui.screens.dashboard.DashboardEmptyScreen
import com.nkwabyte.medilert.ui.screens.dashboard.DashboardScreen
import com.nkwabyte.medilert.ui.screens.medication.AddMedicationStep1Screen
import com.nkwabyte.medilert.ui.screens.medication.AddMedicationStep2Screen
import com.nkwabyte.medilert.ui.screens.medication.AddMedicationStep3Screen
import com.nkwabyte.medilert.ui.screens.medication.AddMedicationStep4Screen
import com.nkwabyte.medilert.ui.screens.medication.EditMedicationScreen
import com.nkwabyte.medilert.ui.screens.medication.MedicationHistoryScreen
import com.nkwabyte.medilert.ui.screens.onboarding.OnboardingScreen1
import com.nkwabyte.medilert.ui.screens.onboarding.OnboardingScreen2
import com.nkwabyte.medilert.ui.screens.profile.ProfilePhotoViewScreen
import com.nkwabyte.medilert.ui.screens.profile.ProfileScreen
import com.nkwabyte.medilert.ui.screens.profile.UserProfileCompleteScreen
import com.nkwabyte.medilert.ui.screens.reminder.ReminderScreen
import com.nkwabyte.medilert.ui.screens.settings.AppVersionScreen
import com.nkwabyte.medilert.ui.screens.settings.LanguageSettingsScreen
import com.nkwabyte.medilert.ui.screens.settings.PrivacyPolicyScreen
import com.nkwabyte.medilert.ui.screens.setup.ConfirmPinScreen
import com.nkwabyte.medilert.ui.screens.setup.ForgetPinScreen
import com.nkwabyte.medilert.ui.screens.setup.LanguageScreen
import com.nkwabyte.medilert.ui.screens.setup.PersonalInfoScreen
import com.nkwabyte.medilert.ui.screens.setup.ResetPinScreen
import com.nkwabyte.medilert.ui.screens.setup.SetPinScreen
import com.nkwabyte.medilert.ui.screens.setup.UserRoleScreen
import com.nkwabyte.medilert.ui.screens.setup.VerifyPhoneScreen
import com.nkwabyte.medilert.ui.screens.setup.VoiceAccessibilityScreen
import com.nkwabyte.medilert.viewmodel.NavViewModel

@Composable
fun AppNavigation(navViewModel: NavViewModel = viewModel { NavViewModel() }) {
    val backStack = navViewModel.backStack
    val current = backStack.lastOrNull() ?: Splash

    Crossfade(targetState = current, label = "nav") { destination ->
        when (destination) {
            is Splash -> SplashScreen()
            is Onboarding1 -> OnboardingScreen1()
            is Onboarding2 -> OnboardingScreen2()
            is Login -> LoginScreen()
            is SignUp -> SignUpScreen()
            is ForgotPassword -> ForgotPasswordScreen()
            is RecoveryOtp -> VerifyOtpScreen(source = destination.source)
            is NewPassword -> NewPasswordScreen()
            is VerifyPhone -> VerifyPhoneScreen()
            is Language -> LanguageScreen()
            is VoiceAccessibility -> VoiceAccessibilityScreen()
            is PersonalInfo -> PersonalInfoScreen()
            is SetPin -> SetPinScreen()
            is ConfirmPin -> ConfirmPinScreen()
            is ForgetPin -> ForgetPinScreen()
            is ResetPin -> ResetPinScreen()
            is UserRole -> UserRoleScreen()
            is UserProfileComplete -> UserProfileCompleteScreen()
            is Dashboard -> DashboardScreen()
            is DashboardEmpty -> DashboardEmptyScreen()
            is CareGiverDashboard -> CareGiverDashboardScreen()
            is CaregiverAddPatient -> CaregiverAddPatientScreen()
            is AddMedication1 -> AddMedicationStep1Screen()
            is AddMedication2 -> AddMedicationStep2Screen()
            is AddMedication3 -> AddMedicationStep3Screen()
            is AddMedication4 -> AddMedicationStep4Screen()
            is EditMedication -> EditMedicationScreen(medicationId = destination.id)
            is ProfilePage -> ProfileScreen()
            is ProfilePhotoView -> ProfilePhotoViewScreen()
            is LanguageSettings -> LanguageSettingsScreen()
            is PrivacyPolicy -> PrivacyPolicyScreen()
            is AppVersion -> AppVersionScreen()
            is MorningReminder -> ReminderScreen(type = "morning", time = "8:00 AM")
            is AfternoonReminder -> ReminderScreen(type = "afternoon", time = "12:00 PM")
            is EveningReminder -> ReminderScreen(type = "evening", time = "8:00 PM")
            is MedicationHistory -> MedicationHistoryScreen()
            else -> SplashScreen()
        }
    }
}
