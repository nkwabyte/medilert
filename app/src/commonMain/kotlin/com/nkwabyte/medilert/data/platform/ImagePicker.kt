package com.nkwabyte.medilert.data.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

expect class ImagePicker {
    fun pickFromGallery()
    fun pickFromCamera()
}

@Composable
expect fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): ImagePicker

