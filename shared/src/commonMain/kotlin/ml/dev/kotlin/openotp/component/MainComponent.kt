package ml.dev.kotlin.openotp.component

import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.decompose.value.update
import com.arkivanov.decompose.value.updateAndGet
import com.arkivanov.essenty.backhandler.BackCallback
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ml.dev.kotlin.openotp.USER_OTP_CODE_DATA_MODULE_QUALIFIER
import ml.dev.kotlin.openotp.USER_PREFERENCES_MODULE_QUALIFIER
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
    val confirmOtpDataDelete: Value<Boolean>
    val codeData: Value<UserOtpCodeData>
    val isSearchActive: Value<Boolean>
    val navigateToScanQRCodeWhenCameraPermissionChanged: Value<Boolean>

    fun onRequestedCameraPermission()

    fun onOtpCodeDataRemove(otpData: OtpData): Boolean

    fun onOtpCodeDataRestart(otpData: OtpData)

    fun copyOtpCode(clipboardManager: ClipboardManager, item: OtpData, timestamp: Long)

    fun onSearchBarActiveChange(isActive: Boolean)

    fun onSettingsClick()

    fun onScanQRCodeClick()

    fun onAddProviderClick()
}

class MainComponentImpl(
    componentContext: ComponentContext,
    private val navigateOnScanQRCode: () -> Unit,
    private val navigateOnAddProvider: () -> Unit,
    private val navigateSettings: () -> Unit,
    private val updateTimeDelay: Duration = 10.milliseconds
) : AbstractComponent(componentContext), MainComponent {

    private val userOtpCodeData: ValueSettings<UserOtpCodeData> = get(USER_OTP_CODE_DATA_MODULE_QUALIFIER)

    private val userPreferences: ValueSettings<UserPreferencesModel> = get(USER_PREFERENCES_MODULE_QUALIFIER)

    private val _timestamp: MutableValue<Long> = MutableValue(currentEpochMilliseconds())
    override val timestamp: Value<Long> = _timestamp

    override val confirmOtpDataDelete: Value<Boolean> = userPreferences.value.map { it.confirmOtpDataDelete }

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

    override fun copyOtpCode(clipboardManager: ClipboardManager, item: OtpData, timestamp: Long) {
        val code = item.code(timestamp)
        val string = AnnotatedString(code)
        clipboardManager.setText(string)
        toast(
            message = stringResource(OpenOtpResources.strings.copied_code_to_clipboard, code),
            withDismissAction = false,
        )
    }

    override fun onSearchBarActiveChange(isActive: Boolean) {
        val updated = isSearchActive.updateAndGet { isActive }
        searchBackCallback.isEnabled = updated
    }

    override fun onSettingsClick() {
        navigateSettings()
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
