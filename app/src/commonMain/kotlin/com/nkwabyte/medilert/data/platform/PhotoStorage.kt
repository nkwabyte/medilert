package com.nkwabyte.medilert.data.platform

expect fun saveProfilePhotoBytes(bytes: ByteArray)
expect fun loadProfilePhotoBytes(): ByteArray?
expect fun clearProfilePhotoBytes()
