package com.nkwabyte.medilert.data.platform

expect suspend fun downloadImageBytes(url: String): ByteArray?
