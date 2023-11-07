package ml.dev.kotlin.openotp.component

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import ml.dev.kotlin.openotp.USER_PREFERENCES_MODULE_QUALIFIER
import ml.dev.kotlin.openotp.component.SettingsComponentImpl.LinkedAccountState
import ml.dev.kotlin.openotp.shared.OpenOtpResources
import ml.dev.kotlin.openotp.util.BiometryAuthenticator
import ml.dev.kotlin.openotp.util.Named
import ml.dev.kotlin.openotp.util.StateFlowSettings
import org.koin.core.component.get

interface SettingsComponent {

    val theme: Value<OpenOtpAppTheme>
    val confirmOtpDataDelete: Value<Boolean>
    val sortOtpDataBy: Value<SortOtpDataBy>
    val canReorderDataManually: Value<Boolean>
    val sortOtpDataNullsFirst: Value<Boolean>
    val sortOtpDataReversed: Value<Boolean>
    val showSortedGroupsHeaders: Value<Boolean>
    val requireAuthentication: Value<Boolean>
    val isAuthenticationAvailable: Boolean
    val linkedAccountsStates: Value<List<LinkedAccountState>>

    fun onSelectedTheme(theme: OpenOtpAppTheme)

    fun onConfirmOtpDataDeleteChange(confirm: Boolean)

    fun onSelectedSortType(sort: SortOtpDataBy)

    fun onSortNullsFirstChange(nullsFirst: Boolean)

    fun onSortReversedChange(reversed: Boolean)

    fun onShowSortedGroupsHeadersChange(show: Boolean)

    fun onRequireAuthenticationChange(require: Boolean)

    fun onExitSettings()
}

class SettingsComponentImpl(
    componentContext: ComponentContext,
    private val navigateOnLinkAccount: (UserLinkedAccountType) -> Unit,
    private val navigateOnExit: () -> Unit,
) : AbstractBackupComponent(componentContext), SettingsComponent {

    private val userPreferences: StateFlowSettings<UserPreferencesModel> =
        get(USER_PREFERENCES_MODULE_QUALIFIER)

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

    override val showSortedGroupsHeaders: Value<Boolean> =
        userPreferences.stateFlow.map { it.showSortedGroupsHeaders }.asValue()

    override val requireAuthentication: Value<Boolean> =
        userPreferences.stateFlow.map { it.requireAuthentication }.asValue()

    override val isAuthenticationAvailable: Boolean
        get() = authenticator.isBiometricAvailable()

    override val linkedAccountsStates: Value<List<LinkedAccountState>> = _userLinkedAccounts
        .stateFlow
        .map { linkedAccounts ->
            UserLinkedAccountType.entries.map { accountType ->
                if (accountType.isLinked(linkedAccounts)) Linked(accountType) else Unlinked(accountType)
            }
        }
        .asValue()

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

    override fun onShowSortedGroupsHeadersChange(show: Boolean) {
        userPreferences.updateInScope { it.copy(showSortedGroupsHeaders = show) }
    }

    override fun onRequireAuthenticationChange(require: Boolean) {
        userPreferences.updateInScope { it.copy(requireAuthentication = require) }
    }

    override fun onExitSettings() {
        navigateOnExit()
    }

    sealed class LinkedAccountState(protected val accountType: UserLinkedAccountType) : Named {
        val icon = accountType.icon

        @Composable
        fun iconContentDescription(): String = accountType.iconContentDescription()

        abstract fun onClick()
    }

    inner class Linked(type: UserLinkedAccountType) : LinkedAccountState(type) {
        override val OpenOtpAppComponentContext.presentableName: String
            get() = stringResource(OpenOtpResources.strings.unlink_account_button_name)

        override fun onClick() {
            _userLinkedAccounts.updateInScope { accountType.reset(it) }
        }
    }

    inner class Unlinked(type: UserLinkedAccountType) : LinkedAccountState(type) {
        override val OpenOtpAppComponentContext.presentableName: String
            get() = stringResource(OpenOtpResources.strings.link_account_button_name)

        override fun onClick() {
            _userLinkedAccounts
                .updateInScope { accountType.reset(it) }
                .invokeOnCompletion {
                    navigateOnLinkAccount(accountType)
                }
        }
    }
}
