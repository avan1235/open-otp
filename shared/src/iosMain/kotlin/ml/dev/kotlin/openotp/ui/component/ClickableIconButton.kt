package ml.dev.kotlin.openotp.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun ClickableIconButton(
    onClick: () -> Unit,
    modifier: Modifier,
    containerColor: Color,
    contentColor: Color,
    interactionSource: MutableInteractionSource,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = true,
        colors = IconButtonDefaults.iconButtonColors(containerColor, contentColor, containerColor, contentColor),
        interactionSource = interactionSource,
        content = content
    )
}
