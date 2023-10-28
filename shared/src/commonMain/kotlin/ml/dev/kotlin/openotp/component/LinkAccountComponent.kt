package ml.dev.kotlin.openotp.component

import androidx.compose.ui.platform.UriHandler
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ml.dev.kotlin.openotp.USER_LINKED_ACCOUNTS_MODULE_QUALIFIER
import ml.dev.kotlin.openotp.backup.OAuth2AccountService
import ml.dev.kotlin.openotp.util.StateFlowSettings
import org.koin.core.component.get

interface LinkAccountComponent {

    val code: Value<String>
    val shouldEnterCode: Value<Boolean>
    val isLoadingAppPermissions: Value<Boolean>
    val isErrorGettingToken: Value<Boolean>

    fun onRequestAppPermissions(uriHandler: UriHandler)
    fun onUserAccessCodeChanged(code: String)
    fun onUserAccessCodeConfirmed()
    fun onCancel()
}

class LinkAccountComponentImpl(
    private val accountType: UserLinkedAccountType,
    componentContext: ComponentContext,
    private val navigateOnCancel: () -> Unit,
) : AbstractComponent(componentContext), LinkAccountComponent {

    private val userLinkedAccounts: StateFlowSettings<UserLinkedAccountsModel> =
        get(USER_LINKED_ACCOUNTS_MODULE_QUALIFIER)

    private val requestedPermissions: MutableStateFlow<OAuth2AccountService.RequestedPermissions?> =
        MutableStateFlow(null)

    private val _code: MutableStateFlow<String> = MutableStateFlow("")
    override val code: Value<String> = _code.asValue()

    private val _isLoadingAppPermissions: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val isLoadingAppPermissions: Value<Boolean> = _isLoadingAppPermissions.asValue()

    private val _isErrorGettingToken: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val isErrorGettingToken: Value<Boolean> = _isErrorGettingToken.asValue()

    override val shouldEnterCode: Value<Boolean> =
        requestedPermissions.map { it != null }.asValue()

    override fun onUserAccessCodeChanged(code: String) {
        _code.update { code }
    }

    override fun onRequestAppPermissions(uriHandler: UriHandler) {
        val verifyUri = accountType
            .createService()
            .requestPermissions()
            .also { requestedPermissions.value = it }
            ?.generateVerifyUri()
            ?: return toast("Not supported")
        uriHandler.openUri(verifyUri)
    }

    override fun onUserAccessCodeConfirmed() {
        val requestedPermissions = requestedPermissions.value ?: return

        scope.launch {
            _isLoadingAppPermissions.update { true }

            requestedPermissions.authenticateUser(code.value)
                .onSuccess { authenticated ->
                    userLinkedAccounts
                        .updateInScope(this, authenticated::updateUserLinkedAccounts)
                        .invokeOnCompletion {
                            _isLoadingAppPermissions.update { false }
                            onCancel()
                        }
                }.onFailure {
                    _isLoadingAppPermissions.update { false }
                    _isErrorGettingToken.update { true }
                }

        }
    }

    override fun onCancel() {
        navigateOnCancel()
    }
}
