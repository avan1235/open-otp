package ml.dev.kotlin.openotp.qr

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable

@Composable
expect fun QRCodeScanner(
    onResult: (QRResult) -> Boolean,
    innerPadding: PaddingValues,
    onIsLoadingChange: (Boolean) -> Unit,
)

@Composable
expect fun rememberCameraPermissionState(): CameraPermissionState?

interface CameraPermissionState {
    val permission: CameraPermission

    fun launchRequest()
}

enum class CameraPermission {
    Granted, Denied;

    val isGranted: Boolean get() = this == Granted
}

sealed interface QRResult {

    data class QRSuccess(val contents: List<String>) : QRResult

    data class QRError(val exception: Exception) : QRResult
}
