package ml.dev.kotlin.openotp.qr

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ml.dev.kotlin.openotp.qr.CameraPermission.Denied

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
        override val permission: CameraPermission = Denied
        override fun launchRequest() = Unit
    }
}
