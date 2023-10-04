package ml.dev.kotlin.openotp.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import ml.dev.kotlin.openotp.USER_PREFERENCES_MODULE_QUALIFIER
import ml.dev.kotlin.openotp.util.StateFlowSettings
import org.koin.core.component.get

interface SettingsComponent {

    val theme: Value<OpenOtpAppTheme>

    val confirmOtpDataDelete: Value<Boolean>

    fun onSelectedTheme(theme: OpenOtpAppTheme)

    fun onConfirmOtpDataDeleteChange(confirm: Boolean)

    fun onExitSettings()
}

class SettingsComponentImpl(
    componentContext: ComponentContext,
    private val navigateOnExit: () -> Unit,
) : AbstractComponent(componentContext), SettingsComponent {

    private val userPreferences: StateFlowSettings<UserPreferencesModel> = get(USER_PREFERENCES_MODULE_QUALIFIER)

    override val theme: Value<OpenOtpAppTheme> =
        userPreferences.stateFlow.map { it.theme }.asValue()

    override val confirmOtpDataDelete: Value<Boolean> =
        userPreferences.stateFlow.map { it.confirmOtpDataDelete }.asValue()

    override fun onSelectedTheme(theme: OpenOtpAppTheme) {
        userPreferences.updateInScope { it.copy(theme = theme) }
    }

    override fun onConfirmOtpDataDeleteChange(confirm: Boolean) {
        userPreferences.updateInScope { it.copy(confirmOtpDataDelete = confirm) }
    }

    override fun onExitSettings() {
        navigateOnExit()
    }
}
