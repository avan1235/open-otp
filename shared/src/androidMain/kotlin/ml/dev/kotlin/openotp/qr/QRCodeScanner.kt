package ml.dev.kotlin.openotp.qr

import android.widget.LinearLayout.LayoutParams
import androidx.appcompat.widget.ListPopupWindow
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
actual fun QRCodeScanner(
    onResult: (QRResult) -> Boolean,
    innerPadding: PaddingValues,
    onIsLoadingChange: (Boolean) -> Unit,
) {
    val localContext = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val analysisExecutor = rememberExecutor()
    val cameraController = remember { LifecycleCameraController(localContext) }
    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        factory = { context ->
            PreviewView(context).apply {
                setBackgroundColor(backgroundColor.toArgb())

                layoutParams = LayoutParams(ListPopupWindow.MATCH_PARENT, ListPopupWindow.MATCH_PARENT)
                scaleType = PreviewView.ScaleType.FILL_START
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                controller = cameraController

                var handleNext = true

                cameraController.setImageAnalysisAnalyzer(
                    analysisExecutor,
                    QRCodeAnalyzer(
                        handle = { handleNext = handleNext && onResult(it) },
                        onPassCompleted = { failureOccurred -> onIsLoadingChange(failureOccurred) },
                    )
                )

                cameraController.bindToLifecycle(lifecycleOwner)
            }
        },
        onRelease = {
            cameraController.unbind()
            analysisExecutor.shutdown()
        }
    )
}

@Composable
actual fun rememberCameraPermissionState(): CameraPermissionState {
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )
    return remember {
        object : CameraPermissionState {
            override val isAvailable: Boolean = true
            override val permission: CameraPermission
                get() = when (cameraPermissionState.status) {
                    PermissionStatus.Granted -> CameraPermission.Granted
                    is PermissionStatus.Denied -> CameraPermission.Denied
                }

            override fun launchRequest() = cameraPermissionState.launchPermissionRequest()
        }
    }
}

@Composable
private fun rememberExecutor(): ExecutorService {
    val executor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
        }
    }
    return executor
}
