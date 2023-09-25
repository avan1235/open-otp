package ml.dev.kotlin.openotp.otp

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ml.dev.kotlin.openotp.component.OpenOtpAppComponentContext
import ml.dev.kotlin.openotp.util.Named

@Serializable
enum class HmacAlgorithm : Named {
    SHA1, SHA256, SHA512;

    @Transient
    override val OpenOtpAppComponentContext.presentableName: String
        get() = this@HmacAlgorithm.name
}