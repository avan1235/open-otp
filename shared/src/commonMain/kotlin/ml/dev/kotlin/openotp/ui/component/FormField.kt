package ml.dev.kotlin.openotp.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import dev.icerock.moko.resources.compose.stringResource
import ml.dev.kotlin.openotp.shared.OpenOtpResources

@Composable
internal fun FormField(
    name: String,
    text: String,
    onTextChange: (String) -> Unit,
    isError: Boolean = false,
    buttonType: FormFieldButtonType = FormFieldButtonType.Next,
    password: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current
    var showPassword by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        isError = isError,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = name) },
        shape = MaterialTheme.shapes.medium,
        singleLine = true,
        visualTransformation = if (password && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType).run {
            if (password) copy(keyboardType = KeyboardType.Password) else this
        }.copy(imeAction = buttonType.imeAction),
        keyboardActions = KeyboardActions(
            onNext = on(buttonType == FormFieldButtonType.Next) { focusManager.moveFocus(FocusDirection.Down) },
            onDone = on(buttonType == FormFieldButtonType.Done) { focusManager.clearFocus() },
        ),
        trailingIcon = trailingIcon ?: if (password) {
            { PasswordIcon(showPassword, onIconClick = { showPassword = !showPassword }) }
        } else null,
    )
}

enum class FormFieldButtonType(val imeAction: ImeAction) { Next(ImeAction.Next), Done(ImeAction.Done) }

private fun <A> on(
    condition: Boolean,
    action: (A) -> Unit,
): (A) -> Unit = if (condition) action else fun(_: A) {}

@Composable
private fun PasswordIcon(
    showPassword: Boolean,
    onIconClick: () -> Unit,
) {
    IconButton(onClick = onIconClick) {
        Icon(
            imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
            contentDescription = stringResource(OpenOtpResources.strings.password_field_name)
        )
    }
}