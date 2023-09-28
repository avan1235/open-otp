package ml.dev.kotlin.openotp.qr

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun QRCodeScanner(
    onResult: (QRResult) -> Boolean,
    innerPadding: PaddingValues,
    onIsLoadingChange: (Boolean) -> Unit,
) {
}

@Composable
actual fun rememberCameraPermissionState(): CameraPermissionState = remember {
    object : CameraPermissionState {
        override val isAvailable: Boolean = false
        override val permission: CameraPermission = CameraPermission.Denied
        override fun launchRequest() = Unit
    }
}
