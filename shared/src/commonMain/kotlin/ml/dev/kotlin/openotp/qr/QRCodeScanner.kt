package ml.dev.kotlin.openotp.qr

import androidx.compose.runtime.Composable

@Composable
expect fun rememberQRCodeScanner(resultHandler: (QRResult) -> Unit): (() -> Unit)?

sealed interface QRResult {

    data class QRSuccess(val content: String?) : QRResult

    data object QRUserCanceled : QRResult

    data object QRMissingPermission : QRResult

    data class QRError(val exception: Exception) : QRResult
}
