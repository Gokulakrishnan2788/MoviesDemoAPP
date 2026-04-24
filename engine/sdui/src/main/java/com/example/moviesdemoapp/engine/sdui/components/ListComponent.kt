package com.example.moviesdemoapp.engine.sdui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.example.moviesdemoapp.engine.sdui.applyAccessibility
import androidx.compose.ui.zIndex
import com.example.moviesdemoapp.core.network.model.ComponentNode
import com.example.moviesdemoapp.core.ui.DesignTokens
import kotlin.math.roundToInt

@Composable
internal fun RenderList(
    node: ComponentNode,
    data: Map<String, String>,
    listData: Map<String, List<Map<String, String>>>,
    onAction: (String, Map<String, String>) -> Unit,
    renderNode: NodeRenderer,
) {
    val binding = node.listDataBinding ?: return
    val items   = listData[binding] ?: emptyList()
    val layout  = node.itemLayout ?: return
    val spacing = node.style?.spacing?.dp ?: 0.dp
    val spacingPx = with(LocalDensity.current) { spacing.toPx() }

    // Drag state — shared across all items in this list
    var draggingIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetY   by remember { mutableStateOf(0f) }
    // Height of each item in px, populated via onGloballyPositioned
    val itemHeights = remember { mutableStateMapOf<Int, Float>() }

    // Index where the dragged item would land at current offset
    val targetIndex = if (draggingIndex >= 0 && itemHeights.isNotEmpty() && items.isNotEmpty()) {
        val cellH = (itemHeights[draggingIndex] ?: itemHeights.values.average().toFloat()) + spacingPx
        (draggingIndex + (dragOffsetY / cellH).roundToInt()).coerceIn(items.indices)
    } else -1

    Column(
        verticalArrangement = if (spacing > 0.dp) Arrangement.spacedBy(spacing) else Arrangement.Top,
    ) {
        items.forEachIndexed { index, itemData ->
            val isDragging = index == draggingIndex
            val cellH      = (itemHeights[draggingIndex] ?: 0f) + spacingPx

            // How far to visually shift this item so others "make room" for the dragged item
            val translationY = when {
                isDragging -> dragOffsetY
                targetIndex >= 0 && draggingIndex >= 0 -> when {
                    draggingIndex < targetIndex && index in (draggingIndex + 1)..targetIndex -> -cellH
                    draggingIndex > targetIndex && index in targetIndex until draggingIndex  ->  cellH
                    else -> 0f
                }
                else -> 0f
            }

            // key() ensures Compose tracks each item by identity, not position,
            // so composable state stays correct when the list reorders.
            key(itemData["id"] ?: index.toString()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .applyAccessibility(node.screenAccessibility, data + itemData)
                        .onGloballyPositioned { itemHeights[index] = it.size.height.toFloat() }
                        .zIndex(if (isDragging) 1f else 0f)
                        .graphicsLayer {
                            this.translationY = translationY
                            if (isDragging) { scaleX = 1.03f; scaleY = 1.03f }
                        }
                        .pointerInput(index) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { draggingIndex = index; dragOffsetY = 0f },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffsetY += dragAmount.y
                                },
                                onDragEnd = {
                                    if (draggingIndex >= 0 && itemHeights.isNotEmpty() && items.isNotEmpty()) {
                                        val h = (itemHeights[draggingIndex]
                                            ?: itemHeights.values.average().toFloat()) + spacingPx
                                        val to = (draggingIndex + (dragOffsetY / h).roundToInt())
                                            .coerceIn(items.indices)
                                        if (to != draggingIndex) {
                                            onAction(
                                                "reorder",
                                                mapOf(
                                                    "binding" to binding,
                                                    "from"    to draggingIndex.toString(),
                                                    "to"      to to.toString(),
                                                ),
                                            )
                                        }
                                    }
                                    draggingIndex = -1; dragOffsetY = 0f
                                },
                                onDragCancel = { draggingIndex = -1; dragOffsetY = 0f },
                            )
                        },
                    contentAlignment = Alignment.CenterStart,
                ) {
                    renderNode(layout, data + itemData, listData, onAction)
                    // Subtle drag handle hint at the trailing edge
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "Drag to reorder",
                        tint = DesignTokens.SecondaryText.copy(alpha = 0.35f),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                            .size(18.dp)
                            .clearAndSetSemantics {},
                    )
                }
            }
        }
    }
}
