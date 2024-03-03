import ml.dev.kotlin.openotp.component.OpenOtpAppComponentContext
import ml.dev.kotlin.openotp.initOpenOtpKoin
import ml.dev.kotlin.openotp.util.BiometryAuthenticator
import org.koin.dsl.module

@Suppress("UNUSED")
fun initIOSKoin() {
    initOpenOtpKoin {
        modules(module {
            single { OpenOtpAppComponentContext() }
            single { BiometryAuthenticator() }
        })
    }
}
