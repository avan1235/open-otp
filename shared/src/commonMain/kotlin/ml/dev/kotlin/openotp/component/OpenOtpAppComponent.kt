package ml.dev.kotlin.openotp.component

import kotlinx.serialization.Serializable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import ml.dev.kotlin.openotp.component.OpenOtpAppComponent.Child

interface OpenOtpAppComponent {

    val stack: Value<ChildStack<*, Child>>

    fun onBackClicked(toIndex: Int)

    sealed class Child {
        class Main(val component: MainComponent) : Child()
        class ScanQRCode(val component: ScanQRCodeComponent) : Child()
        class AddProvider(
            val totpComponent: AddTotpProviderComponent,
            val hotpComponent: AddHotpProviderComponent,
        ) : Child()
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

    private fun child(config: Config, childComponentContext: ComponentContext): Child = when (config) {
        is Config.Main -> Child.Main(
            MainComponentImpl(
                componentContext = childComponentContext,
                navigateOnScanQRCode = { navigation.push(Config.ScanQRCode) },
                navigateOnAddProvider = { navigation.push(Config.AddProvider) },
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
    }
}
