package com.nkwabyte.medilert.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
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
    val incompleteRegistration: Boolean = false // New flag for incomplete registration
)

class AuthViewModel @JvmOverloads constructor(
    application: Application,
    private val authService: AuthService = AuthService(application),
    private val userService: UserService = UserService()
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val prefsManager: PreferencesManager by lazy {
        PreferencesManager.getInstance(getApplication())
    }

    val currentUser get() = authService.currentUser
    val isLoggedIn get() = authService.isLoggedIn

    fun signInWithEmail(email: String, password: String, rememberMe: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authService.signInWithEmail(email, password)) {
                is FirebaseResult.Success -> {
                    // Handle remember me preference
                    prefsManager.setRememberMe(rememberMe)
                    if (rememberMe) {
                        prefsManager.saveLastLoginEmail(email)
                    } else {
                        prefsManager.clearLastLoginEmail()
                    }
                    _uiState.value = AuthUiState(isSuccess = true)
                }
                is FirebaseResult.Error -> _uiState.value =
                    AuthUiState(errorMessage = result.message)

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
                    // Check if the error is "email already in use"
                    if (result.message.contains(
                            "email address is already in use",
                            ignoreCase = true
                        ) ||
                        result.message.contains("EMAIL_EXISTS", ignoreCase = true)
                    ) {
                        // Try to sign in and check if profile is complete
                        handleIncompleteRegistration(email, password)
                    } else {
                        _uiState.value = AuthUiState(errorMessage = result.message)
                    }
                }

                else -> Unit
            }
        }
    }

    /**
     * Handles the case where email exists in Firebase Auth but profile might not be complete.
     * Signs in the user and checks if their Firestore profile exists.
     */
    private suspend fun handleIncompleteRegistration(email: String, password: String) {
        when (val signInResult = authService.signInWithEmail(email, password)) {
            is FirebaseResult.Success -> {
                // Successfully signed in, now check if Firestore profile exists
                when (val profileResult = userService.getProfile()) {
                    is FirebaseResult.Success -> {
                        // Profile exists and is complete - they should use sign in flow
                        _uiState.value = AuthUiState(
                            errorMessage = "This email is already registered with a complete profile. Please use the Sign In page instead."
                        )
                    }

                    is FirebaseResult.Error -> {
                        // Profile doesn't exist - let them complete registration
                        _uiState.value = AuthUiState(
                            isSuccess = true,
                            incompleteRegistration = true
                        )
                    }

                    else -> Unit
                }
            }

            is FirebaseResult.Error -> {
                // Sign in failed - likely wrong password
                _uiState.value = AuthUiState(
                    errorMessage = "This email is already registered. Please use the correct password or reset it."
                )
            }

            else -> Unit
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authService.signInWithGoogle(context)) {
                is FirebaseResult.Success -> _uiState.value = AuthUiState(isSuccess = true)
                is FirebaseResult.Error -> _uiState.value =
                    AuthUiState(errorMessage = result.message)

                else -> Unit
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authService.sendPasswordResetEmail(email)) {
                is FirebaseResult.Success -> _uiState.value = AuthUiState(isSuccess = true)
                is FirebaseResult.Error -> _uiState.value =
                    AuthUiState(errorMessage = result.message)

                else -> Unit
            }
        }
    }

    fun sendPhoneVerificationCode(activity: Activity, phoneNumber: String) {
        _uiState.value = AuthUiState(isLoading = true)
        authService.sendPhoneVerificationCode(
            activity = activity,
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
                is FirebaseResult.Error -> _uiState.value =
                    AuthUiState(errorMessage = result.message)

                else -> Unit
            }
        }
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            authService.signOut(context)
            _uiState.value = AuthUiState()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Get last login email if remember me was enabled
     */
    fun getLastLoginEmail(): String? {
        return prefsManager.getLastLoginEmail()
    }

    /**
     * Check if remember me is enabled
     */
    fun isRememberMeEnabled(): Boolean {
        return prefsManager.isRememberMeEnabled()
    }
}
