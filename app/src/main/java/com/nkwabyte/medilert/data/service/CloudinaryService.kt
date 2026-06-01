package com.nkwabyte.medilert.data.service

import android.content.Context
import android.net.Uri
import com.nkwabyte.medilert.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.UUID

object CloudinaryService {

    private val cloudName  get() = BuildConfig.CLOUDINARY_CLOUD_NAME
    private val apiKey     get() = BuildConfig.CLOUDINARY_API_KEY
    private val apiSecret  get() = BuildConfig.CLOUDINARY_API_SECRET
    private val folder     get() = BuildConfig.CLOUDINARY_UPLOAD_FOLDER

    /**
     * Upload an image Uri to Cloudinary and return the secure HTTPS URL.
     * Returns null on error.
     */
    suspend fun uploadProfilePhoto(context: Context, uri: Uri): String? =
        withContext(Dispatchers.IO) {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@withContext null

                val timestamp = System.currentTimeMillis() / 1000
                val paramsToSign = "folder=$folder&timestamp=$timestamp"
                val signature   = sha1("$paramsToSign$apiSecret")
                val boundary    = "MedilertBoundary${UUID.randomUUID().toString().replace("-", "")}"

                val endpoint = URL("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                val conn = (endpoint.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                    doOutput  = true
                    doInput   = true
                    connectTimeout = 30_000
                    readTimeout    = 60_000
                }

                DataOutputStream(conn.outputStream).use { out ->
                    out.writePart(boundary, "api_key",   apiKey)
                    out.writePart(boundary, "timestamp", timestamp.toString())
                    out.writePart(boundary, "signature", signature)
                    out.writePart(boundary, "folder",    folder)
                    out.writeFilePart(boundary, "file", bytes, "photo_${timestamp}.jpg")
                    out.writeBytes("--$boundary--\r\n")
                }

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val body = conn.inputStream.bufferedReader().readText()
                    JSONObject(body).optString("secure_url").takeIf { it.isNotBlank() }
                } else {
                    val err = conn.errorStream?.bufferedReader()?.readText() ?: "unknown"
                    android.util.Log.e("Cloudinary", "Upload failed [$responseCode]: $err")
                    null
                }
            } catch (e: Exception) {
                android.util.Log.e("Cloudinary", "Upload exception: ${e.message}", e)
                null
            }
        }

    // ── Multipart helpers ─────────────────────────────────────────────────────

    private fun DataOutputStream.writePart(boundary: String, name: String, value: String) {
        writeBytes("--$boundary\r\n")
        writeBytes("Content-Disposition: form-data; name=\"$name\"\r\n\r\n")
        writeBytes("$value\r\n")
    }

    private fun DataOutputStream.writeFilePart(
        boundary: String, name: String, data: ByteArray, filename: String
    ) {
        writeBytes("--$boundary\r\n")
        writeBytes("Content-Disposition: form-data; name=\"$name\"; filename=\"$filename\"\r\n")
        writeBytes("Content-Type: image/jpeg\r\n\r\n")
        write(data)
        writeBytes("\r\n")
    }

    private fun sha1(input: String): String {
        val digest = MessageDigest.getInstance("SHA-1")
        return digest.digest(input.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }
}
