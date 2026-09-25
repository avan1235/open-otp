package ml.dev.kotlin.openotp.otp

import ml.dev.kotlin.openotp.util.decodeBase32ToByteArray
import org.kotlincrypto.macs.hmac.sha1.HmacSHA1
import org.kotlincrypto.macs.hmac.sha2.HmacSHA256
import org.kotlincrypto.macs.hmac.sha2.HmacSHA512
import kotlin.experimental.and
import kotlin.math.pow

class HotpGenerator(
    secret: String,
    private val config: HotpConfig
) {
    private val secret: ByteArray = secret.decodeBase32ToByteArray()

    fun generate(count: HotpCounter): String {
        val message = ByteArray(Long.SIZE_BYTES) { idx ->
            (count ushr (Byte.SIZE_BITS * (Long.SIZE_BYTES - 1 - idx))).toByte()
        }

        val hash = when (config.hmacAlgorithm) {
            HmacAlgorithm.SHA1 -> HmacSHA1(secret)
            HmacAlgorithm.SHA256 -> HmacSHA256(secret)
            HmacAlgorithm.SHA512 -> HmacSHA512(secret)
        }.doFinal(message)

        val offset = hash.last().and(0x0F).toInt()

        val binary = ByteArray(Int.SIZE_BYTES) { idx -> hash[idx + offset] }

        binary[0] = binary[0].and(0x7F)

        val digits = config.codeDigits.number
        val codeInt = binary.fold(0) { acc, byte -> (acc shl Byte.SIZE_BITS) or (byte.toInt() and 0xFF) }
            .rem(10.0.pow(digits).toInt())

        return codeInt.toString().padStart(digits, padChar = '0')
    }
}
