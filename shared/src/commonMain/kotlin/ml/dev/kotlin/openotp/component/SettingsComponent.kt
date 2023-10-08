package ml.dev.kotlin.openotp.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import ml.dev.kotlin.openotp.USER_PREFERENCES_MODULE_QUALIFIER
import ml.dev.kotlin.openotp.util.BiometryAuthenticator
import ml.dev.kotlin.openotp.util.StateFlowSettings
import org.koin.core.component.get

interface SettingsComponent {

    val theme: Value<OpenOtpAppTheme>
    val confirmOtpDataDelete: Value<Boolean>
    val sortOtpDataBy: Value<SortOtpDataBy>
    val canReorderDataManually: Value<Boolean>
    val sortOtpDataNullsFirst: Value<Boolean>
    val sortOtpDataReversed: Value<Boolean>
    val requireAuthentication: Value<Boolean>
    val isAuthenticationAvailable: Boolean

    fun onSelectedTheme(theme: OpenOtpAppTheme)

    fun onConfirmOtpDataDeleteChange(confirm: Boolean)

    fun onSelectedSortType(sort: SortOtpDataBy)

    fun onSortNullsFirstChange(nullsFirst: Boolean)

    fun onSortReversedChange(reversed: Boolean)

    fun onRequireAuthenticationChange(require: Boolean)

    fun onExitSettings()
}

class SettingsComponentImpl(
    componentContext: ComponentContext,
    private val navigateOnExit: () -> Unit,
) : AbstractComponent(componentContext), SettingsComponent {

    private val userPreferences: StateFlowSettings<UserPreferencesModel> = get(USER_PREFERENCES_MODULE_QUALIFIER)

    private val authenticator: BiometryAuthenticator = get()

    override val theme: Value<OpenOtpAppTheme> =
        userPreferences.stateFlow.map { it.theme }.asValue()

    override val confirmOtpDataDelete: Value<Boolean> =
        userPreferences.stateFlow.map { it.confirmOtpDataDelete }.asValue()

    override val sortOtpDataBy: Value<SortOtpDataBy> =
        userPreferences.stateFlow.map { it.sortOtpDataBy }.asValue()

    override val canReorderDataManually: Value<Boolean> =
        userPreferences.stateFlow.map { it.sortOtpDataBy == SortOtpDataBy.Dont }.asValue()

    override val sortOtpDataNullsFirst: Value<Boolean> =
        userPreferences.stateFlow.map { it.sortOtpDataNullsFirst }.asValue()

    override val sortOtpDataReversed: Value<Boolean> =
        userPreferences.stateFlow.map { it.sortOtpDataReversed }.asValue()

    override val requireAuthentication: Value<Boolean> =
        userPreferences.stateFlow.map { it.requireAuthentication }.asValue()

    override val isAuthenticationAvailable: Boolean
        get() = authenticator.isBiometricAvailable()

    override fun onSelectedTheme(theme: OpenOtpAppTheme) {
        userPreferences.updateInScope { it.copy(theme = theme) }
    }

    override fun onConfirmOtpDataDeleteChange(confirm: Boolean) {
        userPreferences.updateInScope { it.copy(confirmOtpDataDelete = confirm) }
    }

    override fun onSelectedSortType(sort: SortOtpDataBy) {
        userPreferences.updateInScope { it.copy(sortOtpDataBy = sort) }
    }

    override fun onSortNullsFirstChange(nullsFirst: Boolean) {
        userPreferences.updateInScope { it.copy(sortOtpDataNullsFirst = nullsFirst) }
    }

    override fun onSortReversedChange(reversed: Boolean) {
        userPreferences.updateInScope { it.copy(sortOtpDataReversed = reversed) }
    }

    override fun onRequireAuthenticationChange(require: Boolean) {
        userPreferences.updateInScope { it.copy(requireAuthentication = require) }
    }

    override fun onExitSettings() {
        navigateOnExit()
    }
}
