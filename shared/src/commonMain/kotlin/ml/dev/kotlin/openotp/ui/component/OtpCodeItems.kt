package ml.dev.kotlin.openotp.ui.component

import androidx.compose.animation.VectorConverter
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.material3.DismissDirection.EndToStart
import androidx.compose.material3.DismissDirection.StartToEnd
import androidx.compose.material3.DismissValue.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ml.dev.kotlin.openotp.otp.HotpData
import ml.dev.kotlin.openotp.otp.OtpData
import ml.dev.kotlin.openotp.otp.TotpData
import ml.dev.kotlin.openotp.otp.UserOtpCodeData
import ml.dev.kotlin.openotp.ui.providerIcon
import ml.dev.kotlin.openotp.util.letTrue
import kotlin.math.roundToInt

@Composable
internal fun OtpCodeItems(
    codeData: UserOtpCodeData,
    timestamp: Long,
    onOtpCodeDataDismiss: (OtpData) -> Boolean,
    onRestartCode: (OtpData) -> Unit,
    listState: LazyListState = rememberLazyListState(),
) {
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp)
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    coroutineScope.launch {
                        listState.scrollBy(-delta)
                    }
                },
            ),
    ) {
        items(
            items = codeData,
            key = { it.uuid },
        ) { item ->
            val currentItem by rememberUpdatedState(item)
            val currentTimestamp by rememberUpdatedState(timestamp)
            val localClipboardManager = LocalClipboardManager.current
            val dismissState = rememberDismissState(
                confirmValueChange = {
                    when (it) {
                        DismissedToEnd -> localClipboardManager.copyOtpCode(currentItem, currentTimestamp).letTrue()
                        DismissedToStart -> onOtpCodeDataDismiss(currentItem)

                        else -> false
                    }
                },
            )
            if (dismissState.isDismissed(StartToEnd)) {
                LaunchedEffect(Unit) { dismissState.reset() }
            }
            SwipeToDismiss(
                state = dismissState,
                modifier = Modifier.animateItemPlacement(),
                background = { DismissBackground(dismissState) },
                dismissContent = {
                    OtpCodeItem(
                        timestamp = currentTimestamp,
                        item = currentItem,
                        onClick = {
                            val code = currentItem.code(currentTimestamp)
                            localClipboardManager.setText(AnnotatedString(code))
                        },
                        onRestartCode = { onRestartCode(currentItem) },
                    )
                }
            )
        }
    }
}

@Composable
private fun OtpCodeItem(
    timestamp: Long,
    item: OtpData,
    onClick: () -> Unit,
    onRestartCode: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.padding(
            top = 0.dp,
            bottom = 12.dp,
            start = 12.dp,
            end = 12.dp,
        ),
        onClick = onClick,
    ) {
        val trailingData = when (item) {
            is HotpData -> TrailingData.Restart
            is TotpData -> {
                val initialColor = MaterialTheme.colorScheme.primary
                val targetColor = MaterialTheme.colorScheme.error
                val color = remember {
                    Animatable(
                        initialValue = initialColor,
                        typeConverter = Color.VectorConverter(ColorSpaces.LinearSrgb),
                    )
                }
                val left = remember(timestamp, item) { item.timeslotLeft(timestamp) }
                LaunchedEffect(item) {
                    color.animateTo(
                        targetValue = targetColor,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = item.periodMillis,
                                easing = LinearEasing,
                            ),
                            repeatMode = RepeatMode.Restart,
                            initialStartOffset = StartOffset(
                                offsetMillis = ((1.0 - left) * item.periodMillis).roundToInt(),
                                offsetType = StartOffsetType.FastForward,
                            )
                        )
                    )
                }
                TrailingData.CountDown(color, left)
            }
        }

        ListItem(
            overlineContent = content@{
                val issuer = item.issuer?.takeIf { it.isNotBlank() }
                val accountName = item.accountName?.takeIf { it.isNotBlank() }
                val text = when {
                    issuer != null && accountName != null -> "$issuer: $accountName"
                    issuer != null -> issuer
                    accountName != null -> accountName
                    else -> return@content
                }
                Text(
                    text = text,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            headlineContent = {
                when (trailingData) {
                    is TrailingData.CountDown -> Text(
                        text = item.codePresentation(timestamp),
                        color = trailingData.color.value,
                        fontSize = 24.sp
                    )

                    is TrailingData.Restart -> Text(
                        text = item.codePresentation(timestamp),
                        fontSize = 24.sp
                    )
                }
            },
            leadingContent = {
                Icon(
                    imageVector = item.issuer.providerIcon,
                    contentDescription = "provider",
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape,
                        )
                        .padding(12.dp),
                )
            },
            trailingContent = {
                when (trailingData) {
                    is TrailingData.CountDown -> CountDownStatusCircle(
                        percent = trailingData.percent.toFloat(),
                        color = trailingData.color.value,
                    )

                    TrailingData.Restart -> RestartButton(
                        onClick = {
                            onClick()
                            onRestartCode()
                        }
                    )
                }
            },
        )
    }
}

