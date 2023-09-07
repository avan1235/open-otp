package ml.dev.kotlin.openotp.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun AddActionButton(
    expanded: Boolean,
    visible: Boolean,
    onScanQRCodeClick: (() -> Unit)?,
    onAddWithTextClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd,
        ) {
            val fabState = rememberMultiFabState()
            MultiFloatingActionButton(
                items = listOfNotNull(
                    MultiFabItem(
                        id = FabItem.ScanQRCode,
                        icon = Icons.Default.QrCodeScanner,
                    ).takeUnless { onScanQRCodeClick == null },
                    MultiFabItem(
                        id = FabItem.AddWithText,
                        icon = Icons.Default.EditNote,
                    ),
                ),
                fabIcon = FabIcon(
                    icon = Icons.Default.Add,
                    iconRotate = 45f,
                    text = { "Add" }
                ),
                expanded = expanded && !fabState.value.isExpanded(),
                containerColor = MaterialTheme.colorScheme.tertiary,
                onFabItemClicked = {
                    when (it) {
                        FabItem.ScanQRCode -> onScanQRCodeClick?.invoke()
                        FabItem.AddWithText -> onAddWithTextClick()
                    }
                    fabState.value = MultiFabState.Collapsed
                },
                fabState = fabState,
            )
        }
    }
}

private enum class FabItem {
    ScanQRCode, AddWithText,
}
