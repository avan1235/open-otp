package ml.dev.kotlin.openotp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.icerock.moko.resources.compose.stringResource
import `in`.procyk.compose.camera.qr.CameraPermission.Denied
import `in`.procyk.compose.camera.qr.CameraPermission.Granted
import `in`.procyk.compose.camera.qr.rememberCameraPermissionState
import ml.dev.kotlin.openotp.component.MainComponent
import ml.dev.kotlin.openotp.otp.OtpData
import ml.dev.kotlin.openotp.otp.PresentedOtpCodeData
import ml.dev.kotlin.openotp.shared.OpenOtpResources
import ml.dev.kotlin.openotp.ui.component.*

@Composable
internal fun MainScreen(mainComponent: MainComponent) {
    SystemBarsScreen {
        val cameraPermissionState = rememberCameraPermissionState()
        val navigateToScanQRCodeWhenCameraPermissionChanged by mainComponent.navigateToScanQRCodeWhenCameraPermissionChanged.subscribeAsState()

        val isGranted = cameraPermissionState.permission.isGranted
        LaunchedEffect(isGranted) {
            if (isGranted && navigateToScanQRCodeWhenCameraPermissionChanged) {
                mainComponent.onScanQRCodeClick()
            }
        }

        val codeData by mainComponent.codeData.subscribeAsState()
        val timestamp by mainComponent.timestamp.subscribeAsState()
        val confirmOtpDataDelete by mainComponent.confirmOtpDataDelete.subscribeAsState()
        val isSearchActive by mainComponent.isSearchActive.subscribeAsState()

        FilteredOtpCodeItems(
            codeData = codeData,
            timestamp = timestamp,
            confirmCodeDismiss = confirmOtpDataDelete,
            isSearchActive = isSearchActive,
            onOtpCodeDataDismiss = mainComponent::onOtpCodeDataRemove,
            onSearchBarActiveChange = mainComponent::onSearchBarActiveChange,
            onRestartCode = mainComponent::onOtpCodeDataRestart,
            onMoveCode = mainComponent::onOtpCodeDataReordered,
            copyOtpCode = mainComponent::copyOtpCode,
            onSettingsIconClick = mainComponent::onSettingsClick
        )

        val listState = rememberLazyListState()
        val dragDropState = rememberDragDropState(listState, mainComponent::onOtpCodeDataReordered)
        val isDragAndDropEnabled by mainComponent.isDragAndDropEnabled.subscribeAsState()
        val showSortedGroupsHeaders by mainComponent.showSortedGroupsHeaders.subscribeAsState()
        AllOtpCodeItems(
            codeData = codeData,
            timestamp = timestamp,
            confirmCodeDismiss = confirmOtpDataDelete,
            isDragAndDropEnabled = isDragAndDropEnabled,
            showSortedGroupsHeaders = showSortedGroupsHeaders,
            onOtpCodeDataDismiss = mainComponent::onOtpCodeDataRemove,
            onRestartCode = mainComponent::onOtpCodeDataRestart,
            copyOtpCode = mainComponent::copyOtpCode,
            dragDropState = dragDropState,
        )
        AddActionButton(
            dragDropState = dragDropState,
            visible = !isSearchActive,
            onScanQRCodeClick = {
                when (cameraPermissionState.permission) {
                    Granted -> mainComponent.onScanQRCodeClick()
                    Denied -> {
                        mainComponent.onRequestedCameraPermission()
                        cameraPermissionState.launchRequest()
                    }
                }
            }.takeIf { cameraPermissionState.isAvailable },
            onAddWithTextClick = mainComponent::onAddProviderClick,
        )
    }
}

@Composable
private fun AllOtpCodeItems(
    codeData: PresentedOtpCodeData,
    timestamp: Long,
    confirmCodeDismiss: Boolean,
    isDragAndDropEnabled: Boolean,
    showSortedGroupsHeaders: Boolean,
    onOtpCodeDataDismiss: (OtpData) -> Boolean,
    onRestartCode: (OtpData) -> Unit,
    dragDropState: DragDropState,
    copyOtpCode: ClipboardManager.(item: OtpData, timestamp: Long) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column {
            Spacer(Modifier.height(70.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f, fill = true),
                contentAlignment = Alignment.Center,
            ) {
                if (!codeData.isEmpty) {
                    OtpCodeItems(
                        codeData,
                        timestamp,
                        confirmCodeDismiss,
                        isDragAndDropEnabled,
                        showSortedGroupsHeaders,
                        onOtpCodeDataDismiss,
                        onRestartCode,
                        dragDropState,
                        copyOtpCode,
                    )
                } else {
                    Text(text = stringResource(OpenOtpResources.strings.add_new_keys))
                }
            }
        }
    }
}
