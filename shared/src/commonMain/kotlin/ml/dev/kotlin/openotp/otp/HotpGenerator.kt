package ml.dev.kotlin.openotp.otp

import com.ditchoom.buffer.PlatformBuffer
import com.ditchoom.buffer.allocate
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
        val message = PlatformBuffer.allocate(8).apply { this[0] = count }

        val hash = when (config.hmacAlgorithm) {
            HmacAlgorithm.SHA1 -> HmacSHA1(secret)
            HmacAlgorithm.SHA256 -> HmacSHA256(secret)
            HmacAlgorithm.SHA512 -> HmacSHA512(secret)
        }.doFinal(message.readByteArray(8))

        val offset = hash.last().and(0x0F).toInt()

        val binary = PlatformBuffer.allocate(4).apply {
            for (i in 0..3) {
                this[i] = hash[i + offset]
            }
        }

        binary[0] = binary[0].and(0x7F)

        val digits = config.codeDigits.number
        val codeInt = binary.readInt().rem(10.0.pow(digits).toInt())

        return codeInt.toString().padStart(digits, padChar = '0')
    }
}

typealias HotpCounter = Long
