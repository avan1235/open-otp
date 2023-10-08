package ml.dev.kotlin.openotp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import kotlinx.serialization.builtins.ListSerializer
import ml.dev.kotlin.openotp.component.OpenOtpAppComponent
import ml.dev.kotlin.openotp.component.OpenOtpAppComponent.Child
import ml.dev.kotlin.openotp.component.UserPreferencesModel
import ml.dev.kotlin.openotp.otp.OtpData
import ml.dev.kotlin.openotp.ui.screen.*
import ml.dev.kotlin.openotp.ui.theme.OpenOtpTheme
import ml.dev.kotlin.openotp.util.BindBiometryAuthenticatorEffect
import ml.dev.kotlin.openotp.util.BiometryAuthenticator
import ml.dev.kotlin.openotp.util.OnceLaunchedEffect
import ml.dev.kotlin.openotp.util.StateFlowSettings
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

@Composable
internal fun OpenOtpApp(component: OpenOtpAppComponent) {
    OpenOtpTheme(component) {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            BindBiometryAuthenticatorEffect(koinInject<BiometryAuthenticator>())
            OnceLaunchedEffect { component.onAuthenticate() }

            val authenticated by component.authenticated.subscribeAsState()
            AuthenticationScreen(
                authenticated = authenticated,
                onAuthenticate = component::onAuthenticate,
            ) {
                Children(
                    stack = component.stack,
                    modifier = Modifier.fillMaxSize(),
                    animation = stackAnimation(slide())
                ) { child ->
                    val snackbarHostState = koinInject<SnackbarHostState>()
                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when (val instance = child.instance) {
                            is Child.Main -> MainScreen(instance.component)
                            is Child.ScanQRCode -> ScanQRCodeScreen(instance.component)
                            is Child.AddProvider -> AddProviderScreen(instance.totpComponent, instance.hotpComponent)
                            is Child.Settings -> SettingsScreen(instance.component)
                        }
                    }
                }
            }
        }
    }
}

internal val USER_OTP_CODE_DATA_MODULE_QUALIFIER: StringQualifier = named("userOtpCodeDataModule")

internal val USER_PREFERENCES_MODULE_QUALIFIER: StringQualifier = named("userPreferencesModule")


internal fun initOpenOtpKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(module {
            userOtpCodeDataModule()
            userPreferencesModule()
            snackbarHostStateModule()
        })
    }
}

private fun Module.userOtpCodeDataModule() {
    single(USER_OTP_CODE_DATA_MODULE_QUALIFIER) {
        StateFlowSettings(
            name = "user-otp-code-data",
            context = get(),
            serializer = ListSerializer(OtpData.serializer()),
            default = emptyList(),
        )
    }
}

private fun Module.userPreferencesModule() {
    single(USER_PREFERENCES_MODULE_QUALIFIER) {
        StateFlowSettings(
            name = "user-preferences",
            context = get(),
            serializer = UserPreferencesModel.serializer(),
            default = UserPreferencesModel(),
        )
    }
}

private fun Module.snackbarHostStateModule() {
    single { SnackbarHostState() }
}
