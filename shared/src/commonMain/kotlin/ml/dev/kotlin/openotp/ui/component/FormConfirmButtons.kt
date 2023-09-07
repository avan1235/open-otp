package ml.dev.kotlin.openotp.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun FormConfirmButtons(
    confirm: FormConfirmButtonData,
    cancel: FormConfirmButtonData,
    padding: Dp = 16.dp,
    buttonPadding: PaddingValues = PaddingValues(
        horizontal = 20.dp,
        vertical = 12.dp,
    ),
    buttonSpacer: Dp = 8.dp,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.BottomEnd,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalButton(
                onClick = cancel.onClick,
                contentPadding = buttonPadding,
            ) {
                Icon(imageVector = cancel.imageVector, contentDescription = cancel.contentDescription)
                Spacer(Modifier.width(buttonSpacer))
                Text(
                    text = cancel.text,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Button(
                onClick = confirm.onClick,
                contentPadding = buttonPadding
            ) {
                Icon(imageVector = confirm.imageVector, contentDescription = confirm.contentDescription)
                Spacer(Modifier.width(buttonSpacer))
                Text(
                    text = confirm.text,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

internal class FormConfirmButtonData(
    val text: String,
    val contentDescription: String?,
    val imageVector: ImageVector,
    val onClick: () -> Unit,
)
