package ml.dev.kotlin.openotp.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.decompose.value.update
import ml.dev.kotlin.openotp.USER_OTP_CODE_DATA_MODULE_QUALIFIER
import ml.dev.kotlin.openotp.otp.*
import ml.dev.kotlin.openotp.shared.OpenOtpResources
import ml.dev.kotlin.openotp.util.ValueSettings
import ml.dev.kotlin.openotp.util.isValidBase32Secret
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

    protected val secureStorage: ValueSettings<UserOtpCodeData> = get(USER_OTP_CODE_DATA_MODULE_QUALIFIER)

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

    private val model: MutableValue<TotpModel> = MutableValue(TotpModel())

    override val issuer: Value<String> get() = model.map { it.issuer }
    override val accountName: Value<String> get() = model.map { it.accountName }
    override val secret: Value<String> get() = model.map { it.secret }
    override val secretIsError: Value<Boolean> get() = model.map { it.secretIsError }
    override val algorithm: Value<HmacAlgorithm> get() = model.map { it.algorithm }
    override val digits: Value<OtpDigits> get() = model.map { it.digits }
    override val period: Value<TotpPeriod> get() = model.map { it.period }

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
        val updated = secureStorage.get() + codeData
        secureStorage.set(updated)

        super.onSaveClicked()
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

    private val model: MutableValue<HotpModel> = MutableValue(HotpModel())

    override val issuer: Value<String> get() = model.map { it.issuer }
    override val accountName: Value<String> get() = model.map { it.accountName }
    override val secret: Value<String> get() = model.map { it.secret }
    override val secretIsError: Value<Boolean> get() = model.map { it.secretIsError }
    override val algorithm: Value<HmacAlgorithm> get() = model.map { it.algorithm }
    override val digits: Value<OtpDigits> get() = model.map { it.digits }
    override val counter: Value<String> get() = model.map { it.counter }
    override val counterIsError: Value<Boolean> get() = model.map { it.counterIsError }

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
        val updated = secureStorage.get() + codeData
        secureStorage.set(updated)

        super.onSaveClicked()
    }

    private fun notifyInvalidCounter() =
        notifyInvalid(fieldName = stringResource(OpenOtpResources.strings.counter_field))
}
