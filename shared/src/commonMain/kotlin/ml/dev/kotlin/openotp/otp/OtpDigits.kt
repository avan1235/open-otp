package ml.dev.kotlin.openotp.otp

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import ml.dev.kotlin.openotp.util.Named

@Serializable
enum class OtpDigits(val number: Int) : Named {
    Six(6), Eight(8);

    @Composable
    override fun presentableName(): String = number.toString()
}
