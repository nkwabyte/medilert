package com.nkwabyte.medilert.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.nkwabyte.medilert.ui.screens.SplashScreen
import com.nkwabyte.medilert.ui.screens.auth.*
import com.nkwabyte.medilert.ui.screens.dashboard.*
import com.nkwabyte.medilert.ui.screens.medication.*
import com.nkwabyte.medilert.ui.screens.onboarding.*
import com.nkwabyte.medilert.ui.screens.profile.*
import com.nkwabyte.medilert.ui.screens.reminder.ReminderScreen
import com.nkwabyte.medilert.ui.screens.settings.*
import com.nkwabyte.medilert.ui.screens.setup.*
import com.nkwabyte.medilert.viewmodel.NavViewModel

@Composable
fun AppNavigation(navViewModel: NavViewModel = viewModel()) {
    NavDisplay(
        backStack = navViewModel.backStack,
        onBack = { navViewModel.popBack() },
        entryProvider = entryProvider {
            // Splash & Onboarding
            entry<Splash> { SplashScreen() }
            entry<Onboarding1> { OnboardingScreen1() }
            entry<Onboarding2> { OnboardingScreen2() }

            // Auth
            entry<Login> { LoginScreen() }
            entry<SignUp> { SignUpScreen() }
            entry<ForgotPassword> { ForgotPasswordScreen() }
            entry<RecoveryOtp> { key -> VerifyOtpScreen(source = key.source) }
            entry<NewPassword> { NewPasswordScreen() }

            // Account Setup
            entry<VerifyPhone> { VerifyPhoneScreen() }
            entry<Language> { LanguageScreen() }
            entry<VoiceAccessibility> { VoiceAccessibilityScreen() }
            entry<PersonalInfo> { PersonalInfoScreen() }
            entry<SetPin> { SetPinScreen() }
            entry<ConfirmPin> { ConfirmPinScreen() }
            entry<ForgetPin> { ForgetPinScreen() }
            entry<ResetPin> { ResetPinScreen() }
            entry<UserRole> { UserRoleScreen() }
            entry<UserProfileComplete> { UserProfileCompleteScreen() }

            // Dashboard
            entry<Dashboard> { DashboardScreen() }
            entry<DashboardEmpty> { DashboardEmptyScreen() }
            entry<CareGiverDashboard> { CareGiverDashboardScreen() }
            entry<CaregiverAddPatient> { CaregiverAddPatientScreen() }

            // Medication
            entry<AddMedication1> { AddMedicationStep1Screen() }
            entry<AddMedication2> { AddMedicationStep2Screen() }
            entry<AddMedication3> { AddMedicationStep3Screen() }
            entry<AddMedication4> { AddMedicationStep4Screen() }
            entry<EditMedication> { key -> EditMedicationScreen(medicationId = key.id) }

            // Profile
            entry<ProfilePage> { ProfileScreen() }
            entry<ProfilePhotoView> { ProfilePhotoViewScreen() }

            // Settings
            entry<LanguageSettings> { LanguageSettingsScreen() }
            entry<PrivacyPolicy> { PrivacyPolicyScreen() }
            entry<AppVersion> { AppVersionScreen() }

            // Reminders
            entry<MorningReminder> { ReminderScreen(type = "morning", time = "8:00 AM") }
            entry<AfternoonReminder> { ReminderScreen(type = "afternoon", time = "12:00 PM") }
            entry<EveningReminder> { ReminderScreen(type = "evening", time = "8:00 PM") }

            // History
            entry<MedicationHistory> { MedicationHistoryScreen() }
        }
    )
}
