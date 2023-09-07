package ml.dev.kotlin.openotp.qr

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import io.github.g00fy2.quickie.ScanCustomCode
import io.github.g00fy2.quickie.config.ScannerConfig
import io.github.g00fy2.quickie.QRResult as LibQRResult

@Composable
actual fun rememberQRCodeScanner(resultHandler: (QRResult) -> Unit): (() -> Unit)? {
    val scanQrCodeLauncher = rememberLauncherForActivityResult(ScanCustomCode()) {
        val result = when (it) {
            is LibQRResult.QRError -> QRResult.QRError(it.exception)
            LibQRResult.QRMissingPermission -> QRResult.QRMissingPermission
            is LibQRResult.QRSuccess -> QRResult.QRSuccess(it.content.rawValue)
            LibQRResult.QRUserCanceled -> QRResult.QRUserCanceled
        }
        resultHandler(result)
    }
    return {
        scanQrCodeLauncher.launch(
            ScannerConfig.build {
                setHapticSuccessFeedback(true)
                setHorizontalFrameRatio(1f)
                setShowCloseButton(true)
                setShowTorchToggle(true)
            }
        )
    }
}
