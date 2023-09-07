package ml.dev.kotlin.openotp.otp

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ml.dev.kotlin.openotp.util.Named

@Serializable
enum class OtpType : Named {
    TOTP, HOTP;

    @Transient
    override val presentableName: String by lazy {
        when (this) {
            TOTP -> "TOTP - Time Based"
            HOTP -> "HOTP - Counter Based"
        }
    }
}
