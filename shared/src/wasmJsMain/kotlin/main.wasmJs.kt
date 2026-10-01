import androidx.compose.ui.window.ComposeViewport
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import ml.dev.kotlin.openotp.OpenOtpApp
import ml.dev.kotlin.openotp.component.OpenOtpAppComponentContext
import ml.dev.kotlin.openotp.component.OpenOtpAppComponentImpl
import ml.dev.kotlin.openotp.initOpenOtpKoin
import ml.dev.kotlin.openotp.util.BiometryAuthenticator
import org.koin.compose.KoinContext
import org.koin.dsl.module

fun webOpenOtpApp() {
    initOpenOtpKoin {
        modules(module {
            single { OpenOtpAppComponentContext() }
            single { BiometryAuthenticator() }
        })
    }
    val lifecycle = LifecycleRegistry()
    val component = OpenOtpAppComponentImpl(DefaultComponentContext(lifecycle))
    lifecycle.resume()
    ComposeViewport {
        KoinContext {
            OpenOtpApp(component)
        }
    }
}
