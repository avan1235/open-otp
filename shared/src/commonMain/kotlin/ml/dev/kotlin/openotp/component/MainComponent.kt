package ml.dev.kotlin.openotp.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.decompose.value.updateAndGet
import com.arkivanov.essenty.backhandler.BackCallback
import com.eygraber.uri.Uri
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ml.dev.kotlin.openotp.USER_OTP_CODE_DATA_MODULE_QUALIFIER
import ml.dev.kotlin.openotp.otp.*
import ml.dev.kotlin.openotp.qr.QRResult
import ml.dev.kotlin.openotp.shared.OpenOtpResources
import ml.dev.kotlin.openotp.util.ValueSettings
import ml.dev.kotlin.openotp.util.currentEpochMilliseconds
import ml.dev.kotlin.openotp.util.isValidBase32Secret
import org.koin.core.component.get
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

interface MainComponent {

    val timestamp: Value<Long>
    val codeData: Value<UserOtpCodeData>
    val isSearchActive: Value<Boolean>

    fun onOtpCodeDataRemove(otpData: OtpData): Boolean

    fun onOtpCodeDataRestart(otpData: OtpData)

    fun onQRCodeScanned(result: QRResult)

    fun onSearchBarActiveChange(isActive: Boolean)

    fun onAddProviderClick()
}

class MainComponentImpl(
    componentContext: ComponentContext,
    private val navigateOnAddProvider: () -> Unit,
    private val updateTimeDelay: Duration = 10.milliseconds
) : AbstractComponent(componentContext), MainComponent {

    private val userOtpCodeData: ValueSettings<UserOtpCodeData> = get(USER_OTP_CODE_DATA_MODULE_QUALIFIER)

    private val _timestamp: MutableValue<Long> = MutableValue(currentEpochMilliseconds())
    override val timestamp: Value<Long> = _timestamp

    override val codeData: Value<UserOtpCodeData> = userOtpCodeData.value

    override val isSearchActive: MutableValue<Boolean> = MutableValue(false)

    private val searchBackCallback: BackCallback = BackCallback {
        isSearchActive.value = false
    }

    init {
        backHandler.register(searchBackCallback)
        scope.launch {
            while (isActive) {
                delay(updateTimeDelay)
                _timestamp.update { currentEpochMilliseconds() }
            }
        }
    }

    override fun onOtpCodeDataRemove(otpData: OtpData): Boolean {
        val before = userOtpCodeData.get()
        val filtered = before.filter { it != otpData }
        userOtpCodeData.set(filtered)
        return userOtpCodeData.get() != before
    }

    override fun onOtpCodeDataRestart(otpData: OtpData) = when (otpData) {
        is TotpData -> Unit
        is HotpData -> {
            val before = userOtpCodeData.get()
            val updated = otpData.increaseCounter()
            val mapped = before.map { if (it != otpData) it else updated }
            userOtpCodeData.set(mapped)
        }
    }

    override fun onQRCodeScanned(result: QRResult) {
        when (result) {
            QRResult.QRUserCanceled -> Unit
            QRResult.QRMissingPermission -> return notifyMissingCameraPermissions()
            is QRResult.QRError -> return notifyInvalidQRCodeData()
            is QRResult.QRSuccess -> storeQRCodeUserOtpCodeData(result)
        }
    }

    private fun notifyInvalidQRCodeData() {
        toast(message = stringResource(OpenOtpResources.strings.invalid_qr_code))
    }

    private fun notifyMissingCameraPermissions() {
        toast(message = stringResource(OpenOtpResources.strings.missing_camera_permissions))
    }

    override fun onSearchBarActiveChange(isActive: Boolean) {
        val updated = isSearchActive.updateAndGet { isActive }
        searchBackCallback.isEnabled = updated
    }

    override fun onAddProviderClick() {
        navigateOnAddProvider()
    }

    private fun storeQRCodeUserOtpCodeData(qrSuccess: QRResult.QRSuccess) {
        try {
            val uri = qrSuccess.uri

            uri.validateScheme()
            val secret = uri.secret
            val issuer = uri.issuer
            val name = uri.label
            val hmacAlgorithm = uri.hmacAlgorithm
            val codeDigits = uri.codeDigits

            when (uri.otpType) {
                OtpType.TOTP -> {
                    val period = uri.period
                    val config = TotpConfig(
                        period = period ?: TotpConfig.DEFAULT.period,
                        codeDigits = codeDigits ?: TotpConfig.DEFAULT.codeDigits,
                        hmacAlgorithm = hmacAlgorithm ?: TotpConfig.DEFAULT.hmacAlgorithm
                    )
                    val data = TotpData(issuer, name, secret, config)
                    val updatedData = userOtpCodeData.get() + data
                    userOtpCodeData.set(updatedData)
                }

                else -> {
                    val counter = uri.counter
                    val config = HotpConfig(
                        codeDigits = codeDigits ?: HotpConfig.DEFAULT.codeDigits,
                        hmacAlgorithm = hmacAlgorithm ?: HotpConfig.DEFAULT.hmacAlgorithm
                    )
                    val data = HotpData(issuer, name, secret, counter, config)
                    val updatedData = userOtpCodeData.get() + data
                    userOtpCodeData.set(updatedData)
                }
            }
        } catch (_: InvalidQRCodeException) {
            notifyInvalidQRCodeData()
        }
    }
}

private object InvalidQRCodeException : IllegalArgumentException()

private val Uri.otpType: OtpType
    get() = when (host) {
        "totp" -> OtpType.TOTP
        "hotp" -> OtpType.HOTP
        else -> throw InvalidQRCodeException
    }

private val Uri.secret: String
    get() = getQueryParameter("secret")
        ?.takeIf { it.isValidBase32Secret }
        ?: throw InvalidQRCodeException

private val Uri.issuer: String?
    get() = getQueryParameter("issuer")

private val Uri.label: String?
    get() = path?.removePrefix("/")

private val Uri.hmacAlgorithm: HmacAlgorithm?
    get() = when (getQueryParameter("algorithm")) {
        "sha1" -> HmacAlgorithm.SHA1
        "sha256" -> HmacAlgorithm.SHA256
        "sha512" -> HmacAlgorithm.SHA512
        null -> null
        else -> throw InvalidQRCodeException
    }

private val Uri.codeDigits: OtpDigits?
    get() = when (getQueryParameter("digits")) {
        "6" -> OtpDigits.Six
        "8" -> OtpDigits.Eight
        null -> null
        else -> throw InvalidQRCodeException
    }

private val Uri.period: TotpPeriod?
    get() = when (getQueryParameter("period")) {
        "15" -> TotpPeriod.Fifteen
        "30" -> TotpPeriod.Thirty
        "60" -> TotpPeriod.Sixty
        null -> null
        else -> throw InvalidQRCodeException
    }

private val Uri.counter: HotpCounter
    get() = getQueryParameter("counter")
        ?.toLongOrNull()
        ?.takeIf { it.isValid() }
        ?: throw InvalidQRCodeException

private fun Uri.validateScheme(): Unit =
    if ("otpauth" != scheme) throw InvalidQRCodeException else Unit

private val QRResult.QRSuccess.uri: Uri
    get() = content
        ?.let(Uri.Companion::parseOrNull)
        ?: throw InvalidQRCodeException
