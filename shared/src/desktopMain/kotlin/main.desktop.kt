import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.jetbrains.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import ml.dev.kotlin.openotp.OpenOtpApp
import ml.dev.kotlin.openotp.component.OpenOtpAppComponentContext
import ml.dev.kotlin.openotp.component.OpenOtpAppComponentImpl
import ml.dev.kotlin.openotp.initOpenOtpKoin
import ml.dev.kotlin.openotp.util.BiometryAuthenticator
import ml.dev.kotlin.openotp.util.runOnUiThread
import org.koin.compose.KoinContext
import org.koin.dsl.module

fun desktopOpenOtpApp() {
    initOpenOtpKoin {
        modules(module {
            single { OpenOtpAppComponentContext() }
            single { BiometryAuthenticator() }
        })
    }
    val lifecycle = LifecycleRegistry()
    val component = runOnUiThread {
        OpenOtpAppComponentImpl(DefaultComponentContext(lifecycle))
    }
    application {
        KoinContext {
            val windowState = rememberWindowState(
                height = DEFAULT_DESKTOP_SIZE.height,
                width = DEFAULT_DESKTOP_SIZE.width,
            )

            LifecycleController(lifecycle, windowState)

            Window(
                title = "OpenOTP",
                onCloseRequest = ::exitApplication,
                state = windowState,
                icon = BitmapPainter(useResource("ic_launcher.png", ::loadImageBitmap)),
            ) {
                OpenOtpApp(component)
            }
        }
    }
}

private val DEFAULT_DESKTOP_SIZE: DpSize = DpSize(560.dp, 800.dp)
