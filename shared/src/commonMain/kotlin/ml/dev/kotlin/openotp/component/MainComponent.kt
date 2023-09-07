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
import ml.dev.kotlin.openotp.util.ValueSettings
import ml.dev.kotlin.openotp.util.currentEpochMilliseconds
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
            is QRResult.QRSuccess -> {
                val content = result.content ?: return notifyInvalidQRCodeData()
                val uri = Uri.parseOrNull(content) ?: return notifyInvalidQRCodeData()
                if ("otpauth" != uri.scheme) {
                    return notifyInvalidQRCodeData()
                }
                val type = when (uri.host) {
                    "totp" -> OtpType.TOTP
                    "hotp" -> OtpType.HOTP
                    else -> return notifyInvalidQRCodeData()
                }
                val secret = uri.getQueryParameter("secret") ?: return notifyInvalidQRCodeData()
                val issuer = uri.getQueryParameter("issuer")
                val name = uri.path?.removePrefix("/")
                when (type) {
                    OtpType.TOTP -> {
                        val updatedData = userOtpCodeData.get() + TotpData(issuer, name, secret, TotpConfig.DEFAULT)
                        userOtpCodeData.set(updatedData)
                    }

                    else -> return notifyUnsupportedHOTP()
                }
            }
        }
    }

    private fun notifyInvalidQRCodeData() {
        toast("Invalid QR code")
    }

    private fun notifyMissingCameraPermissions() {
        toast("Missing camera permissions")
    }

    private fun notifyUnsupportedHOTP() {
        toast("Unsupported HOTP")
    }

    override fun onSearchBarActiveChange(isActive: Boolean) {
        val updated = isSearchActive.updateAndGet { isActive }
        searchBackCallback.isEnabled = updated
    }

    override fun onAddProviderClick() {
        navigateOnAddProvider()
    }
}
