package com.nkwabyte.medilert.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkwabyte.medilert.data.platform.clearProfilePhotoBytes
import com.nkwabyte.medilert.data.platform.downloadImageBytes
import com.nkwabyte.medilert.data.platform.loadProfilePhotoBytes
import com.nkwabyte.medilert.data.platform.saveProfilePhotoBytes
import com.nkwabyte.medilert.data.platform.uploadImageToCloudinary
import com.nkwabyte.medilert.data.service.AuthService
import com.nkwabyte.medilert.data.service.UserService
import com.nkwabyte.medilert.model.User
import com.nkwabyte.medilert.model.UserRole
import com.nkwabyte.medilert.ui.components.DashboardTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

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

    private val _activeDashboardTab = MutableStateFlow(DashboardTab.HOME)
    val activeDashboardTab: StateFlow<DashboardTab> = _activeDashboardTab.asStateFlow()

    private val _fontScale = MutableStateFlow(1f)
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    private val _notificationTone = MutableStateFlow("Happy Bells")
    val notificationTone: StateFlow<String> = _notificationTone.asStateFlow()

    private val _isUploadingPhoto = MutableStateFlow(false)
    val isUploadingPhoto: StateFlow<Boolean> = _isUploadingPhoto.asStateFlow()

    private val _profilePhotoBytes = MutableStateFlow<ByteArray?>(null)
    val profilePhotoBytes: StateFlow<ByteArray?> = _profilePhotoBytes.asStateFlow()

    init {
        // Restore photo from local cache immediately so it's available before auth resolves
        _profilePhotoBytes.value = loadProfilePhotoBytes()

        viewModelScope.launch {
            authService.authStateFlow.collect { firebaseUser ->
                _isLoggedIn.value = firebaseUser != null
                if (firebaseUser == null) {
                    _currentUser.value = User()
                    _userRole.value = UserRole.PATIENT
                }
            }
        }
        viewModelScope.launch {
            userService.userProfileFlow.collect { user ->
                user?.let {
                    _currentUser.value = it
                    _userRole.value = it.role
                    _selectedLanguage.value = it.preferences.language
                    _isDarkMode.value = it.preferences.theme == "dark"
                    _voiceEnabled.value = it.preferences.voiceEnabled
                    _notificationTone.value = if (it.preferences.notificationTone.isNotBlank()) it.preferences.notificationTone else "Happy Bells"
                    setTextSize(it.preferences.fontSize)
                    // Only try network download if no local cache exists
                    if (it.photoUrl.isNotBlank() && _profilePhotoBytes.value == null) {
                        viewModelScope.launch {
                            val bytes = downloadImageBytes(it.photoUrl)
                            if (bytes != null) {
                                _profilePhotoBytes.value = bytes
                                saveProfilePhotoBytes(bytes)
                            }
                        }
                    }
                }
            }
        }
    }

    fun setDashboardTab(tab: DashboardTab) { _activeDashboardTab.value = tab }

    fun setUserRole(role: UserRole) { _userRole.value = role }
    fun setLanguage(lang: String) { _selectedLanguage.value = lang }
    fun setVoiceEnabled(enabled: Boolean) { _voiceEnabled.value = enabled }
    fun setTempPin(pin: String) { _tempPin.value = pin }
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

    fun setLanguageAndPersist(lang: String) {
        _selectedLanguage.value = lang
        updatePreferences(language = lang)
    }

    fun updateUser(user: User) { _currentUser.value = user }

    fun updatePreferences(
        notificationsEnabled: Boolean? = null,
        soundEnabled: Boolean? = null,
        vibrationEnabled: Boolean? = null,
        theme: String? = null,
        language: String? = null,
        fontSize: String? = null,
        voiceEnabled: Boolean? = null,
        missedAlertsEnabled: Boolean? = null,
        lowAdherenceEnabled: Boolean? = null,
        notificationTone: String? = null
    ) {
        viewModelScope.launch {
            val currentPrefs = _currentUser.value.preferences
            val updatedPrefs = currentPrefs.copy(
                notificationsEnabled = notificationsEnabled ?: currentPrefs.notificationsEnabled,
                soundEnabled = soundEnabled ?: currentPrefs.soundEnabled,
                vibrationEnabled = vibrationEnabled ?: currentPrefs.vibrationEnabled,
                theme = theme ?: currentPrefs.theme,
                language = language ?: currentPrefs.language,
                fontSize = fontSize ?: currentPrefs.fontSize,
                voiceEnabled = voiceEnabled ?: currentPrefs.voiceEnabled,
                missedAlertsEnabled = missedAlertsEnabled ?: currentPrefs.missedAlertsEnabled,
                lowAdherenceEnabled = lowAdherenceEnabled ?: currentPrefs.lowAdherenceEnabled,
                notificationTone = notificationTone ?: currentPrefs.notificationTone
            )
            _currentUser.value = _currentUser.value.copy(preferences = updatedPrefs)
            _isDarkMode.value = updatedPrefs.theme == "dark"
            _notificationTone.value = updatedPrefs.notificationTone

            userService.updateProfile(mapOf("preferences" to updatedPrefs))
        }
    }

    fun setNotificationTone(tone: String) {
        _notificationTone.value = tone
        updatePreferences(notificationTone = tone)
    }

    fun updateProfileInfo(
        name: String? = null,
        phone: String? = null,
        email: String? = null,
        dateOfBirth: String? = null,
        gender: String? = null,
        emergencyContact: String? = null,
        specialty: String? = null
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
            if (updates.isNotEmpty()) {
                updates["updatedAt"] = Clock.System.now().toEpochMilliseconds()
                userService.updateProfile(updates)
            }
        }
    }

    fun uploadProfilePhoto(imageBytes: ByteArray) {
        viewModelScope.launch {
            _isUploadingPhoto.value = true
            _profilePhotoBytes.value = imageBytes
            saveProfilePhotoBytes(imageBytes)
            val url = uploadImageToCloudinary(imageBytes)
            if (url != null) {
                userService.updateProfile(mapOf("photoUrl" to url))
            } else {
                _profilePhotoBytes.value = null
                clearProfilePhotoBytes()
            }
            _isUploadingPhoto.value = false
        }
    }

    fun removeProfilePhoto() {
        viewModelScope.launch {
            _profilePhotoBytes.value = null
            clearProfilePhotoBytes()
            userService.updateProfile(mapOf("photoUrl" to ""))
        }
    }

    fun logout() {
        viewModelScope.launch {
            authService.signOut()
            _currentUser.value = User()
            _userRole.value = UserRole.PATIENT
            _selectedLanguage.value = "English"
            _voiceEnabled.value = false
            _tempPin.value = ""
            _textSize.value = "Medium"
            _activeDashboardTab.value = DashboardTab.HOME
            _profilePhotoBytes.value = null
            clearProfilePhotoBytes()
            _isLoggedIn.value = false
        }
    }
}
