package ml.dev.kotlin.openotp.qr

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.github.eduramiba.webcamcapture.drivers.NativeDriver
import com.github.sarxos.webcam.Webcam
import com.github.sarxos.webcam.WebcamException
import com.github.sarxos.webcam.WebcamResolution
import com.google.zxing.BinaryBitmap
import com.google.zxing.NotFoundException
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.qrcode.QRCodeMultiReader
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import ml.dev.kotlin.openotp.qr.CameraPermission.Denied
import ml.dev.kotlin.openotp.qr.CameraPermission.Granted
import ml.dev.kotlin.openotp.shared.OpenOtpResources
import ml.dev.kotlin.openotp.util.OnceLaunchedEffect
import ml.dev.kotlin.openotp.util.runIfNonNull
import java.awt.image.BufferedImage
import java.lang.System.currentTimeMillis

@Composable
actual fun QRCodeScanner(
    onResult: (QRResult) -> Boolean,
    isLoading: MutableState<Boolean>,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val webcam = remember { Webcam.getDefault()?.apply { setViewSize(WebcamResolution.VGA.size) } }
        if (webcam == null) {
            onResult(QRResult.QRError)
            return
        }
        DisposableEffect(webcam) {
            try {
                webcam.open()
            } catch (_: WebcamException) {
            }
            onDispose { webcam.close() }
        }
        val qrCodeReader = remember { QRCodeMultiReader() }
        val imageBitmap = remember { mutableStateOf<ImageBitmap?>(null) }

        analyzeCameraImage(webcam, qrCodeReader, isLoading, imageBitmap, onResult)

        val bitmap = imageBitmap.value
        isLoading.value = bitmap == null

        if (bitmap != null) Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Image(
                bitmap = bitmap,
                contentDescription = stringResource(OpenOtpResources.strings.camara_image_name),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

}

@Composable
actual fun rememberCameraPermissionState(): CameraPermissionState = remember {
    object : CameraPermissionState {
        init {
            val os = System.getProperty("os.name")
            if (os.contains("mac", ignoreCase = true)) {
                Webcam.setDriver(NativeDriver())
            }
        }

        override val isAvailable: Boolean by lazy {
            try {
                runIfNonNull(Webcam.getDefault()) { true } ?: false
            } catch (_: WebcamException) {
                false
            }
        }

        override val permission: CameraPermission
            get() = if (isAvailable) Granted else Denied

        override fun launchRequest() = Unit
    }
}

@Composable
private fun analyzeCameraImage(
    webcam: Webcam,
    qrCodeReader: QRCodeMultiReader,
    isLoading: MutableState<Boolean>,
    imageBitmap: MutableState<ImageBitmap?>,
    onResult: (QRResult) -> Boolean,
) {
    OnceLaunchedEffect {
        while (isActive) {
            val isWebcamOpen = webcam.isOpen
            if (!isWebcamOpen) {
                isLoading.value = true
                delay(CAPTURE_IMAGE_DELAY_MILLIS)
                continue
            }

            val bufferedImage = webcam.image
            if (bufferedImage == null) {
                isLoading.value = true
                delay(CAPTURE_IMAGE_DELAY_MILLIS)
                continue
            }

            var handleNext = false
            val beforeMillis = currentTimeMillis()

            awaitAll(
                async { handleNext = qrCodeReader.readQR(bufferedImage, onResult) },
                async { imageBitmap.value = bufferedImage.toComposeImageBitmap() }
            )
            if (!handleNext) break

            val diff = currentTimeMillis() - beforeMillis
            delay(CAPTURE_IMAGE_DELAY_MILLIS - diff)
        }
    }
}

private fun QRCodeMultiReader.readQR(
    image: BufferedImage,
    onResult: (QRResult) -> Boolean,
): Boolean {
    val binaryBitmap = BinaryBitmap(HybridBinarizer(BufferedImageLuminanceSource(image)))
    val result = try {
        val results = decodeMultiple(binaryBitmap)
        QRResult.QRSuccess(results) { it?.text } ?: return true
    } catch (_: NotFoundException) {
        return true
    }
    return onResult(result)
}

private const val CAPTURE_IMAGE_DELAY_MILLIS: Long = 50
