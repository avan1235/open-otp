package ml.dev.kotlin.openotp.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import ml.dev.kotlin.openotp.shared.OpenOtpResources

@Composable
internal fun FormConfirmButtons(
    confirm: FormConfirmButtonData,
    cancel: FormConfirmButtonData,
    padding: Dp = 16.dp,
    buttonPadding: PaddingValues = FORM_BUTTON_PADDING,
    buttonSpacer: Dp = FORM_BUTTON_SPACE,
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

internal val FORM_BUTTON_PADDING = PaddingValues(
    horizontal = 20.dp,
    vertical = 12.dp,
)

internal val FORM_BUTTON_SPACE = 8.dp

internal class FormConfirmButtonData(
    val text: String,
    val contentDescription: String?,
    val imageVector: ImageVector,
    val onClick: () -> Unit,
)

@Composable
internal fun SaveCancelFormConfirmButtons(
    onSaveClicked: () -> Unit,
    onCancelClicked: () -> Unit,
) {
    val saveText = stringResource(OpenOtpResources.strings.save_button_name)
    val cancelText = stringResource(OpenOtpResources.strings.cancel_button_name)
    FormConfirmButtons(
        confirm = FormConfirmButtonData(
            text = saveText,
            contentDescription = saveText,
            imageVector = Icons.Outlined.Save,
            onClick = onSaveClicked,
        ),
        cancel = FormConfirmButtonData(
            text = cancelText,
            contentDescription = cancelText,
            imageVector = Icons.Outlined.Cancel,
            onClick = onCancelClicked,
        )
    )
}
