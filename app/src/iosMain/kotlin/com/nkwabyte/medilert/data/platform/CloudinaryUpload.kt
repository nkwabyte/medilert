package com.nkwabyte.medilert.data.platform

import com.nkwabyte.medilert.util.sha1Hex
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.*
import kotlin.coroutines.resume

// These credentials are read-only on the client; for production,
// sign uploads server-side and use an unsigned preset instead.
private const val CLOUD_NAME = "dtmzbg1aw"
private const val API_KEY    = "165682576583475"
private const val API_SECRET = "m8g6z2j_AoIT-bQqDunGlYO_Pt4"
private const val FOLDER     = "medilert/profiles"

@OptIn(ExperimentalForeignApi::class)
actual suspend fun uploadImageToCloudinary(imageBytes: ByteArray): String? =
    suspendCancellableCoroutine { cont ->
        try {
            val timestamp = (NSDate().timeIntervalSince1970).toLong().toString()
            val signature = sha1Hex("folder=$FOLDER&timestamp=$timestamp$API_SECRET")
            val boundary  = "MedilertBoundary${NSUUID().UUIDString.replace("-", "")}"

            val url     = NSURL.URLWithString("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload")!!
            val request = NSMutableURLRequest.requestWithURL(url)
            request.HTTPMethod = "POST"
            request.setValue("multipart/form-data; boundary=$boundary", forHTTPHeaderField = "Content-Type")

            val body = NSMutableData()
            fun addPart(name: String, value: String) {
                "--$boundary\r\nContent-Disposition: form-data; name=\"$name\"\r\n\r\n$value\r\n"
                    .encodeToByteArray().toNSData()?.let { body.appendData(it) }
            }
            fun addFilePart(name: String, data: ByteArray, filename: String) {
                "--$boundary\r\nContent-Disposition: form-data; name=\"$name\"; filename=\"$filename\"\r\nContent-Type: image/jpeg\r\n\r\n"
                    .encodeToByteArray().toNSData()?.let { body.appendData(it) }
                data.toNSData()?.let { body.appendData(it) }
                "\r\n".encodeToByteArray().toNSData()?.let { body.appendData(it) }
            }

            addPart("api_key",   API_KEY)
            addPart("timestamp", timestamp)
            addPart("signature", signature)
            addPart("folder",    FOLDER)
            addFilePart("file", imageBytes, "photo_$timestamp.jpg")
            "--$boundary--\r\n".encodeToByteArray().toNSData()?.let { body.appendData(it) }

            request.HTTPBody = body

            val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { data, _, error ->
                if (error != null || data == null) { cont.resume(null); return@dataTaskWithRequest }
                val json = NSString.create(data, NSUTF8StringEncoding) as? String
                cont.resume(json?.let { parseSecureUrl(it) })
            }
            task.resume()
            cont.invokeOnCancellation { task.cancel() }
        } catch (_: Exception) {
            cont.resume(null)
        }
    }

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData? {
    if (isEmpty()) return NSData.data()
    return usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), size.toULong())
    }
}

private fun parseSecureUrl(json: String): String? =
    Regex("\"secure_url\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
        .find(json)?.groupValues?.getOrNull(1)?.replace("\\/", "/")
