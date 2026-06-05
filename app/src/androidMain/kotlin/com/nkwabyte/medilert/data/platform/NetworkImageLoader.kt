package com.nkwabyte.medilert.data.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

actual suspend fun downloadImageBytes(url: String): ByteArray? =
    withContext(Dispatchers.IO) {
        runCatching { URL(url).readBytes() }.getOrNull()
    }
