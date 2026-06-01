package com.nkwabyte.medilert.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkwabyte.medilert.data.service.AuthService
import com.nkwabyte.medilert.data.service.UserService
import com.nkwabyte.medilert.model.User
import com.nkwabyte.medilert.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel(
    private val authService: AuthService = AuthService(),
    private val userService: UserService = UserService()
) : ViewModel() {


    private val _currentUser = MutableStateFlow(User())
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(authService.isLoggedIn)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userRole = MutableStateFlow(UserRole.PATIENT)
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _voiceEnabled = MutableStateFlow(false)
    val voiceEnabled: StateFlow<Boolean> = _voiceEnabled.asStateFlow()

    private val _tempPin = MutableStateFlow("")
    val tempPin: StateFlow<String> = _tempPin.asStateFlow()

    private val _textSize = MutableStateFlow("Medium")
    val textSize: StateFlow<String> = _textSize.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _fontScale = MutableStateFlow(1f)
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    init {
        // React to Firebase auth state changes
        viewModelScope.launch {
            authService.authStateFlow.collect { firebaseUser ->
                _isLoggedIn.value = firebaseUser != null
                if (firebaseUser == null) {
                    _currentUser.value = User()
                    _userRole.value = UserRole.PATIENT
                }
            }
        }
        // Keep local user state in sync with Firestore in real time
        viewModelScope.launch {
            userService.userProfileFlow.collect { user ->
                user?.let {
                    _currentUser.value = it
                    _userRole.value = it.role
                }
            }
        }
    }

    fun setUserRole(role: UserRole) {
        _userRole.value = role
    }

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    fun setVoiceEnabled(enabled: Boolean) {
        _voiceEnabled.value = enabled
    }

    fun setTempPin(pin: String) {
        _tempPin.value = pin
    }

    fun setTextSize(size: String) {
        _textSize.value = size
        _fontScale.value = when (size) {
            "Small"  -> 0.85f
            "Large"  -> 1.18f
            "XLarge" -> 1.35f
            else     -> 1f
        }
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        updatePreferences(theme = if (enabled) "dark" else "light")
    }

    fun updateUser(user: User) {
        _currentUser.value = user
    }

    /**
     * Update user preferences in Firebase
     */
    fun updatePreferences(
        notificationsEnabled: Boolean? = null,
        soundEnabled: Boolean? = null,
        vibrationEnabled: Boolean? = null,
        theme: String? = null,
        language: String? = null,
        fontSize: String? = null,
        voiceEnabled: Boolean? = null
    ) {
        viewModelScope.launch {
            val updates = mutableMapOf<String, Any>()

            notificationsEnabled?.let { updates["preferences.notificationsEnabled"] = it }
            soundEnabled?.let { updates["preferences.soundEnabled"] = it }
            vibrationEnabled?.let { updates["preferences.vibrationEnabled"] = it }
            theme?.let { updates["preferences.theme"] = it }
            language?.let { updates["preferences.language"] = it }
            fontSize?.let { updates["preferences.fontSize"] = it }
            voiceEnabled?.let { updates["preferences.voiceEnabled"] = it }

            if (updates.isNotEmpty()) {
                userService.updateProfile(updates)
            }
        }
    }

    /**
     * Update user profile information in Firebase
     */
    fun updateProfileInfo(
        name: String? = null,
        phone: String? = null,
        email: String? = null,
        dateOfBirth: String? = null,
        gender: String? = null,
        emergencyContact: String? = null,
        specialty: String? = null,
        photoUrl: String? = null
    ) {
        viewModelScope.launch {
            val updates = mutableMapOf<String, Any>()

            name?.let { updates["name"] = it }
            phone?.let { updates["phone"] = it }
            email?.let { updates["email"] = it }
            dateOfBirth?.let { updates["dateOfBirth"] = it }
            gender?.let { updates["gender"] = it }
            emergencyContact?.let { updates["emergencyContact"] = it }
            specialty?.let { updates["specialty"] = it }
            photoUrl?.let { updates["photoUrl"] = it }

            if (updates.isNotEmpty()) {
                updates["updatedAt"] = System.currentTimeMillis()
                userService.updateProfile(updates)
            }
        }
    }

    fun setLanguageAndPersist(lang: String, context: Context) {
        _selectedLanguage.value = lang
        updatePreferences(language = lang)
        // Recreate the host activity so locale change takes visible effect
        (context as? android.app.Activity)?.recreate()
    }

    fun logout(context: Context) {
        viewModelScope.launch {
            // Sign out from Auth service (clears Firebase auth + session)
            authService.signOut(context)

            // Reset all local state
            _currentUser.value = User()
            _userRole.value = UserRole.PATIENT
            _selectedLanguage.value = "English"
            _voiceEnabled.value = false
            _tempPin.value = ""
            _textSize.value = "Medium"
            _isLoggedIn.value = false
        }
    }
}
