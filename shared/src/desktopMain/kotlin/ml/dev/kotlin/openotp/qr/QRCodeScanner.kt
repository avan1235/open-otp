package ml.dev.kotlin.openotp.qr

import androidx.compose.runtime.Composable

@Composable
actual fun rememberQRCodeScanner(resultHandler: (QRResult) -> Unit): (() -> Unit)? = null
