package ml.dev.kotlin.openotp.component

import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ml.dev.kotlin.openotp.USER_OTP_CODE_DATA_MODULE_QUALIFIER
import ml.dev.kotlin.openotp.USER_PREFERENCES_MODULE_QUALIFIER
import ml.dev.kotlin.openotp.otp.HotpData
import ml.dev.kotlin.openotp.otp.OtpData
import ml.dev.kotlin.openotp.otp.TotpData
import ml.dev.kotlin.openotp.otp.UserOtpCodeData
import ml.dev.kotlin.openotp.shared.OpenOtpResources
import ml.dev.kotlin.openotp.util.StateFlowSettings
import ml.dev.kotlin.openotp.util.currentEpochMilliseconds
import ml.dev.kotlin.openotp.util.unit
import org.koin.core.component.get
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

interface MainComponent {

    val timestamp: Value<Long>
    val confirmOtpDataDelete: Value<Boolean>
    val codeData: Value<UserOtpCodeData>
    val isSearchActive: Value<Boolean>
    val isDragAndDropEnabled: Value<Boolean>
    val navigateToScanQRCodeWhenCameraPermissionChanged: Value<Boolean>

    fun onRequestedCameraPermission()

    fun onOtpCodeDataRemove(otpData: OtpData): Boolean

    fun onOtpCodeDataReordered(currentIndex: Int, updatedIndex: Int)

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
    private val updateTimeDelay: Duration = 10.milliseconds,
) : AbstractComponent(componentContext), MainComponent {

    private data class CameraPermissionRequest(
        val count: Int = 0,
        val changed: Boolean = false,
    )

    private val userOtpCodeData: StateFlowSettings<UserOtpCodeData> = get(USER_OTP_CODE_DATA_MODULE_QUALIFIER)

    private val userPreferences: StateFlowSettings<UserPreferencesModel> = get(USER_PREFERENCES_MODULE_QUALIFIER)

    private val _timestamp: MutableStateFlow<Long> = MutableStateFlow(currentEpochMilliseconds())
    override val timestamp: Value<Long> = _timestamp.asValue()

    override val confirmOtpDataDelete: Value<Boolean> =
        userPreferences.stateFlow.map { it.confirmOtpDataDelete }.asValue()

    private val _cameraPermissionRequest: MutableStateFlow<CameraPermissionRequest> =
        MutableStateFlow(CameraPermissionRequest())

    override val navigateToScanQRCodeWhenCameraPermissionChanged: Value<Boolean> =
        _cameraPermissionRequest.map { it.changed }.asValue()

    override val codeData: Value<UserOtpCodeData> = combine(
        userOtpCodeData.stateFlow,
        userPreferences.stateFlow.map { it.sortOtpDataBy },
        userPreferences.stateFlow.map { it.sortOtpDataReversed },
        userPreferences.stateFlow.map { it.sortOtpDataNullsFirst },
    ) transform@{ codes, sortBy, sortReversed, sortNullsFirst ->
        val selector = sortBy.selector ?: return@transform codes
        val comparator: Comparator<String?> = if (sortNullsFirst) nullsFirst() else nullsLast()
        val otpDataComparator: Comparator<OtpData> = compareBy(comparator) { selector(it)?.toLowerCase(Locale.current) }
        val reversedComparator = if (sortReversed) otpDataComparator.reversed() else otpDataComparator
        codes.sortedWith(reversedComparator)
    }.asValue()

    private val _isSearchActive: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val isSearchActive: Value<Boolean> = _isSearchActive.asValue()

    override val isDragAndDropEnabled: Value<Boolean> =
        userPreferences.stateFlow.map { it.sortOtpDataBy == SortOtpDataBy.Dont }.asValue()

    private val searchBackCallback: BackCallback = BackCallback {
        _isSearchActive.value = false
    }

    init {
        backHandler.register(searchBackCallback)
        scope.launch {
            while (isActive) {
                delay(updateTimeDelay)
                _timestamp.emit(currentEpochMilliseconds())
            }
        }
    }

    override fun onRequestedCameraPermission() {
        val updated = _cameraPermissionRequest.updateAndGet {
            it.copy(count = it.count + 1, changed = true)
        }
        if (updated.count < MAX_CAMERA_PERMISSION_SILENT_REQUESTS) return

        toast(message = stringResource(OpenOtpResources.strings.missing_camera_permissions))
    }

    override fun onOtpCodeDataRemove(otpData: OtpData): Boolean {
        userOtpCodeData.updateInScope { before ->
            before.filter { it != otpData }
        }.unit()
        return true
    }

    override fun onOtpCodeDataReordered(currentIndex: Int, updatedIndex: Int) {
        userOtpCodeData.updateInScope {
            buildList {
                addAll(it)
                set(currentIndex, it[updatedIndex])
                set(updatedIndex, it[currentIndex])
            }
        }.unit()
    }

    override fun onOtpCodeDataRestart(otpData: OtpData) = when (otpData) {
        is TotpData -> Unit
        is HotpData -> userOtpCodeData.updateInScope { before ->
            val updated = otpData.increaseCounter()
            before.map { if (it != otpData) it else updated }
        }.unit()
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
        val updated = _isSearchActive.updateAndGet { isActive }
        searchBackCallback.isEnabled = updated
    }

    override fun onSettingsClick() {
        navigateSettings()
    }

    override fun onScanQRCodeClick() {
        _cameraPermissionRequest.update { it.copy(changed = false) }
        navigateOnScanQRCode()
    }

    override fun onAddProviderClick() {
        navigateOnAddProvider()
    }
}

private const val MAX_CAMERA_PERMISSION_SILENT_REQUESTS: Int = 2
