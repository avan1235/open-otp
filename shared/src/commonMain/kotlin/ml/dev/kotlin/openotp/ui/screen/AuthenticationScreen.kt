package ml.dev.kotlin.openotp.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import ml.dev.kotlin.openotp.shared.OpenOtpResources
import ml.dev.kotlin.openotp.ui.OtpIcons
import ml.dev.kotlin.openotp.ui.component.LoadingAnimatedVisibility
import ml.dev.kotlin.openotp.ui.icons.OpenOtp
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
internal fun AuthenticationScreen(
    authenticated: Boolean,
    onAuthenticate: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    LoadingAnimatedVisibility(
        visibleContent = authenticated,
        loadingVerticalArrangement = Arrangement.SpaceEvenly,
        precedingContent = {
            Image(
                imageVector = OtpIcons.OpenOtp,
                contentDescription = stringResource(OpenOtpResources.strings.app_icon),
                modifier = Modifier.size(128.dp)
            )
        },
        centerContent = null,
        followingContent = {
            Button(
                onClick = onAuthenticate,
                contentPadding = PaddingValues(
                    horizontal = 20.dp,
                    vertical = 12.dp,
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = stringResource(OpenOtpResources.strings.locked_icon_name)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(OpenOtpResources.strings.authenticate_request),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        content = content,
    )
}