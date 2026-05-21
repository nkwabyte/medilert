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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Drop this composable into any screen that requires notification permission
 * (e.g. Dashboard). It requests POST_NOTIFICATIONS on Android 13+ and saves
 * the FCM token to Firestore so the backend can send reminders to this device.
 *
 * Usage:
 *   RequestNotificationPermissionAndRegisterToken()
 */
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
            // Below Android 13 notifications are granted automatically
            saveFcmToken()
        }
    }
}

private fun saveFcmToken() {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .update("fcmToken", token)
    }
}
