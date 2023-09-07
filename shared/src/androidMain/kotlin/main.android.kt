import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.SnackbarHostState
import com.arkivanov.decompose.defaultComponentContext
import ml.dev.kotlin.openotp.OpenOtpApp
import ml.dev.kotlin.openotp.component.OpenOtpAppComponentContext
import ml.dev.kotlin.openotp.component.OpenOtpAppComponentImpl
import ml.dev.kotlin.openotp.initOpenOtpKoin
import org.koin.compose.KoinContext
import org.koin.dsl.module

fun ComponentActivity.androidOpenOtpApp() {
    initOpenOtpKoin {
        modules(
            module {
                single { OpenOtpAppComponentContext(this@androidOpenOtpApp) }
                single { SnackbarHostState() }
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
