package ml.dev.kotlin.openotp.util

import org.kotlincrypto.SecureRandom

fun randomBytesChallenge(count: Int): ByteArray? = runCatchingOrNull {
    ByteArray(count).apply(SecureRandom()::nextBytesCopyTo)
}
