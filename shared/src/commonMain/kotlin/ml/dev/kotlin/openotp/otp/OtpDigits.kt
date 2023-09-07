package ml.dev.kotlin.openotp.otp

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ml.dev.kotlin.openotp.util.Named

@Serializable
enum class OtpDigits(val number: Int) : Named {
    Six(6), Eight(8);

    @Transient
    override val presentableName: String = number.toString()
}



