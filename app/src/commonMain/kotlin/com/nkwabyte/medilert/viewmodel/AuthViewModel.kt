package com.nkwabyte.medilert.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.data.PreferencesManager
import com.nkwabyte.medilert.data.service.AuthService
import com.nkwabyte.medilert.data.service.UserService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val verificationId: String? = null,
    val incompleteRegistration: Boolean = false
)

class AuthViewModel(
    private val authService: AuthService = AuthService(),
    private val userService: UserService = UserService(),
    private val prefsManager: PreferencesManager = PreferencesManager.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUser get() = authService.currentUser
    val isLoggedIn get() = authService.isLoggedIn

    fun signInWithEmail(email: String, password: String, rememberMe: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authService.signInWithEmail(email, password)) {
                is FirebaseResult.Success -> {
                    prefsManager.setRememberMe(rememberMe)
                    if (rememberMe) prefsManager.saveLastLoginEmail(email)
                    else prefsManager.clearLastLoginEmail()
                    _uiState.value = AuthUiState(isSuccess = true)
                }
                is FirebaseResult.Error -> _uiState.value = AuthUiState(errorMessage = result.message)
                else -> Unit
            }
        }
    }

    fun registerWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authService.registerWithEmail(email, password)) {
                is FirebaseResult.Success -> _uiState.value = AuthUiState(isSuccess = true)
                is FirebaseResult.Error -> {
                    if (result.message.contains("email address is already in use", ignoreCase = true) ||
                        result.message.contains("EMAIL_EXISTS", ignoreCase = true)) {
                        handleIncompleteRegistration(email, password)
                    } else {
                        _uiState.value = AuthUiState(errorMessage = result.message)
                    }
                }
                else -> Unit
            }
        }
    }

    private suspend fun handleIncompleteRegistration(email: String, password: String) {
        when (val signInResult = authService.signInWithEmail(email, password)) {
            is FirebaseResult.Success -> {
                when (userService.getProfile()) {
                    is FirebaseResult.Success -> _uiState.value = AuthUiState(
                        errorMessage = "This email is already registered with a complete profile. Please use the Sign In page instead."
                    )
                    is FirebaseResult.Error -> _uiState.value = AuthUiState(
                        isSuccess = true,
                        incompleteRegistration = true
                    )
                    else -> Unit
                }
            }
            is FirebaseResult.Error -> _uiState.value = AuthUiState(
                errorMessage = "This email is already registered. Please use the correct password or reset it."
            )
            else -> Unit
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authService.signInWithGoogle()) {
                is FirebaseResult.Success -> _uiState.value = AuthUiState(isSuccess = true)
                is FirebaseResult.Error -> _uiState.value = AuthUiState(errorMessage = result.message)
                else -> Unit
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authService.sendPasswordResetEmail(email)) {
                is FirebaseResult.Success -> _uiState.value = AuthUiState(isSuccess = true)
                is FirebaseResult.Error -> _uiState.value = AuthUiState(errorMessage = result.message)
                else -> Unit
            }
        }
    }

    fun sendPhoneVerificationCode(phoneNumber: String) {
        _uiState.value = AuthUiState(isLoading = true)
        authService.sendPhoneVerificationCode(
            phoneNumber = phoneNumber,
            onCodeSent = { verificationId ->
                _uiState.value = AuthUiState(isSuccess = true, verificationId = verificationId)
            },
            onVerificationFailed = { error ->
                _uiState.value = AuthUiState(errorMessage = error)
            }
        )
    }

    fun verifyPhoneCode(verificationId: String, code: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authService.verifyPhoneCode(verificationId, code)) {
                is FirebaseResult.Success -> _uiState.value = AuthUiState(isSuccess = true)
                is FirebaseResult.Error -> _uiState.value = AuthUiState(errorMessage = result.message)
                else -> Unit
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authService.signOut()
            _uiState.value = AuthUiState()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun getLastLoginEmail(): String? = prefsManager.getLastLoginEmail()
    fun isRememberMeEnabled(): Boolean = prefsManager.isRememberMeEnabled()
}