private sealed interface TrailingData {
    data object Restart : TrailingData
    data class CountDown(
        val color: Animatable<Color, AnimationVector4D>,
        val percent: Double,
    ) : TrailingData
}

@Composable
private fun RestartButton(
    onClick: () -> Unit,
    size: Dp = 36.dp,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(size),
    ) {
        Icon(
            imageVector = Icons.Default.RestartAlt,
            contentDescription = "restart",
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun CountDownStatusCircle(
    percent: Float,
    color: Color,
    size: Dp = 28.dp,
    borderSize: Dp = 2.dp,
    padding: PaddingValues = PaddingValues(horizontal = 3.dp, vertical = 5.dp)
) {
    val sizePx = with(LocalDensity.current) { size.toPx() }
    val borderSizePx = with(LocalDensity.current) { borderSize.toPx() }
    Box(
        modifier = Modifier.padding(padding)
    ) {
        Canvas(
            modifier = Modifier.size(size),
        ) {
            drawCircle(
                color = color,
                style = Stroke(borderSizePx),
            )
            drawArc(
                color = color,
                startAngle = 270f,
                sweepAngle = percent * 360f,
                useCenter = true,
                size = Size(sizePx, sizePx),
            )
        }
    }
}

private fun OtpData.codePresentation(timestamp: Long): String {
    val code = code(timestamp)
    val length = code.length
    val halfLength = length / 2
    return code.substring(0..<halfLength) + " " + code.substring(halfLength..<length)
}

@Composable
private fun DismissBackground(dismissState: DismissState) {
    val direction = dismissState.dismissDirection ?: return

    val color by animateColorAsState(
        when (dismissState.targetValue) {
            Default -> MaterialTheme.colorScheme.surface
            DismissedToEnd -> MaterialTheme.colorScheme.secondaryContainer
            DismissedToStart -> MaterialTheme.colorScheme.errorContainer
        }
    )
    val alignment = when (direction) {
        StartToEnd -> Alignment.CenterStart
        EndToStart -> Alignment.CenterEnd
    }
    val icon = when (direction) {
        StartToEnd -> Icons.Default.ContentCopy
        EndToStart -> Icons.Default.Delete
    }
    val contentDescription = when (direction) {
        StartToEnd -> "copy"
        EndToStart -> "delete"
    }
    val scale by animateFloatAsState(
        targetValue = if (dismissState.targetValue == Default) 0.75f else 1f
    )
    Box(
        Modifier
            .fillMaxSize()
            .padding(bottom = 12.dp)
            .background(color)
            .padding(horizontal = 36.dp),
        contentAlignment = alignment
    ) {
        Icon(icon, contentDescription, Modifier.scale(scale))
    }
}

private fun ClipboardManager.copyOtpCode(item: OtpData, timestamp: Long) {
    val code = item.code(timestamp)
    val string = AnnotatedString(code)
    setText(string)
}