import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arkivanov.decompose.defaultComponentContext
import ml.dev.kotlin.openotp.OpenOtpApp
import ml.dev.kotlin.openotp.component.OpenOtpAppComponentContext
import ml.dev.kotlin.openotp.component.OpenOtpAppComponentImpl
import ml.dev.kotlin.openotp.initOpenOtpKoin
import ml.dev.kotlin.openotp.util.BiometryAuthenticator
import org.koin.compose.KoinContext
import org.koin.dsl.module

fun ComponentActivity.androidOpenOtpApp() {
    val activity = this@androidOpenOtpApp
    initOpenOtpKoin {
        modules(
            module {
                single { OpenOtpAppComponentContext(activity) }
                single { BiometryAuthenticator(activity.applicationContext) }
            }
        )
    }
    val component = OpenOtpAppComponentImpl(defaultComponentContext())
    setContent {
        KoinContext {
            OpenOtpApp(component)
        }
    }
}
