package com.nkwabyte.medilert.data.platform

import com.nkwabyte.medilert.MedilertApplication
import java.io.File

private fun photoFile(): File =
    File(MedilertApplication.appContext.filesDir, "profile_photo.jpg")

actual fun saveProfilePhotoBytes(bytes: ByteArray) {
    photoFile().writeBytes(bytes)
}

actual fun loadProfilePhotoBytes(): ByteArray? =
    photoFile().takeIf { it.exists() }?.readBytes()

actual fun clearProfilePhotoBytes() {
    photoFile().delete()
}
