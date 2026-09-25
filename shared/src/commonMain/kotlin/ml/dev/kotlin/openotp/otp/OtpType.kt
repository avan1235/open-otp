package ml.dev.kotlin.openotp.otp

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import ml.dev.kotlin.openotp.shared.*
import ml.dev.kotlin.openotp.util.Named
import org.jetbrains.compose.resources.stringResource

@Serializable
enum class OtpType : Named {
    TOTP, HOTP;

    @Composable
    override fun presentableName(): String = when (this@OtpType) {
        TOTP -> stringResource(Res.string.totp_presentation)
        HOTP -> stringResource(Res.string.hotp_presentation)
    }
}
