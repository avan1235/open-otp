package ml.dev.kotlin.openotp.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.decompose.value.updateAndGet
import com.arkivanov.essenty.backhandler.BackCallback
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ml.dev.kotlin.openotp.USER_OTP_CODE_DATA_MODULE_QUALIFIER
import ml.dev.kotlin.openotp.otp.HotpData
import ml.dev.kotlin.openotp.otp.OtpData
import ml.dev.kotlin.openotp.otp.TotpData
import ml.dev.kotlin.openotp.otp.UserOtpCodeData
import ml.dev.kotlin.openotp.shared.OpenOtpResources
import ml.dev.kotlin.openotp.util.ValueSettings
import ml.dev.kotlin.openotp.util.currentEpochMilliseconds
import org.koin.core.component.get
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

interface MainComponent {

    val timestamp: Value<Long>
    val codeData: Value<UserOtpCodeData>
    val isSearchActive: Value<Boolean>
    val navigateToScanQRCodeWhenCameraPermissionChanged: Value<Boolean>

    fun onRequestedCameraPermission()

    fun onOtpCodeDataRemove(otpData: OtpData): Boolean

    fun onOtpCodeDataRestart(otpData: OtpData)

    fun onSearchBarActiveChange(isActive: Boolean)

    fun onScanQRCodeClick()

    fun onAddProviderClick()
}

class MainComponentImpl(
    componentContext: ComponentContext,
    private val navigateOnScanQRCode: () -> Unit,
    private val navigateOnAddProvider: () -> Unit,
    private val updateTimeDelay: Duration = 10.milliseconds
) : AbstractComponent(componentContext), MainComponent {

    private val userOtpCodeData: ValueSettings<UserOtpCodeData> = get(USER_OTP_CODE_DATA_MODULE_QUALIFIER)

    private val _timestamp: MutableValue<Long> = MutableValue(currentEpochMilliseconds())
    override val timestamp: Value<Long> = _timestamp

    private val _requestedCameraPermissionCount: MutableValue<Int> = MutableValue(0)

    private val _navigateToScanQRCodeWhenCameraPermissionChanged: MutableValue<Boolean> = MutableValue(false)
    override val navigateToScanQRCodeWhenCameraPermissionChanged: Value<Boolean> =
        _navigateToScanQRCodeWhenCameraPermissionChanged


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

    override fun onRequestedCameraPermission() {
        val updated = _requestedCameraPermissionCount.updateAndGet { it + 1 }
        _navigateToScanQRCodeWhenCameraPermissionChanged.update { true }

        if (updated < MAX_CAMERA_PERMISSION_SILENT_REQUESTS) return

        toast(message = stringResource(OpenOtpResources.strings.missing_camera_permissions))
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

    override fun onSearchBarActiveChange(isActive: Boolean) {
        val updated = isSearchActive.updateAndGet { isActive }
        searchBackCallback.isEnabled = updated
    }

    override fun onScanQRCodeClick() {
        _navigateToScanQRCodeWhenCameraPermissionChanged.update { false }
        navigateOnScanQRCode()
    }

    override fun onAddProviderClick() {
        navigateOnAddProvider()
    }
}

private const val MAX_CAMERA_PERMISSION_SILENT_REQUESTS: Int = 2
