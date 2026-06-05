package com.nkwabyte.medilert.data.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.usePinned
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.posix.SEEK_END
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.ftell
import platform.posix.fwrite
import platform.posix.remove
import platform.posix.rewind

@OptIn(ExperimentalForeignApi::class)
private fun photoFilePath(): String {
    val dirs = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
    val caches = dirs.firstOrNull() as? String ?: return ""
    return "$caches/profile_photo.jpg"
}

@OptIn(ExperimentalForeignApi::class)
actual fun saveProfilePhotoBytes(bytes: ByteArray) {
    val path = photoFilePath().takeIf { it.isNotEmpty() } ?: return
    val file = fopen(path, "wb") ?: return
    bytes.usePinned { pinned ->
        fwrite(pinned.addressOf(0), 1uL, bytes.size.toULong(), file)
    }
    fclose(file)
}

@OptIn(ExperimentalForeignApi::class)
actual fun loadProfilePhotoBytes(): ByteArray? {
    val path = photoFilePath().takeIf { it.isNotEmpty() } ?: return null
    val file = fopen(path, "rb") ?: return null
    fseek(file, 0, SEEK_END)
    val size = ftell(file).toInt()
    if (size <= 0) { fclose(file); return null }
    rewind(file)
    val result = memScoped {
        val buf = allocArray<ByteVar>(size)
        val read = fread(buf, 1uL, size.toULong(), file)
        if (read.toInt() == size) buf.readBytes(size) else null
    }
    fclose(file)
    return result
}

@OptIn(ExperimentalForeignApi::class)
actual fun clearProfilePhotoBytes() {
    val path = photoFilePath().takeIf { it.isNotEmpty() } ?: return
    remove(path)
}
