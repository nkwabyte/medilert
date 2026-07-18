package com.nkwabyte.medilert

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.navigation.AppNavigation
import com.nkwabyte.medilert.ui.theme.MedilertTheme
import com.nkwabyte.medilert.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                "medilert_reminders",
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { 
                description = "Reminders to take your medication on time" 
                val soundUri = Uri.parse("android.resource://$packageName/${R.raw.urgent_simple_tone_loop}")
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .build()
                setSound(soundUri, audioAttributes)
            }
            manager.createNotificationChannel(channel)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel()

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
            val appViewModel: AppViewModel = viewModel { AppViewModel() }
            val isDarkMode by appViewModel.isDarkMode.collectAsState()
            val fontScale  by appViewModel.fontScale.collectAsState()
            val selectedLanguage by appViewModel.selectedLanguage.collectAsState()

            MedilertTheme(darkTheme = isDarkMode, fontScale = fontScale) {
                androidx.compose.runtime.CompositionLocalProvider(
                    com.nkwabyte.medilert.util.LocalAppLanguage provides selectedLanguage
                ) {
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
    }

    override fun onDestroy() {
        super.onDestroy()
        if (AndroidActivityHolder.activity === this) {
            AndroidActivityHolder.activity = null
        }
    }
}
