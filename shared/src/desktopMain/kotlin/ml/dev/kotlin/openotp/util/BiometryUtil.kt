package ml.dev.kotlin.openotp.util

import androidx.compose.runtime.Composable

actual class BiometryAuthenticator {
    actual suspend fun checkBiometryAuthentication(
        requestTitle: String,
        requestReason: String,
    ): Boolean = true

    actual fun isBiometricAvailable(): Boolean = false
}

@Composable
actual fun BindBiometryAuthenticatorEffect(biometryAuthenticator: BiometryAuthenticator) = Unit
