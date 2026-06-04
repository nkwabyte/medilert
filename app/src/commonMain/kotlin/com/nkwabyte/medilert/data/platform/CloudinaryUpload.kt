package com.nkwabyte.medilert.data.platform

expect suspend fun uploadImageToCloudinary(imageBytes: ByteArray): String?
