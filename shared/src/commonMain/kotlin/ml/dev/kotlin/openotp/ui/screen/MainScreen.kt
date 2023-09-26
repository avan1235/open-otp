package ml.dev.kotlin.openotp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.icerock.moko.resources.compose.stringResource
import ml.dev.kotlin.openotp.component.MainComponent
import ml.dev.kotlin.openotp.otp.OtpData
import ml.dev.kotlin.openotp.otp.UserOtpCodeData
import ml.dev.kotlin.openotp.qr.CameraPermission.Denied
import ml.dev.kotlin.openotp.qr.CameraPermission.Granted
import ml.dev.kotlin.openotp.qr.rememberCameraPermissionState
import ml.dev.kotlin.openotp.shared.OpenOtpResources
import ml.dev.kotlin.openotp.ui.component.AddActionButton
import ml.dev.kotlin.openotp.ui.component.FilteredOtpCodeItems
import ml.dev.kotlin.openotp.ui.component.OtpCodeItems
import ml.dev.kotlin.openotp.util.lambda
import ml.dev.kotlin.openotp.util.runIfNonNull

@Composable
internal fun MainScreen(mainComponent: MainComponent) {
    val cameraPermissionState = rememberCameraPermissionState()
    val navigateToScanQRCodeWhenCameraPermissionChanged by mainComponent.navigateToScanQRCodeWhenCameraPermissionChanged.subscribeAsState()

    if (cameraPermissionState != null) {
        val isGranted = cameraPermissionState.permission.isGranted
        LaunchedEffect(isGranted) {
            if (isGranted && navigateToScanQRCodeWhenCameraPermissionChanged) {
                mainComponent.onCameraPermissionGranted()
                mainComponent.onScanQRCodeClick()
            }
        }
    }

    val listState = rememberLazyListState()
    val isFirstListItemVisible = remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }

    val codeData by mainComponent.codeData.subscribeAsState()
    val timestamp by mainComponent.timestamp.subscribeAsState()
    val isSearchActive by mainComponent.isSearchActive.subscribeAsState()

    FilteredOtpCodeItems(
        codeData = codeData,
        timestamp = timestamp,
        isSearchActive = isSearchActive,
        onOtpCodeDataDismiss = mainComponent::onOtpCodeDataRemove,
        onSearchBarActiveChange = mainComponent::onSearchBarActiveChange,
        onRestartCode = mainComponent::onOtpCodeDataRestart,
    )
    AllOtpCodeItems(
        codeData = codeData,
        timestamp = timestamp,
        listState = listState,
        onOtpCodeDataDismiss = mainComponent::onOtpCodeDataRemove,
        onRestartCode = mainComponent::onOtpCodeDataRestart,
    )
    AddActionButton(
        expanded = isFirstListItemVisible.value,
        visible = !isSearchActive,
        onScanQRCodeClick = runIfNonNull(cameraPermissionState) {
            lambda {
                when (it.permission) {
                    Granted -> mainComponent.onScanQRCodeClick()
                    Denied -> {
                        mainComponent.onRequestedCameraPermission()
                        it.launchRequest()
                    }
                }
            }
        },
        onAddWithTextClick = mainComponent::onAddProviderClick,
    )
}

@Composable
private fun AllOtpCodeItems(
    codeData: UserOtpCodeData,
    timestamp: Long,
    listState: LazyListState,
    onOtpCodeDataDismiss: (OtpData) -> Boolean,
    onRestartCode: (OtpData) -> Unit,
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
                if (codeData.isNotEmpty()) {
                    OtpCodeItems(codeData, timestamp, onOtpCodeDataDismiss, onRestartCode, listState)
                } else {
                    Text(text = stringResource(OpenOtpResources.strings.add_new_keys))
                }
            }
        }
    }
}
