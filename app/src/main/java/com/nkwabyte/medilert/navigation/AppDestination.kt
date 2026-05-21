package com.nkwabyte.medilert.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface AppDestination : NavKey

// region Splash & Onboarding
@Serializable data object Splash : AppDestination
@Serializable data object Onboarding1 : AppDestination
@Serializable data object Onboarding2 : AppDestination
// endregion

// region Auth
@Serializable data object Login : AppDestination
@Serializable data object SignUp : AppDestination
@Serializable data object ForgotPassword : AppDestination
@Serializable data class RecoveryOtp(val source: String) : AppDestination
@Serializable data object NewPassword : AppDestination
// endregion

// region Account Setup
@Serializable data object VerifyPhone : AppDestination
@Serializable data object Language : AppDestination
@Serializable data object VoiceAccessibility : AppDestination
@Serializable data object PersonalInfo : AppDestination
@Serializable data object SetPin : AppDestination
@Serializable data object ConfirmPin : AppDestination
@Serializable data object ForgetPin : AppDestination
@Serializable data object ResetPin : AppDestination
@Serializable data object UserRole : AppDestination
@Serializable data object UserProfileComplete : AppDestination
// endregion

// region Dashboard
@Serializable data object Dashboard : AppDestination
@Serializable data object DashboardEmpty : AppDestination
@Serializable data object CareGiverDashboard : AppDestination
@Serializable data object CaregiverAddPatient : AppDestination
// endregion

// region Medication
@Serializable data object AddMedication1 : AppDestination
@Serializable data object AddMedication2 : AppDestination
@Serializable data object AddMedication3 : AppDestination
@Serializable data object AddMedication4 : AppDestination
@Serializable data class EditMedication(val id: String) : AppDestination
// endregion

// region Profile
@Serializable data object ProfilePage : AppDestination
@Serializable data object ProfilePhotoView : AppDestination
// endregion

// region Settings
@Serializable data object LanguageSettings : AppDestination
@Serializable data object PrivacyPolicy : AppDestination
@Serializable data object AppVersion : AppDestination
// endregion

// region Reminders
@Serializable data object MorningReminder : AppDestination
@Serializable data object AfternoonReminder : AppDestination
@Serializable data object EveningReminder : AppDestination
// endregion

// region History
@Serializable data object MedicationHistory : AppDestination
// endregion
