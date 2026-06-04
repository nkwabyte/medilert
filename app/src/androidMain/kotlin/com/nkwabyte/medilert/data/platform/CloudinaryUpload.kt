package com.nkwabyte.medilert.data.platform

import android.util.Log
import com.nkwabyte.medilert.BuildConfig
import com.nkwabyte.medilert.util.sha1Hex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

actual suspend fun uploadImageToCloudinary(imageBytes: ByteArray): String? =
    withContext(Dispatchers.IO) {
        runCatching {
            val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
            val apiKey = BuildConfig.CLOUDINARY_API_KEY
            val apiSecret = BuildConfig.CLOUDINARY_API_SECRET
            val folder = BuildConfig.CLOUDINARY_UPLOAD_FOLDER

            val timestamp = System.currentTimeMillis() / 1000
            val signature = sha1Hex("folder=$folder&timestamp=$timestamp$apiSecret")
            val boundary = "MedilertBoundary${UUID.randomUUID().toString().replace("-", "")}"

            val conn = (URL("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                .openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                doOutput = true
                doInput = true
                connectTimeout = 30_000
                readTimeout = 60_000
            }

            DataOutputStream(conn.outputStream).use { out ->
                out.part(boundary, "api_key", apiKey)
                out.part(boundary, "timestamp", timestamp.toString())
                out.part(boundary, "signature", signature)
                out.part(boundary, "folder", folder)
                out.filePart(boundary, "file", imageBytes, "photo_$timestamp.jpg")
                out.writeBytes("--$boundary--\r\n")
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                JSONObject(conn.inputStream.bufferedReader().readText())
                    .optString("secure_url").takeIf { it.isNotBlank() }
            } else {
                Log.e("Cloudinary", "Upload failed [${conn.responseCode}]: " +
                        "${conn.errorStream?.bufferedReader()?.readText()}")
                null
            }
        }.getOrElse { e ->
            Log.e("Cloudinary", "Upload exception: ${e.message}", e)
            null
        }
    }

private fun DataOutputStream.part(boundary: String, name: String, value: String) {
    writeBytes("--$boundary\r\nContent-Disposition: form-data; name=\"$name\"\r\n\r\n$value\r\n")
}

private fun DataOutputStream.filePart(
    boundary: String, name: String, data: ByteArray, filename: String
) {
    writeBytes("--$boundary\r\n")
    writeBytes("Content-Disposition: form-data; name=\"$name\"; filename=\"$filename\"\r\n")
    writeBytes("Content-Type: image/jpeg\r\n\r\n")
    write(data)
    writeBytes("\r\n")
}
