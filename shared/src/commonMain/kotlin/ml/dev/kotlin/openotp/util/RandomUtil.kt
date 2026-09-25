package ml.dev.kotlin.openotp.util

import org.kotlincrypto.random.CryptoRand

fun randomBytesChallenge(count: Int): ByteArray? = runCatchingOrNull {
    CryptoRand.Default.nextBytes(ByteArray(count))
}
