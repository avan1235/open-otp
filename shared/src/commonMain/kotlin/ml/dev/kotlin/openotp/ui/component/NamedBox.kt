package ml.dev.kotlin.openotp.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
internal fun NamedBox(
    name: String,
    icon: ImageVector? = null,
    nameModifier: Modifier = Modifier.fillMaxWidth(0.5f),
    contentModifier: Modifier = Modifier.fillMaxWidth(),
    content: @Composable BoxWithConstraintsScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(nameModifier) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(28.dp)
                    )
                }
                Text(name)
            }
        }
        BoxWithConstraints(
            modifier = contentModifier,
            content = content,
        )
    }
}