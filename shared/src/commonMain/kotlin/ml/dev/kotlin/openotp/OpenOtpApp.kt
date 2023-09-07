package ml.dev.kotlin.openotp

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import kotlinx.serialization.builtins.ListSerializer
import ml.dev.kotlin.openotp.component.OpenOtpAppComponent
import ml.dev.kotlin.openotp.component.OpenOtpAppComponent.Child
import ml.dev.kotlin.openotp.otp.OtpData
import ml.dev.kotlin.openotp.ui.screen.AddProviderScreen
import ml.dev.kotlin.openotp.ui.screen.MainScreen
import ml.dev.kotlin.openotp.ui.theme.OpenOtpTheme
import ml.dev.kotlin.openotp.util.ValueSettings
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

@Composable
internal fun OpenOtpApp(component: OpenOtpAppComponent) {
    OpenOtpTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars),
        ) {
            Children(
                stack = component.stack,
                modifier = Modifier.fillMaxSize(),
                animation = stackAnimation(slide())
            ) { child ->
                val snackbarHostState = koinInject<SnackbarHostState>()
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                ) {
                    when (val instance = child.instance) {
                        is Child.Main -> MainScreen(mainComponent = instance.component)
                        is Child.AddProvider -> AddProviderScreen(instance.totpComponent, instance.hotpComponent)
                    }
                }
            }
        }
    }
}

internal val USER_OTP_CODE_DATA_MODULE_QUALIFIER: StringQualifier = named("userOtpCodeDataModule")

internal fun initOpenOtpKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(module {
            userOtpCodeDataModule()
        })
    }
}

private fun Module.userOtpCodeDataModule() {
    single(USER_OTP_CODE_DATA_MODULE_QUALIFIER) {
        ValueSettings(
            name = "user-otp-code-data",
            context = get(),
            serializer = ListSerializer(OtpData.serializer()),
            default = emptyList(),
        )
    }
}
