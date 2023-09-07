package ml.dev.kotlin.openotp.util

import kotlin.Char.Companion.MIN_HIGH_SURROGATE
import kotlin.Char.Companion.MIN_LOW_SURROGATE

private val hexDigits: CharArray = "0123456789ABCDEF".toCharArray()

private val unreservedChars = BooleanArray('z'.code + 1).apply {
    set('-'.code, true)
    set('.'.code, true)
    set('_'.code, true)
    for (c in '0'..'9') {
        set(c.code, true)
    }
    for (c in 'A'..'Z') {
        set(c.code, true)
    }
    for (c in 'a'..'z') {
        set(c.code, true)
    }
}

private fun Char.isUnreserved(): Boolean = this <= 'z' && unreservedChars[code]

private fun StringBuilder.appendEncodedDigit(digit: Int) {
    this.append(hexDigits[digit and 0x0F])
}

private fun StringBuilder.appendEncodedByte(ch: Int) {
    this.append("%")
    this.appendEncodedDigit(ch shr 4)
    this.appendEncodedDigit(ch)
}

fun decode(source: String, plusToSpace: Boolean = false): String {
    if (source.isEmpty()) {
        return source
    }

    val length = source.length
    val out = StringBuilder(length)
    var bytesBuffer: ByteArray? = null
    var bytesPos = 0
    var i = 0
    var started = false
    while (i < length) {
        val ch = source[i]
        if (ch == '%') {
            if (!started) {
                out.append(source, 0, i)
                started = true
            }
            if (bytesBuffer == null) {
                // the remaining characters divided by the length of the encoding format %xx, is the maximum number
                // of bytes that can be extracted
                bytesBuffer = ByteArray((length - i) / 3)
            }
            i++
            require(length >= i + 2) { "Incomplete trailing escape ($ch) pattern" }
            try {
                val v = source.substring(i, i + 2).toInt(16)
                require(v in 0..0xFF) { "Illegal escape value" }
                bytesBuffer[bytesPos++] = v.toByte()
                i += 2
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Illegal characters in escape sequence: $e.message", e)
            }
        } else {
            if (bytesBuffer != null) {
                out.append(bytesBuffer.decodeToString(0, bytesPos))
                started = true
                bytesBuffer = null
                bytesPos = 0
            }
            if (plusToSpace && ch == '+') {
                if (!started) {
                    out.append(source, 0, i)
                    started = true
                }
                out.append(" ")
            } else if (started) {
                out.append(ch)
            }
            i++
        }
    }

    if (bytesBuffer != null) {
        out.append(bytesBuffer.decodeToString(0, bytesPos))
    }

    return if (!started) source else out.toString()
}

private fun CharSequence.codePointAt(index: Int): Int {
    if (index !in indices) throw IndexOutOfBoundsException("index $index was not in range $indices")

    val firstChar = this[index]
    if (firstChar.isHighSurrogate()) {
        val nextChar = getOrNull(index + 1)
        if (nextChar?.isLowSurrogate() == true) {
            return toCodePoint(firstChar, nextChar)
        }
    }

    return firstChar.code
}

private fun isSupplementaryCodePoint(codePoint: Int): Boolean =
    codePoint in MIN_SUPPLEMENTARY_CODE_POINT..MAX_CODE_POINT

private fun toCodePoint(highSurrogate: Char, lowSurrogate: Char): Int =
    (highSurrogate.code shl 10) + lowSurrogate.code + SURROGATE_DECODE_OFFSET

private fun isBmpCodePoint(codePoint: Int): Boolean = codePoint ushr 16 == 0

private fun highSurrogateOf(codePoint: Int): Char =
    ((codePoint ushr 10) + HIGH_SURROGATE_ENCODE_OFFSET.code).toChar()

private fun lowSurrogateOf(codePoint: Int): Char =
    ((codePoint and 0x3FF) + MIN_LOW_SURROGATE.code).toChar()

private const val MAX_CODE_POINT: Int = 0x10FFFF

private const val MIN_SUPPLEMENTARY_CODE_POINT: Int = 0x10000

private const val SURROGATE_DECODE_OFFSET: Int =
    MIN_SUPPLEMENTARY_CODE_POINT - (MIN_HIGH_SURROGATE.code shl 10) - MIN_LOW_SURROGATE.code

private const val HIGH_SURROGATE_ENCODE_OFFSET: Char = MIN_HIGH_SURROGATE - (MIN_SUPPLEMENTARY_CODE_POINT ushr 10)
