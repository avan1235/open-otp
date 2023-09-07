package ml.dev.kotlin.openotp.otp

import kotlinx.serialization.Serializable

@Serializable
class HotpConfig(
    val codeDigits: OtpDigits,
    val hmacAlgorithm: HmacAlgorithm,
) {
    companion object {
        val DEFAULT: HotpConfig get() = HotpConfig(OtpDigits.Six, HmacAlgorithm.SHA1)
    }
}

@Serializable
class TotpConfig(
    val period: TotpPeriod,
    val codeDigits: OtpDigits,
    val hmacAlgorithm: HmacAlgorithm,
) {
    companion object {
        val DEFAULT: TotpConfig get() = TotpConfig(TotpPeriod.Thirty, OtpDigits.Six, HmacAlgorithm.SHA1)
    }
}

fun TotpConfig.toHotpConfig(): HotpConfig = HotpConfig(codeDigits, hmacAlgorithm)
