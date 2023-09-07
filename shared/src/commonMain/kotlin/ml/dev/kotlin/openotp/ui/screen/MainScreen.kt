package ml.dev.kotlin.openotp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import ml.dev.kotlin.openotp.component.MainComponent
import ml.dev.kotlin.openotp.otp.OtpData
import ml.dev.kotlin.openotp.otp.UserOtpCodeData
import ml.dev.kotlin.openotp.qr.rememberQRCodeScanner
import ml.dev.kotlin.openotp.ui.component.AddActionButton
import ml.dev.kotlin.openotp.ui.component.FilteredOtpCodeItems
import ml.dev.kotlin.openotp.ui.component.OtpCodeItems

@Composable
internal fun MainScreen(mainComponent: MainComponent) {
    val onScanClick = rememberQRCodeScanner(mainComponent::onQRCodeScanned)

    val listState = rememberLazyListState()
    val isFirstListItemVisible = remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }

    val codeData by mainComponent.codeData.subscribeAsState()
    val timestamp by mainComponent.timestamp.subscribeAsState()
    val searchActive by mainComponent.isSearchActive.subscribeAsState()

    FilteredOtpCodeItems(
        codeData = codeData,
        timestamp = timestamp,
        searchActive = searchActive,
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
        visible = !searchActive,
        onScanQRCodeClick = onScanClick,
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
                    Text("Add New Keys")
                }
            }
        }
    }
}
