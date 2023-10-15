package ml.dev.kotlin.openotp.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import dev.icerock.moko.resources.compose.stringResource
import `in`.procyk.compose.camera.qr.QRCodeScanner
import ml.dev.kotlin.openotp.component.ScanQRCodeComponent
import ml.dev.kotlin.openotp.shared.OpenOtpResources
import ml.dev.kotlin.openotp.ui.component.ClickableIconButton
import ml.dev.kotlin.openotp.ui.component.LoadingAnimatedVisibility
import ml.dev.kotlin.openotp.ui.theme.Typography

@Composable
internal fun ScanQRCodeScreen(
    scanQRCodeComponent: ScanQRCodeComponent,
    holePercent: Float = 0.75f,
) {
    NoSystemBarsScreen {
        var isLoading by remember { mutableStateOf(true) }

        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            LoadingAnimatedVisibility(
                visibleContent = !isLoading
            ) {
                QRCodeScanner(
                    onResult = scanQRCodeComponent::onQRCodeScanned,
                    onIsLoadingChange = { isLoading = it },
                    backgroundColor = MaterialTheme.colorScheme.background,
                    contentDescription = stringResource(OpenOtpResources.strings.camera_image_name),
                    missingCameraContent = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(OpenOtpResources.strings.camera_not_available),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                )
                QRCodeCameraHole(holePercent)
                ScanQRCodeScreenDescription(
                    holePercent = holePercent,
                    onCancel = scanQRCodeComponent::onCancelClick,
                )
            }
        }
    }
}

@Composable
private fun CancelScanQRCodeButton(onCancel: () -> Unit) {
    val cancelText = stringResource(OpenOtpResources.strings.cancel_button_name)
    ClickableIconButton(
        onClick = onCancel,
        modifier = Modifier.size(44.dp),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = contentColorFor(MaterialTheme.colorScheme.background),
    ) {
        Icon(
            imageVector = Icons.Outlined.Close,
            contentDescription = cancelText,
            modifier = Modifier.padding(8.dp).fillMaxSize()
        )
    }
}

@Composable
private fun ScanQRCodeScreenDescription(
    holePercent: Float,
    onCancel: () -> Unit,
    buttons: @Composable BoxWithConstraintsScope.() -> Unit = {
        CancelScanQRCodeButton(onCancel)
    },
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val cameraSize = min(maxHeight, maxWidth) * holePercent
        if (maxHeight >= maxWidth) Column {
            val modifier = Modifier.weight(1f).fillMaxSize()
            Box(modifier, Alignment.Center) { ScanQRCodeIcon() }
            Box(Modifier.height(cameraSize).fillMaxWidth())
            BoxWithConstraints(modifier, Alignment.Center, false, buttons)
        } else Row {
            val modifier = Modifier.weight(1f).fillMaxSize()
            Box(modifier, Alignment.Center) { ScanQRCodeIcon() }
            Box(Modifier.width(cameraSize).fillMaxHeight())
            BoxWithConstraints(modifier, Alignment.Center, false, buttons)
        }
    }
}

@Composable
private fun QRCodeCameraHole(
    holePercent: Float,
    border: Dp = 4.dp,
    backgroundAlpha: Float = 0.8f,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
) {
    Canvas(
        modifier = Modifier.fillMaxSize(),
        onDraw = {
            val holeSize = size.minDimension * holePercent
            val borderPx = border.roundToPx()
            val borderSize = holeSize + 2 * borderPx
            val halfBorderSize = borderSize / 2f
            clipPath(
                path = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(center, holeSize / 2f),
                            cornerRadius = CornerRadius(holeSize / 20f)
                        )
                    )
                },
                clipOp = ClipOp.Difference
            ) {
                drawRect(
                    color = backgroundColor,
                    alpha = backgroundAlpha,
                )
                drawRoundRect(
                    color = backgroundColor,
                    size = Size(borderSize, borderSize),
                    topLeft = center.minus(Offset(halfBorderSize, halfBorderSize)),
                    cornerRadius = CornerRadius(borderSize / 20f)
                )
            }
        })
}

@Composable
private fun ScanQRCodeIcon() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val text = stringResource(OpenOtpResources.strings.scan_qr_code)
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = text,
            modifier = Modifier
                .padding(12.dp)
                .size(48.dp)
        )
        Text(text, style = Typography.displayMedium)
    }
}
