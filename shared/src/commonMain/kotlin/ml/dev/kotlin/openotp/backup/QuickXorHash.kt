package ml.dev.kotlin.openotp.backup

import kotlin.io.encoding.Base64

internal fun ByteArray.quickXorHash(): ByteArray {
    val hash = ByteArray(QUICK_XOR_HASH_WIDTH_BYTES)
    for (index in indices) {
        val bitOffset = ((index.toLong() * QUICK_XOR_HASH_SHIFT) % QUICK_XOR_HASH_WIDTH_BITS).toInt()
        val byteIndex = bitOffset / 8
        val shifted = (this[index].toInt() and 0xFF) shl (bitOffset % 8)
        hash[byteIndex] = (hash[byteIndex].toInt() xor (shifted and 0xFF)).toByte()
        val nextByteIndex = (byteIndex + 1) % QUICK_XOR_HASH_WIDTH_BYTES
        hash[nextByteIndex] = (hash[nextByteIndex].toInt() xor (shifted ushr 8)).toByte()
    }
    var length = size.toLong()
    for (index in QUICK_XOR_HASH_WIDTH_BYTES - Long.SIZE_BYTES..<QUICK_XOR_HASH_WIDTH_BYTES) {
        hash[index] = (hash[index].toInt() xor (length and 0xFF).toInt()).toByte()
        length = length ushr 8
    }
    return hash
}

internal fun ByteArray.quickXorHashBase64(): String = Base64.encode(quickXorHash())

private const val QUICK_XOR_HASH_WIDTH_BITS: Int = 160

private const val QUICK_XOR_HASH_WIDTH_BYTES: Int = QUICK_XOR_HASH_WIDTH_BITS / 8

private const val QUICK_XOR_HASH_SHIFT: Int = 11
