package ml.dev.kotlin.openotp.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
expect fun ClickableIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    contentColor: Color = LocalContentColor.current,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
)
