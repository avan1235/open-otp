package ml.dev.kotlin.openotp.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
actual fun ClickableIconButton(
    onClick: () -> Unit,
    modifier: Modifier,
    containerColor: Color,
    contentColor: Color,
    interactionSource: MutableInteractionSource,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(40.0.dp)
            .clip(CircleShape)
            .background(containerColor)
            .indication(
                interactionSource, indication = rememberRipple(
                    bounded = false,
                    radius = 20.dp,
                )
            )
            .pointerHoverIcon(PointerIcon.Default)
            .hoverable(enabled = true, interactionSource = interactionSource)
            .focusable(enabled = true, interactionSource = interactionSource)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClick() })
            },
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor, content = content)
    }
}
