package ml.dev.kotlin.openotp.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.icerock.moko.resources.compose.stringResource
import ml.dev.kotlin.openotp.component.LinkAccountComponent
import ml.dev.kotlin.openotp.shared.OpenOtpResources
import ml.dev.kotlin.openotp.ui.OtpIcons
import ml.dev.kotlin.openotp.ui.component.FORM_BUTTON_PADDING
import ml.dev.kotlin.openotp.ui.component.FORM_BUTTON_SPACE
import ml.dev.kotlin.openotp.ui.component.FormField
import ml.dev.kotlin.openotp.ui.component.FormFieldButtonType
import ml.dev.kotlin.openotp.ui.icons.Dropbox
import ml.dev.kotlin.openotp.util.TopBarClickableIconScreen

@Composable
internal fun LinkAccountScreen(
    component: LinkAccountComponent,
) {
    TopBarClickableIconScreen(
        onIconClick = component::onCancel,
        contentAlignment = Alignment.Center,
    ) {
        val shouldEnterCode by component.shouldEnterCode.subscribeAsState()
        val uriHandler = LocalUriHandler.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
        ) {
            Icon(
                imageVector = OtpIcons.Dropbox,
                contentDescription = "icon",
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.secondary,
            )
            OutlinedButton(onClick = { component.onRequestAppPermissions(uriHandler) }) {
                Text(stringResource(OpenOtpResources.strings.request_app_permissions))
            }
            AnimatedVisibility(
                visible = shouldEnterCode,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    val code by component.code.subscribeAsState()
                    val isErrorGettingToken by component.isErrorGettingToken.subscribeAsState()
                    FormField(
                        name = "Access code",
                        text = code,
                        isError = isErrorGettingToken,
                        onTextChange = component::onUserAccessCodeChanged,
                        buttonType = FormFieldButtonType.Done,
                    )
                    Button(
                        onClick = { component.onUserAccessCodeConfirmed() },
                        contentPadding = FORM_BUTTON_PADDING,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "link",
                            modifier = Modifier.rotate(-45f)
                        )
                        Spacer(Modifier.width(FORM_BUTTON_SPACE))
                        Text("Link account")
                    }
                    val isLoadingAppPermissions by component.isLoadingAppPermissions.subscribeAsState()
                    AnimatedVisibility(visible = isLoadingAppPermissions) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
