package ml.dev.kotlin.openotp.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import ml.dev.kotlin.openotp.USER_OTP_CODE_DATA_MODULE_QUALIFIER
import ml.dev.kotlin.openotp.otp.*
import ml.dev.kotlin.openotp.shared.OpenOtpResources
import ml.dev.kotlin.openotp.util.StateFlowSettings
import ml.dev.kotlin.openotp.util.isValidBase32Secret
import ml.dev.kotlin.openotp.util.unit
import org.koin.core.component.get

interface AddOtpProviderComponent {

    val issuer: Value<String>
    val accountName: Value<String>
    val secret: Value<String>
    val secretIsError: Value<Boolean>
    val algorithm: Value<HmacAlgorithm>
    val digits: Value<OtpDigits>

    fun onIssuerChanged(issuer: String)
    fun onAccountNameChanged(accountName: String)
    fun onSecretChanged(secret: String)
    fun onAlgorithmSelected(algorithm: HmacAlgorithm)
    fun onDigitsSelected(digits: OtpDigits)
    fun onSaveClicked()
    fun onCancelClicked()
}

interface AddTotpProviderComponent : AddOtpProviderComponent {

    val period: Value<TotpPeriod>

    fun onPeriodSelected(period: TotpPeriod)
}

interface AddHotpProviderComponent : AddOtpProviderComponent {

    val counter: Value<String>
    val counterIsError: Value<Boolean>

    fun onCounterChanged(counter: String)
}

abstract class AddOtpProviderComponentImpl(
    componentContext: ComponentContext,
    private val navigateOnSaveClicked: () -> Unit,
    private val navigateOnCancelClicked: () -> Unit,
) : AbstractComponent(componentContext), AddOtpProviderComponent {

    protected val secureStorage: StateFlowSettings<UserOtpCodeData> = get(USER_OTP_CODE_DATA_MODULE_QUALIFIER)

    protected fun notifyInvalid(fieldName: String) {
        toast(message = stringResource(OpenOtpResources.strings.invalid_field_name_provided_formatted, fieldName))
    }

    protected fun notifyInvalidSecret() =
        notifyInvalid(fieldName = stringResource(OpenOtpResources.strings.secret_field))

    override fun onSaveClicked() {
        navigateOnSaveClicked()
    }

    override fun onCancelClicked() {
        navigateOnCancelClicked()
    }
}

class AddTotpProviderComponentImpl(
    componentContext: ComponentContext,
    navigateOnSaveClicked: () -> Unit,
    navigateOnCancelClicked: () -> Unit,
) : AddOtpProviderComponentImpl(
    componentContext = componentContext,
    navigateOnSaveClicked = navigateOnSaveClicked,
    navigateOnCancelClicked = navigateOnCancelClicked,
), AddTotpProviderComponent {

    private data class TotpModel(
        val issuer: String = "",
        val accountName: String = "",
        val secret: String = "",
        val secretIsError: Boolean = true,
        val algorithm: HmacAlgorithm = TotpConfig.DEFAULT.hmacAlgorithm,
        val digits: OtpDigits = TotpConfig.DEFAULT.codeDigits,
        val period: TotpPeriod = TotpConfig.DEFAULT.period,
    )

    private val model: MutableStateFlow<TotpModel> = MutableStateFlow(TotpModel())

    override val issuer: Value<String> = model.map { it.issuer }.asValue()
    override val accountName: Value<String> = model.map { it.accountName }.asValue()
    override val secret: Value<String> = model.map { it.secret }.asValue()
    override val secretIsError: Value<Boolean> = model.map { it.secretIsError }.asValue()
    override val algorithm: Value<HmacAlgorithm> = model.map { it.algorithm }.asValue()
    override val digits: Value<OtpDigits> = model.map { it.digits }.asValue()
    override val period: Value<TotpPeriod> = model.map { it.period }.asValue()

    override fun onIssuerChanged(issuer: String) {
        model.update { it.copy(issuer = issuer) }
    }

    override fun onAccountNameChanged(accountName: String) {
        model.update { it.copy(accountName = accountName) }
    }

    override fun onSecretChanged(secret: String) {
        val secretIsError = !secret.isValidBase32Secret
        model.update {
            it.copy(
                secret = secret,
                secretIsError = secretIsError,
            )
        }
    }

    override fun onAlgorithmSelected(algorithm: HmacAlgorithm) {
        model.update { it.copy(algorithm = algorithm) }
    }

    override fun onDigitsSelected(digits: OtpDigits) {
        model.update { it.copy(digits = digits) }
    }

    override fun onPeriodSelected(period: TotpPeriod) {
        model.update { it.copy(period = period) }
    }

    override fun onSaveClicked() = with(model.value) {
        val secret = secret
        if (!secret.isValidBase32Secret) {
            return notifyInvalidSecret()
        }

        val config = TotpConfig(period, digits, algorithm)
        val codeData = TotpData(issuer, accountName, secret, config)
        secureStorage
            .updateInScope { it + codeData }
            .invokeOnCompletion {
                super.onSaveClicked()
            }.unit()
    }
}

