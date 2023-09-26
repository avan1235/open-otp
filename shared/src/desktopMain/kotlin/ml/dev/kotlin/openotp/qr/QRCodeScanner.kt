package ml.dev.kotlin.openotp.qr

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable

@Composable
actual fun QRCodeScanner(
    onResult: (QRResult) -> Boolean,
    innerPadding: PaddingValues,
    onIsLoadingChange: (Boolean) -> Unit,
) {
}

@Composable
actual fun rememberCameraPermissionState(): CameraPermissionState? {
    return null
}
