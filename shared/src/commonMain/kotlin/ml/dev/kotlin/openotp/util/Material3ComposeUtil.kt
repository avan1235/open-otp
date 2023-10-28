package ml.dev.kotlin.openotp.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import ml.dev.kotlin.openotp.shared.OpenOtpResources
import ml.dev.kotlin.openotp.ui.component.ClickableIconButton
import `in`.procyk.compose.util.SystemBarsScreen as NoDefaultsSystemBarsScreen

@Composable
internal fun SystemBarsScreen(
    top: Color = MaterialTheme.colorScheme.background,
    bottom: Color = MaterialTheme.colorScheme.background,
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    NoDefaultsSystemBarsScreen(top, bottom, contentAlignment, propagateMinConstraints, content)
}

@Composable
internal fun TopBarClickableIconScreen(
    onIconClick: () -> Unit,
    accent: Color = MaterialTheme.colorScheme.background,
    text: String? = null,
    height: Dp = 64.dp,
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = false,
    content: @Composable (PaddingValues) -> Unit,
) {
    `in`.procyk.compose.util.SystemBarsScreen(
        top = accent,
        bottom = MaterialTheme.colorScheme.background,
        contentAlignment = contentAlignment,
        propagateMinConstraints = propagateMinConstraints,
    ) {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height)
                        .background(accent),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ClickableIconButton(onClick = onIconClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(OpenOtpResources.strings.back_icon_name),
                            tint = contentColorFor(accent),
                        )
                    }
                    if (text != null) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.titleLarge,
                            color = contentColorFor(accent)
                        )
                    }
                }
            },
            content = content,
        )
    }

}
