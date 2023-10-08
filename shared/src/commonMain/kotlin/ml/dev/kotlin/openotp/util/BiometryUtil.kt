package ml.dev.kotlin.openotp.util

import androidx.compose.runtime.Composable

expect class BiometryAuthenticator {
    suspend fun checkBiometryAuthentication(
        requestTitle: String,
        requestReason: String,
    ): Boolean

    fun isBiometricAvailable(): Boolean
}

@Composable
expect fun BindBiometryAuthenticatorEffect(biometryAuthenticator: BiometryAuthenticator)
