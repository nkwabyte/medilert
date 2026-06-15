package com.nkwabyte.medilert.data.platform

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*

import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

actual class ImagePicker internal constructor(
    private val launchGallery: () -> Unit,
    private val launchCamera: () -> Unit,
) {
    actual fun pickFromGallery() = launchGallery()
    actual fun pickFromCamera() = launchCamera()
}

@Composable
actual fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): ImagePicker {
    val context = LocalContext.current
    val currentCallback by rememberUpdatedState(onImagePicked)
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)
                ?.use { stream -> currentCallback(stream.readBytes()) }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempUri?.let { uri ->
                context.contentResolver.openInputStream(uri)
                    ?.use { stream -> currentCallback(stream.readBytes()) }
            }
        }
    }

    return remember {
        ImagePicker(
            launchGallery = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            launchCamera = {
                val file = File.createTempFile("profile_photo_", ".jpg", context.cacheDir)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                tempUri = uri
                cameraLauncher.launch(uri)
            }
        )
    }
}

