package ml.dev.kotlin.openotp.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import ml.dev.kotlin.openotp.USER_PREFERENCES_MODULE_QUALIFIER
import ml.dev.kotlin.openotp.util.ValueSettings
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

    private val userPreferences: ValueSettings<UserPreferencesModel> = get(USER_PREFERENCES_MODULE_QUALIFIER)

    override val theme: Value<OpenOtpAppTheme> =
        userPreferences.value.map { it.theme }

    override val confirmOtpDataDelete: Value<Boolean> =
        userPreferences.value.map { it.confirmOtpDataDelete }

    override fun onSelectedTheme(theme: OpenOtpAppTheme) {
        val preferences = userPreferences.get()
        val updated = preferences.copy(theme = theme)
        userPreferences.set(updated)
    }

    override fun onConfirmOtpDataDeleteChange(confirm: Boolean) {
        val preferences = userPreferences.get()
        val updated = preferences.copy(confirmOtpDataDelete = confirm)
        userPreferences.set(updated)
    }

    override fun onExitSettings() {
        navigateOnExit()
    }
}
