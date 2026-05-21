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
import com.nkwabyte.medilert.navigation.AppNavigation
import com.nkwabyte.medilert.ui.theme.MedilertTheme
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AndroidActivityHolder.activity = this

        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> AndroidActivityHolder.activity = this
                Lifecycle.Event.ON_DESTROY -> {
                    if (AndroidActivityHolder.activity === this) {
                        AndroidActivityHolder.activity = null
                    }
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

    override fun onDestroy() {
        super.onDestroy()
        if (AndroidActivityHolder.activity === this) {
            AndroidActivityHolder.activity = null
        }
    }
}
