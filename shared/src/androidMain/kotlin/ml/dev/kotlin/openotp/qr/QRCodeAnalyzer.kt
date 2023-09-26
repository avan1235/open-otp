package ml.dev.kotlin.openotp.qr


import android.media.Image
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.lang.System.currentTimeMillis

internal class QRCodeAnalyzer(
    private val handle: (QRResult) -> Unit,
    private val onPassCompleted: (failureOccurred: Boolean) -> Unit,
) : ImageAnalysis.Analyzer {

    private val barcodeScanner: BarcodeScanner? by lazy {
        try {
            BarcodeScanning.getClient(
                BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build()
            )
        } catch (e: Exception) {
            handleSynchronized(QRResult.QRError(e))
            null
        }
    }

    @Volatile
    private var failureTimestamp: Long = NO_FAILURE_FLAG

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val image = imageProxy.image ?: return

        if (failureTimestamp.isFailure && currentTimeMillis() - failureTimestamp < FAILURE_THROTTLE_MILLIS) {
            return imageProxy.close()
        }
        failureTimestamp = NO_FAILURE_FLAG

        val scanner = barcodeScanner ?: return
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        scanner
            .process(image.toInputImage(rotationDegrees))
            .addOnSuccessListener { onNonEmptySuccess(it) }
            .addOnFailureListener {
                failureTimestamp = currentTimeMillis()
                handleSynchronized(QRResult.QRError(it))
            }
            .addOnCompleteListener {
                onPassCompleted(failureTimestamp.isFailure)
                imageProxy.close()
            }
    }

    @Synchronized
    private fun handleSynchronized(result: QRResult) {
        handle(result)
    }

    private fun onNonEmptySuccess(codes: List<Barcode?>?) {
        val someCodes = codes?.mapNotNull { it?.rawValue }?.takeIf { it.isNotEmpty() } ?: return
        handleSynchronized(QRResult.QRSuccess(someCodes))
    }
}

private const val FAILURE_THROTTLE_MILLIS: Long = 1_000L

private const val NO_FAILURE_FLAG: Long = Long.MIN_VALUE

private inline val Long.isFailure: Boolean get() = this != NO_FAILURE_FLAG

@ExperimentalGetImage
private fun Image.toInputImage(rotationDegrees: Int): InputImage =
    InputImage.fromMediaImage(this, rotationDegrees)
