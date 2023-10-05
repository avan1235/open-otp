package ml.dev.kotlin.openotp.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ml.dev.kotlin.openotp.util.runIfNonNull
import kotlin.math.roundToInt

@Composable
internal fun <T : Any> DragDropList(
    items: List<T>,
    key: (T) -> Any,
    modifier: Modifier,
    enabled: Boolean,
    dragDropState: DragDropState,
    itemContent: @Composable LazyItemScope.(item: T, modifier: Modifier) -> Unit,
) {
    var overscrollJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = when {
            !enabled -> modifier
            else -> modifier
                .pointerInput(dragDropState) {
                    detectDragGesturesAfterLongPress(
                        onDrag = { change, offset ->
                            change.consume()
                            dragDropState.onDrag(offset = offset)

                            if (overscrollJob?.isActive == true)
                                return@detectDragGesturesAfterLongPress

                            when (val overscroll = dragDropState.findOverScroll()) {
                                null -> overscrollJob?.cancel()
                                else -> overscrollJob = scope.launch {
                                    dragDropState.listState.animateScrollBy(
                                        value = overscroll * 1.3f,
                                        animationSpec = tween(easing = FastOutLinearInEasing),
                                    )
                                }
                            }
                        },
                        onDragStart = dragDropState::onDragStart,
                        onDragEnd = {
                            dragDropState.onDragInterrupted()
                            overscrollJob?.cancel()
                        },
                        onDragCancel = {
                            dragDropState.onDragInterrupted()
                            overscrollJob?.cancel()
                        }
                    )
                }
        },
        state = dragDropState.listState,
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> key(item) }
        ) { index, item ->
            DraggableItem(
                dragDropState = dragDropState,
                index = index,
            ) { modifier ->
                itemContent(item, modifier)
            }
        }
    }
}

@Composable
internal fun rememberDragDropState(
    lazyListState: LazyListState,
    onSwap: (Int, Int) -> Unit,
): DragDropState {
    val scope = rememberCoroutineScope()
    return remember(lazyListState) {
        DragDropState(
            listState = lazyListState,
            onSwap = onSwap,
            scope = scope
        )
    }
}

private fun LazyListState.getVisibleItemInfoFor(absoluteIndex: Int): LazyListItemInfo? = this
    .layoutInfo
    .visibleItemsInfo
    .getOrNull(absoluteIndex - this.layoutInfo.visibleItemsInfo.first().index)

private val LazyListItemInfo.offsetEnd: Int
    get() = this.offset + this.size

@Composable
private fun LazyItemScope.DraggableItem(
    dragDropState: DragDropState,
    index: Int,
    content: @Composable LazyItemScope.(modifier: Modifier) -> Unit,
) {
    val current by animateFloatAsState(dragDropState.draggingItemOffset)
    val previous by animateFloatAsState(dragDropState.previousItemOffset.value)

    val draggingModifier = when (index) {
        dragDropState.currentDraggedItemIndex -> Modifier
            .zIndex(1f)
            .graphicsLayer { translationY = current }

        dragDropState.preciousDraggedItemIndex -> Modifier
            .zIndex(1f)
            .graphicsLayer { translationY = previous }

        else -> Modifier.animateItemPlacement(
            animationSpec = tween(easing = FastOutLinearInEasing)
        )
    }
    content(draggingModifier)
}

internal class DragDropState(
    val listState: LazyListState,
    private val scope: CoroutineScope,
    private val onSwap: (Int, Int) -> Unit,
) {
    private var draggedDistance by mutableStateOf(0f)

    private var draggingItemInitialOffset by mutableStateOf(0)

    var preciousDraggedItemIndex: Int? by mutableStateOf(null)
        private set

    val previousItemOffset: Animatable<Float, AnimationVector1D> = Animatable(0f)

    private var initiallyDraggedElement: LazyListItemInfo? by mutableStateOf(null)

    var currentDraggedItemIndex: Int? by mutableStateOf(null)

    val draggingItemOffset: Float
        get() = draggingItemLayoutInfo?.let { item ->
            draggingItemInitialOffset + draggedDistance - item.offset
        } ?: 0f

    private val draggingItemLayoutInfo: LazyListItemInfo?
        get() = listState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == currentDraggedItemIndex }

    fun onDragStart(offset: Offset) {
        val dragged = listState.layoutInfo.visibleItemsInfo
            .firstOrNull { item -> offset.y.roundToInt() in item.offset..item.offset + item.size }
            ?: return
        this.currentDraggedItemIndex = dragged.index
        this.initiallyDraggedElement = dragged
        this.draggingItemInitialOffset = dragged.offset
    }

    fun onDragInterrupted() {
        val currentIndexOfDraggedItem = this.currentDraggedItemIndex
        if (currentIndexOfDraggedItem != null) {
            preciousDraggedItemIndex = currentIndexOfDraggedItem
            scope.launch {
                previousItemOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(easing = FastOutLinearInEasing)
                )
                preciousDraggedItemIndex = null
            }
        }
        this.draggingItemInitialOffset = 0
        this.draggedDistance = 0f
        this.currentDraggedItemIndex = null
        this.initiallyDraggedElement = null
    }

    fun onDrag(offset: Offset) {
        draggedDistance += offset.y

        val initiallyDraggedElement = initiallyDraggedElement ?: return

        val topOffset = initiallyDraggedElement.offset
        val bottomOffset = initiallyDraggedElement.offsetEnd
        val startOffset = topOffset + draggedDistance
        val endOffset = bottomOffset + draggedDistance

        val hoveredIndex = currentDraggedItemIndex ?: return
        val hovered = listState.getVisibleItemInfoFor(absoluteIndex = hoveredIndex) ?: return

        val item = listState.layoutInfo.visibleItemsInfo
            .filterNot { item -> item.offsetEnd < startOffset || item.offset > endOffset || hovered.index == item.index }
            .firstOrNull { item ->
                val delta = startOffset - hovered.offset
                when {
                    delta > 0 -> endOffset > item.offsetEnd
                    else -> startOffset < item.offset
                }
            } ?: return
        currentDraggedItemIndex?.let { onSwap(it, item.index) }
        currentDraggedItemIndex = item.index
    }

    fun findOverScroll(): Float? = runIfNonNull(initiallyDraggedElement) {
        val startOffset = it.offset + draggedDistance
        val endOffset = it.offsetEnd + draggedDistance
        when {
            draggedDistance > 0 -> (endOffset - listState.layoutInfo.viewportEndOffset + 50f).takeIf { it > 0 }
            draggedDistance < 0 -> (startOffset - listState.layoutInfo.viewportStartOffset - 50f).takeIf { it < 0 }
            else -> null
        }
    }
}