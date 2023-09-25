package ml.dev.kotlin.openotp.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import ml.dev.kotlin.openotp.shared.OpenOtpResources
import ml.dev.kotlin.openotp.ui.theme.Shapes
import ml.dev.kotlin.openotp.ui.theme.Typography
import ml.dev.kotlin.openotp.util.Named

@Composable
internal fun <T : Named> NamedDropdownMenu(
    name: String,
    selected: T,
    onSelected: (T) -> Unit,
    anyItems: Collection<T>,
) {
    NamedBox(name) {
        val dropDownWidth = maxWidth
        val items = anyItems.toList()
        var expanded by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = selected.presentableName(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Shapes.medium)
                    .clickable(onClick = { expanded = true })
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(16.dp),
                color = contentColorFor(MaterialTheme.colorScheme.secondary),
                style = Typography.titleMedium,
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = stringResource(OpenOtpResources.strings.dropdown_icon_name),
                modifier = Modifier.padding(16.dp),
                tint = contentColorFor(MaterialTheme.colorScheme.secondary),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.tertiary)
                .width(dropDownWidth)
        ) {
            items.forEach {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = it.presentableName(),
                            color = contentColorFor(MaterialTheme.colorScheme.tertiary),
                            style = Typography.titleMedium,
                        )
                    },
                    onClick = {
                        onSelected(it)
                        expanded = false
                    })
            }
        }
    }
}