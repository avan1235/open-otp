package ml.dev.kotlin.openotp.otp

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import ml.dev.kotlin.openotp.util.Named

@Serializable
enum class HmacAlgorithm : Named {
    SHA1, SHA256, SHA512;

    @Composable
    override fun presentableName(): String = this@HmacAlgorithm.name
}
