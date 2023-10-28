package ml.dev.kotlin.openotp.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ml.dev.kotlin.openotp.USER_PREFERENCES_MODULE_QUALIFIER
import ml.dev.kotlin.openotp.component.OpenOtpAppComponent.Child
import ml.dev.kotlin.openotp.shared.OpenOtpResources
import ml.dev.kotlin.openotp.util.BiometryAuthenticator
import ml.dev.kotlin.openotp.util.StateFlowSettings
import org.koin.core.component.get

interface OpenOtpAppComponent {

    val theme: Value<OpenOtpAppTheme>
    val stack: Value<ChildStack<*, Child>>
    val requireAuthentication: Value<Boolean>
    val authenticated: Value<Boolean>

    fun onAuthenticate()

    fun onBackClicked(toIndex: Int)

    sealed class Child {
        class Main(val component: MainComponent) : Child()
        class ScanQRCode(val component: ScanQRCodeComponent) : Child()
        class AddProvider(
            val totpComponent: AddTotpProviderComponent,
            val hotpComponent: AddHotpProviderComponent,
        ) : Child()

        class Settings(val component: SettingsComponent) : Child()
        class LinkAccount(val component: LinkAccountComponent) : Child()
    }
}

class OpenOtpAppComponentImpl(
    componentContext: ComponentContext,
) : AbstractComponent(componentContext), OpenOtpAppComponent {

    private val navigation: StackNavigation<Config> = StackNavigation()

    override val stack: Value<ChildStack<*, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Main,
        handleBackButton = true,
        childFactory = ::child,
    )

    private val userPreferences: StateFlowSettings<UserPreferencesModel> = get(USER_PREFERENCES_MODULE_QUALIFIER)

    private val authenticator: BiometryAuthenticator = get()

    override val theme: Value<OpenOtpAppTheme> = userPreferences.stateFlow.map { it.theme }.asValue()

    override val requireAuthentication: Value<Boolean> =
        userPreferences.stateFlow.map { it.requireAuthentication }.asValue()

    private val _authenticated: MutableStateFlow<Boolean> = MutableStateFlow(!requireAuthentication.value)

    override val authenticated: Value<Boolean> = combine(
        _authenticated,
        userPreferences.stateFlow.map { it.requireAuthentication },
    ) { authenticated, require ->
        !require || authenticated
    }.asValue()

    private fun child(config: Config, childComponentContext: ComponentContext): Child = when (config) {
        is Config.Main -> Child.Main(
            MainComponentImpl(
                componentContext = childComponentContext,
                navigateOnScanQRCode = { navigation.push(Config.ScanQRCode) },
                navigateOnAddProvider = { navigation.push(Config.AddProvider) },
                navigateSettings = { navigation.push(Config.Settings) }
            )
        )

        is Config.ScanQRCode -> Child.ScanQRCode(
            ScanQRCodeComponentImpl(
                componentContext = childComponentContext,
                navigateOnCancel = { message ->
                    navigation.pop { message?.let(::toast) }
                },
            )
        )

        is Config.AddProvider -> Child.AddProvider(
            AddTotpProviderComponentImpl(
                componentContext = childComponentContext,
                navigateOnSaveClicked = navigation::pop,
                navigateOnCancelClicked = navigation::pop,
            ),
            AddHotpProviderComponentImpl(
                componentContext = childComponentContext,
                navigateOnSaveClicked = navigation::pop,
                navigateOnCancelClicked = navigation::pop,
            )
        )

        is Config.Settings -> Child.Settings(
            SettingsComponentImpl(
                componentContext = childComponentContext,
                navigateOnLinkAccount = { navigation.push(Config.LinkAccount(it)) },
                navigateOnExit = navigation::pop,
            )
        )

        is Config.LinkAccount -> Child.LinkAccount(
            LinkAccountComponentImpl(
                accountType = config.accountType,
                componentContext = childComponentContext,
                navigateOnCancel = navigation::pop,
            )
        )
    }

    override fun onAuthenticate() {
        if (!requireAuthentication.value) return

        scope.launch {
            _authenticated.value = authenticator.checkBiometryAuthentication(
                requestTitle = stringResource(OpenOtpResources.strings.authenticate_request_title),
                requestReason = stringResource(OpenOtpResources.strings.authenticate_request_description)
            )
        }
    }

    override fun onBackClicked(toIndex: Int) {
        navigation.popTo(index = toIndex)
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Main : Config

        @Serializable
        data object ScanQRCode : Config

        @Serializable
        data object AddProvider : Config

        @Serializable
        data object Settings : Config

        @Serializable
        data class LinkAccount(val accountType: UserLinkedAccountType) : Config
    }
}
