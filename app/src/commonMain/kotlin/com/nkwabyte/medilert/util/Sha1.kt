package com.nkwabyte.medilert.util

internal fun sha1Hex(input: String): String =
    sha1Bytes(input.encodeToByteArray())
        .joinToString("") { it.toInt().and(0xff).toString(16).padStart(2, '0') }

private fun sha1Bytes(message: ByteArray): ByteArray {
    val padded = buildList<Byte> {
        addAll(message.toList())
        add(0x80.toByte())
        while ((size + 8) % 64 != 0) add(0)
        val bitLen = message.size.toLong() * 8L
        for (i in 7 downTo 0) add((bitLen shr (i * 8)).toByte())
    }.toByteArray()

    var h0 = 0x67452301
    var h1 = 0xEFCDAB89.toInt()
    var h2 = 0x98BADCFE.toInt()
    var h3 = 0x10325476
    var h4 = 0xC3D2E1F0.toInt()

    for (chunkStart in padded.indices step 64) {
        val w = IntArray(80)
        for (i in 0..15) {
            w[i] = ((padded[chunkStart + i * 4].toInt() and 0xff) shl 24) or
                   ((padded[chunkStart + i * 4 + 1].toInt() and 0xff) shl 16) or
                   ((padded[chunkStart + i * 4 + 2].toInt() and 0xff) shl 8) or
                   (padded[chunkStart + i * 4 + 3].toInt() and 0xff)
        }
        for (i in 16..79) {
            val v = w[i - 3] xor w[i - 8] xor w[i - 14] xor w[i - 16]
            w[i] = (v shl 1) or (v ushr 31)
        }

        var a = h0; var b = h1; var c = h2; var d = h3; var e = h4

        for (i in 0..79) {
            val f: Int
            val k: Int
            when {
                i < 20 -> { f = (b and c) or (b.inv() and d); k = 0x5A827999 }
                i < 40 -> { f = b xor c xor d;                k = 0x6ED9EBA1 }
                i < 60 -> { f = (b and c) or (b and d) or (c and d); k = 0x8F1BBCDC.toInt() }
                else   -> { f = b xor c xor d;                k = 0xCA62C1D6.toInt() }
            }
            val temp = ((a shl 5) or (a ushr 27)) + f + e + k + w[i]
            e = d; d = c; c = (b shl 30) or (b ushr 2); b = a; a = temp
        }

        h0 += a; h1 += b; h2 += c; h3 += d; h4 += e
    }

    val result = ByteArray(20)
    listOf(h0, h1, h2, h3, h4).forEachIndexed { i, h ->
        result[i * 4]     = (h shr 24).toByte()
        result[i * 4 + 1] = (h shr 16).toByte()
        result[i * 4 + 2] = (h shr 8).toByte()
        result[i * 4 + 3] = h.toByte()
    }
    return result
}
