package ml.dev.kotlin.openotp.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
internal fun NamedSwitch(
    name: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector? = null,
    nameModifier: Modifier = Modifier.wrapContentWidth(),
    contentModifier: Modifier = Modifier.wrapContentWidth(),
) {
    NamedBox(
        name = name,
        icon = icon,
        nameModifier = nameModifier,
        contentModifier = contentModifier,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Switch(checked, onCheckedChange)
        }
    }
}
