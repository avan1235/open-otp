package ml.dev.kotlin.openotp.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import ml.dev.kotlin.openotp.USER_OTP_CODE_DATA_MODULE_QUALIFIER
import ml.dev.kotlin.openotp.otp.*
import ml.dev.kotlin.openotp.util.ValueSettings
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
    defaultAlgorithm: HmacAlgorithm,
    defaultDigits: OtpDigits,
) : AbstractComponent(componentContext), AddOtpProviderComponent {

    protected val secureStorage: ValueSettings<UserOtpCodeData> = get(USER_OTP_CODE_DATA_MODULE_QUALIFIER)

    private val _issuer: MutableValue<String> = MutableValue("")
    override val issuer: Value<String> = _issuer

    private val _accountName: MutableValue<String> = MutableValue("")
    override val accountName: Value<String> = _accountName

    private val _secret: MutableValue<String> = MutableValue("")
    override val secret: Value<String> = _secret

    private val _secretIsError: MutableValue<Boolean> = MutableValue(false)
    override val secretIsError: Value<Boolean> = _secretIsError

    private val _algorithm: MutableValue<HmacAlgorithm> = MutableValue(defaultAlgorithm)
    override val algorithm: Value<HmacAlgorithm> = _algorithm

    private val _digits: MutableValue<OtpDigits> = MutableValue(defaultDigits)
    override val digits: Value<OtpDigits> = _digits

    override fun onIssuerChanged(issuer: String) {
        _issuer.update { issuer }
    }

    override fun onAccountNameChanged(accountName: String) {
        _accountName.update { accountName }
    }

    override fun onSecretChanged(secret: String) {
        _secret.update { secret }
    }

    override fun onAlgorithmSelected(algorithm: HmacAlgorithm) {
        _algorithm.update { algorithm }
    }

    override fun onDigitsSelected(digits: OtpDigits) {
        _digits.update { digits }
    }

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
    defaultAlgorithm = TotpConfig.DEFAULT.hmacAlgorithm,
    defaultDigits = TotpConfig.DEFAULT.codeDigits,
), AddTotpProviderComponent {

    private val _period: MutableValue<TotpPeriod> = MutableValue(TotpConfig.DEFAULT.period)
    override val period: Value<TotpPeriod> = _period

    override fun onPeriodSelected(period: TotpPeriod) {
        _period.update { period }
    }

    override fun onSaveClicked() {
        val config = TotpConfig(period.value, digits.value, algorithm.value)
        val codeData = TotpData(issuer.value, accountName.value, secret.value, config)
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
    defaultAlgorithm = HotpConfig.DEFAULT.hmacAlgorithm,
    defaultDigits = HotpConfig.DEFAULT.codeDigits,
), AddHotpProviderComponent {

    private val _counter: MutableValue<String> = MutableValue("0")
    override val counter: Value<String> = _counter

    private val _counterIsError: MutableValue<Boolean> = MutableValue(false)
    override val counterIsError: Value<Boolean> = _counterIsError

    override fun onCounterChanged(counter: String) {
        val counterIsError = counter.toLongOrNull() == null && counter.isNotBlank()
        _counterIsError.value = counterIsError
        if (counterIsError) {
            return
        }
        _counter.update { counter }
    }

    override fun onSaveClicked() {
        val config = HotpConfig(digits.value, algorithm.value)
        val counter = counter.value.toLongOrNull() ?: 0
        val codeData = HotpData(issuer.value, accountName.value, secret.value, counter, config)
        val updated = secureStorage.get() + codeData
        secureStorage.set(updated)
        super.onSaveClicked()
    }
}