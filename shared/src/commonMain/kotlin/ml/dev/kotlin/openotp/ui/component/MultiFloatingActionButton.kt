package ml.dev.kotlin.openotp.ui.component

import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun <Id> MultiFloatingActionButton(
    modifier: Modifier = Modifier,
    items: List<MultiFabItem<Id>>,
    fabIcon: FabIcon,
    expanded: Boolean,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = contentColorFor(backgroundColor = containerColor),
    onFabItemClicked: (fabItem: Id) -> Unit,
    stateChanged: (fabState: MultiFabState) -> Unit = {},
    fabState: MutableState<MultiFabState> = rememberMultiFabState(),
) {
    val rotation by animateFloatAsState(
        if (fabState.value.isExpanded()) fabIcon.iconRotate ?: 0f else 0f
    )
    Box(
        modifier = modifier.wrapContentSize(),
        contentAlignment = Alignment.BottomEnd,
    ) {
        items.asReversed().forEachIndexed { index, item ->
            MiniFabItem(
                item = item,
                index = items.lastIndex - index,
                onFabItemClicked = onFabItemClicked,
                containerColor = containerColor,
                contentColor = contentColor,
                fabState = fabState.value,
            )
        }

        ExtendedFloatingActionButton(
            shape = RoundedCornerShape(16.dp),
            onClick = {
                fabState.value = fabState.value.toggle()
                stateChanged(fabState.value)
            },
            containerColor = containerColor,
            contentColor = contentColor,
            icon = {
                Icon(
                    imageVector = fabIcon.icon,
                    contentDescription = fabIcon.contentDescription,
                    modifier = Modifier.rotate(rotation),
                    tint = contentColor
                )
            },
            text = {
                Text(fabIcon.text(fabState.value))
            },
            expanded = expanded,
        )
    }
}

@Composable
private fun <Id> MiniFabItem(
    item: MultiFabItem<Id>,
    index: Int,
    containerColor: Color,
    contentColor: Color,
    fabState: MultiFabState,
    onFabItemClicked: (id: Id) -> Unit,
    size: Dp = 40.dp,
    spacedBy: Dp = 8.dp,
) {
    val elevation by animateDpAsState(
        targetValue = if (fabState.isExpanded()) 6.dp else 0.dp
    )
    val y by animateDpAsState(
        targetValue = if (fabState.isExpanded()) 56.dp + spacedBy + (size + spacedBy) * index else 8.dp,
        animationSpec = spring(visibilityThreshold = Dp.VisibilityThreshold)
    )
    FloatingActionButton(
        shape = RoundedCornerShape(12.dp),
        onClick = { onFabItemClicked(item.id) },
        modifier = Modifier
            .size(size)
            .absoluteOffset(x = -1.dp, y = -y),
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = elevation,
            pressedElevation = elevation,
            focusedElevation = elevation,
            hoveredElevation = elevation,
        ),
        containerColor = containerColor,
        contentColor = contentColor,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.contentDescription,
            tint = contentColor
        )
    }
}

enum class MultiFabState {
    Collapsed, Expand;

    fun isExpanded() = this == Expand

    fun toggle() = if (isExpanded()) Collapsed else Expand
}

@Composable
internal fun rememberMultiFabState(): MutableState<MultiFabState> = remember { mutableStateOf(MultiFabState.Collapsed) }

class MultiFabItem<Id>(
    val id: Id,
    val icon: ImageVector,
    val contentDescription: String? = null,
)

@Immutable
interface FabIcon {
    val icon: ImageVector
    val text: (MultiFabState) -> String
    val contentDescription: String?
    val iconRotate: Float?
}

private class FabIconImpl(
    override val icon: ImageVector,
    override val text: (MultiFabState) -> String,
    override val contentDescription: String?,
    override val iconRotate: Float?
) : FabIcon

fun FabIcon(
    icon: ImageVector,
    text: (MultiFabState) -> String,
    contentDescription: String? = null,
    iconRotate: Float? = null,
): FabIcon = FabIconImpl(icon, text, contentDescription, iconRotate)

