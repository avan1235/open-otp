import androidx.compose.ui.window.ComposeUIViewController
import ml.dev.kotlin.openotp.OpenOtpApp
import ml.dev.kotlin.openotp.component.OpenOtpAppComponent
import org.koin.compose.KoinContext
import platform.UIKit.UIViewController

fun MainViewController(component: OpenOtpAppComponent): UIViewController = ComposeUIViewController() {
    KoinContext {
        OpenOtpApp(component)
//        CameraView()
    }
}