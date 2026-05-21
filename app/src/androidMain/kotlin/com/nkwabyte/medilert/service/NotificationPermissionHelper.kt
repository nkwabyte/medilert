package com.nkwabyte.medilert.service

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun RequestNotificationPermissionAndRegisterToken() {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) saveFcmToken()
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                launcher.launch(permission)
            } else {
                saveFcmToken()
            }
        } else {
            saveFcmToken()
        }
    }
}

private fun saveFcmToken() {
    val uid = Firebase.auth.currentUser?.uid ?: return
    com.google.firebase.messaging.FirebaseMessaging.getInstance().token
        .addOnSuccessListener { token ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Firebase.firestore.collection("users").document(uid)
                        .update(mapOf("fcmToken" to token))
                } catch (_: Exception) { }
            }
        }
}
