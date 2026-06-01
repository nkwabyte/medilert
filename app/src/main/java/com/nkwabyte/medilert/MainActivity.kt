package com.nkwabyte.medilert

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.nkwabyte.medilert.data.PreferencesManager
import com.nkwabyte.medilert.data.SessionManager
import com.nkwabyte.medilert.navigation.AppNavigation
import com.nkwabyte.medilert.ui.theme.MedilertTheme
import com.nkwabyte.medilert.viewmodel.AppViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        prefsManager = PreferencesManager.getInstance(this)

        sessionManager = SessionManager(this) {
            handleSessionExpiration()
        }

        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null && !prefsManager.isSessionExpired()) {
                        prefsManager.updateLastActivityTime()
                        sessionManager.startSessionTracking()
                    }
                }
                Lifecycle.Event.ON_DESTROY -> sessionManager.stopSessionTracking()
                else -> {}
            }
        })

        setContent {
            val appViewModel: AppViewModel = viewModel()
            val isDarkMode by appViewModel.isDarkMode.collectAsState()
            val fontScale  by appViewModel.fontScale.collectAsState()

            MedilertTheme(darkTheme = isDarkMode, fontScale = fontScale) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) {
                    AppNavigation()
                }
            }
        }
    }

    private fun handleSessionExpiration() {
        lifecycleScope.launch {
            FirebaseAuth.getInstance().signOut()
            prefsManager.clearSession()
            recreate()
        }
    }
}
