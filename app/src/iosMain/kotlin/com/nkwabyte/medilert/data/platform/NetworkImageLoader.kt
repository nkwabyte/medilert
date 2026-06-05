package com.nkwabyte.medilert.data.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.*
import platform.posix.memcpy
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
actual suspend fun downloadImageBytes(url: String): ByteArray? =
    suspendCancellableCoroutine { cont ->
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl == null) { cont.resume(null); return@suspendCancellableCoroutine }
        val request = NSMutableURLRequest.requestWithURL(nsUrl)
        val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { data, _, error ->
            if (error != null || data == null) {
                cont.resume(null)
                return@dataTaskWithRequest
            }
            val bytes = ByteArray(data.length.toInt()).also { arr ->
                arr.usePinned { pinned ->
                    memcpy(pinned.addressOf(0), data.bytes, data.length)
                }
            }
            cont.resume(bytes)
        }
        task.resume()
        cont.invokeOnCancellation { task.cancel() }
    }
