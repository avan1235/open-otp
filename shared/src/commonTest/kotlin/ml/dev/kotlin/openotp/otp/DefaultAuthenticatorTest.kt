package ml.dev.kotlin.openotp.otp

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultAuthenticatorTest {

    @Test
    fun testGeneratedCodes() {
        val timestampCodes = listOf(
            -9223372036854775807L to "002042",
            -98765432101234L to "048802",
            -12345678901L to "370133",
            -1516980749L to "560881",
            -1234567L to "151095",
            -5000000L to "778994",
            -42L to "711601",
            0L to "135578",
            1L to "135578",
            42L to "135578",
            5000000L to "837336",
            1234567L to "220545",
            1516980749L to "936634",
            12345678901L to "979610",
            98765432101234L to "982464",
            9223372036854775807L to "794738",
        )
        val generator = TotpGenerator(
            secret = "WHGCNUJ7GAZ37EEYYC356BDOM2J5DFPF",
            config = TotpConfig(TotpPeriod.Thirty, OtpDigits.Six, HmacAlgorithm.SHA1)
        )
        for ((timestamp, expectedCode) in timestampCodes) {
            assertEquals(expectedCode, generator.generate(timestamp))
        }
    }
}