class AddHotpProviderComponentImpl(
    componentContext: ComponentContext,
    navigateOnSaveClicked: () -> Unit,
    navigateOnCancelClicked: () -> Unit,
) : AddOtpProviderComponentImpl(
    componentContext = componentContext,
    navigateOnSaveClicked = navigateOnSaveClicked,
    navigateOnCancelClicked = navigateOnCancelClicked,
), AddHotpProviderComponent {

    private data class HotpModel(
        val issuer: String = "",
        val accountName: String = "",
        val secret: String = "",
        val secretIsError: Boolean = true,
        val algorithm: HmacAlgorithm = HotpConfig.DEFAULT.hmacAlgorithm,
        val digits: OtpDigits = HotpConfig.DEFAULT.codeDigits,
        val counter: String = "0",
        val counterIsError: Boolean = false,
    )

    private val model: MutableStateFlow<HotpModel> = MutableStateFlow(HotpModel())

    override val issuer: Value<String> = model.map { it.issuer }.asValue()
    override val accountName: Value<String> = model.map { it.accountName }.asValue()
    override val secret: Value<String> = model.map { it.secret }.asValue()
    override val secretIsError: Value<Boolean> = model.map { it.secretIsError }.asValue()
    override val algorithm: Value<HmacAlgorithm> = model.map { it.algorithm }.asValue()
    override val digits: Value<OtpDigits> = model.map { it.digits }.asValue()
    override val counter: Value<String> = model.map { it.counter }.asValue()
    override val counterIsError: Value<Boolean> = model.map { it.counterIsError }.asValue()

    override fun onIssuerChanged(issuer: String) {
        model.update { it.copy(issuer = issuer) }
    }

    override fun onAccountNameChanged(accountName: String) {
        model.update { it.copy(accountName = accountName) }
    }

    override fun onSecretChanged(secret: String) {
        val secretIsError = !secret.isValidBase32Secret
        model.update {
            it.copy(
                secret = secret,
                secretIsError = secretIsError,
            )
        }
    }

    override fun onAlgorithmSelected(algorithm: HmacAlgorithm) {
        model.update { it.copy(algorithm = algorithm) }
    }

    override fun onDigitsSelected(digits: OtpDigits) {
        model.update { it.copy(digits = digits) }
    }

    override fun onCounterChanged(counter: String) {
        val counterIsValid = counter.toLongOrNull().isValid()
        val canUpdateCounter = counterIsValid || counter.isBlank()
        if (!canUpdateCounter) {
            return
        }
        model.update {
            it.copy(
                counter = counter,
                counterIsError = !counterIsValid,
            )
        }
    }

    override fun onSaveClicked() = with(model.value) {
        val secret = secret
        if (!secret.isValidBase32Secret) {
            return notifyInvalidSecret()
        }
        val counter = counter.toLongOrNull()
        if (!counter.isValid()) {
            return notifyInvalidCounter()
        }

        val config = HotpConfig(digits, algorithm)
        val codeData = HotpData(issuer, accountName, secret, counter, config)
        secureStorage
            .updateInScope { it + codeData }
            .invokeOnCompletion {
                super.onSaveClicked()
            }.unit()
    }

    private fun notifyInvalidCounter() =
        notifyInvalid(fieldName = stringResource(OpenOtpResources.strings.counter_field))
}
