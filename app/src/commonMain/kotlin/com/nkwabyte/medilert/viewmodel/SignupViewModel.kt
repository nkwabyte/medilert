package com.nkwabyte.medilert.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.data.repository.UserRepository
import com.nkwabyte.medilert.model.User
import com.nkwabyte.medilert.model.UserPreferences
import com.nkwabyte.medilert.model.UserRole
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock

class SignupViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _signupData = MutableStateFlow(SignupData())
    val signupData: StateFlow<SignupData> = _signupData.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    fun setBasicInfo(name: String, email: String) {
        _signupData.value = _signupData.value.copy(name = name, email = email)
    }

    fun setPhone(phone: String) {
        _signupData.value = _signupData.value.copy(phone = phone)
    }

    fun setLanguage(language: String) {
        _signupData.value = _signupData.value.copy(
            preferences = _signupData.value.preferences.copy(language = language)
        )
    }

    fun setVoiceEnabled(enabled: Boolean) {
        _signupData.value = _signupData.value.copy(
            preferences = _signupData.value.preferences.copy(voiceEnabled = enabled)
        )
    }

    fun setPersonalInfo(gender: String, dateOfBirth: String, emergencyContact: String) {
        _signupData.value = _signupData.value.copy(
            gender = gender, dateOfBirth = dateOfBirth, emergencyContact = emergencyContact
        )
    }

    fun setPin(pin: String) {
        _signupData.value = _signupData.value.copy(pin = pin)
    }

    fun setUserRole(role: UserRole, specialty: String = "") {
        _signupData.value = _signupData.value.copy(role = role, specialty = specialty)
    }

    fun updatePreferences(
        theme: String? = null,
        notificationsEnabled: Boolean? = null,
        soundEnabled: Boolean? = null,
        vibrationEnabled: Boolean? = null,
        fontSize: String? = null
    ) {
        val currentPrefs = _signupData.value.preferences
        _signupData.value = _signupData.value.copy(
            preferences = currentPrefs.copy(
                theme = theme ?: currentPrefs.theme,
                notificationsEnabled = notificationsEnabled ?: currentPrefs.notificationsEnabled,
                soundEnabled = soundEnabled ?: currentPrefs.soundEnabled,
                vibrationEnabled = vibrationEnabled ?: currentPrefs.vibrationEnabled,
                fontSize = fontSize ?: currentPrefs.fontSize
            )
        )
    }

    fun saveUserProfile() {
        viewModelScope.launch {
            val currentUser = Firebase.auth.currentUser
            if (currentUser == null) {
                _saveState.value = SaveState.Error("User not authenticated")
                return@launch
            }

            _saveState.value = SaveState.Loading

            val now = Clock.System.now().toEpochMilliseconds()
            val user = User(
                id = currentUser.uid,
                name = _signupData.value.name,
                phone = _signupData.value.phone,
                email = _signupData.value.email,
                gender = _signupData.value.gender,
                dateOfBirth = _signupData.value.dateOfBirth,
                role = _signupData.value.role,
                pin = _signupData.value.pin,
                photoUrl = currentUser.photoURL ?: "",
                specialty = _signupData.value.specialty,
                emergencyContact = _signupData.value.emergencyContact,
                preferences = _signupData.value.preferences,
                createdAt = now,
                updatedAt = now
            )

            _saveState.value = when (val result = userRepository.saveUser(user)) {
                is FirebaseResult.Success -> SaveState.Success
                is FirebaseResult.Error -> SaveState.Error(result.message)
                else -> SaveState.Error("Unknown error occurred")
            }
        }
    }

    fun clearError() {
        if (_saveState.value is SaveState.Error) _saveState.value = SaveState.Idle
    }

    fun reset() {
        _signupData.value = SignupData()
        _saveState.value = SaveState.Idle
    }
}

data class SignupData(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val gender: String = "",
    val dateOfBirth: String = "",
    val emergencyContact: String = "",
    val pin: String = "",
    val role: UserRole = UserRole.PATIENT,
    val specialty: String = "",
    val preferences: UserPreferences = UserPreferences()
)

sealed class SaveState {
    object Idle : SaveState()
    object Loading : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}
