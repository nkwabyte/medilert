package com.nkwabyte.medilert

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.nkwabyte.medilert.data.PreferencesManager
import com.nkwabyte.medilert.data.SessionManager
import com.nkwabyte.medilert.navigation.AppNavigation
import com.nkwabyte.medilert.ui.theme.MedilertTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize preferences manager
        prefsManager = PreferencesManager.getInstance(this)

        // Initialize session manager with auto-logout callback
        sessionManager = SessionManager(this) {
            // Called when session expires (24 hours of inactivity)
            handleSessionExpiration()
        }

        // Add lifecycle observer to track app state
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    // Update activity when app comes to foreground
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null && !prefsManager.isSessionExpired()) {
                        prefsManager.updateLastActivityTime()
                        sessionManager.startSessionTracking()
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    // Don't stop tracking, let it continue in background
                    // Session will timeout after 12 hours of no activity
                }
                Lifecycle.Event.ON_DESTROY -> {
                    sessionManager.stopSessionTracking()
                }
                else -> {}
            }
        })

        setContent {
            MedilertTheme {
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
            // Sign out user
            FirebaseAuth.getInstance().signOut()
            prefsManager.clearSession()

            // Restart activity to go back to login
            recreate()
        }
    }
}
