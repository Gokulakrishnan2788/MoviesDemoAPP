package com.example.moviesdemoapp.engine.sdui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.example.moviesdemoapp.core.ui.DesignTokens
import com.example.moviesdemoapp.core.ui.colorFromToken
import com.example.moviesdemoapp.engine.sdui.ActionModel
import com.example.moviesdemoapp.engine.sdui.ComponentNode
import com.example.moviesdemoapp.engine.sdui.TemplateResolver
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/** Recursive render callback — must be called from a @Composable context. */
typealias NodeRenderer =
    @Composable (node: ComponentNode, data: Map<String, String>, listData: Map<String, List<Map<String, String>>>, onAction: (String, Map<String, String>) -> Unit) -> Unit

/**
 * Built-in rendering implementations for every standard SDUI component type.
 * Actions are forwarded as (actionId, params) strings — the ViewModel decides what to do.
 */
@Singleton
class SDUIComponents @Inject constructor(private val resolver: TemplateResolver) {

    @Composable
    fun RenderBuiltIn(
        node: ComponentNode,
        data: Map<String, String>,
        listData: Map<String, List<Map<String, String>>>,
        onAction: (String, Map<String, String>) -> Unit,
        renderNode: NodeRenderer,
    ) {
        // Visibility check
        if (!resolver.isVisible(node, data)) return

        when (node.type) {
            "topBar"         -> RenderTopBar(node, data, onAction)
            "column"         -> RenderColumn(node, data, listData, onAction, renderNode)
            "row"            -> RenderRow(node, data, listData, onAction, renderNode)
            "card"           -> RenderCard(node, data, listData, onAction, renderNode)
            "spacer"         -> RenderSpacer(node)
            "divider"        -> HorizontalDivider(color = DesignTokens.Surface, thickness = 1.dp)
            "text"           -> RenderText(node, data)
            "header"         -> RenderHeader(node, data, onAction)
            "image"          -> RenderImage(node, data)
            "icon"           -> RenderIcon(node)
            "button"         -> RenderButton(node, data, onAction)
            "list"           -> RenderList(node, data, listData, onAction, renderNode)
            "generatedList"  -> RenderGeneratedList(node, data, listData, onAction, renderNode)
            else -> Box(Modifier.padding(DesignTokens.SpacingMd)) {
                Text("[unknown: ${node.type}]", color = DesignTokens.Accent)
            }
        }
    }

    // ─── TopBar ───────────────────────────────────────────────────────────────

    @Composable
    private fun RenderTopBar(
        node: ComponentNode,
        data: Map<String, String>,
        onAction: (String, Map<String, String>) -> Unit,
    ) {
        val title = node.props["title"]
            ?: node.titleTemplate?.let { resolver.resolve(it, data) } ?: ""
        val subtitle = node.props["subtitle"]
            ?: node.subtitleTemplate?.let { resolver.resolve(it, data) }
        val hasBack = node.props["leadingIcon"] == "back"
        val hasSearch = node.props["trailingIcon"] == "search"
        val padH = node.style?.padding?.dp ?: DesignTokens.SpacingMd
        val padTop = node.style?.paddingTop?.dp ?: DesignTokens.SpacingSm
        val padBottom = node.style?.paddingBottom?.dp ?: DesignTokens.SpacingSm

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = padH, end = padH, top = padTop, bottom = padBottom),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (hasBack) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { node.action?.dispatch(data, onAction) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = DesignTokens.PrimaryText,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
                Text(
                    text = title,
                    color = DesignTokens.PrimaryText,
                    fontSize = DesignTokens.TextXxl,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(4f),
                )
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    if (hasSearch) {
                        IconButton(onClick = { node.action?.dispatch(data, onAction) }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = DesignTokens.PrimaryText)
                        }
                    }
                }
            }
            if (!subtitle.isNullOrEmpty()) {
                Text(
                    text = subtitle,
                    color = DesignTokens.SecondaryText,
                    fontSize = DesignTokens.TextMd,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = DesignTokens.SpacingXs),
                )
            }
        }
    }

    // ─── Layout ───────────────────────────────────────────────────────────────

    @Composable
    private fun RenderColumn(
        node: ComponentNode,
        data: Map<String, String>,
        listData: Map<String, List<Map<String, String>>>,
        onAction: (String, Map<String, String>) -> Unit,
        renderNode: NodeRenderer,
    ) {
        val bg = node.style?.backgroundColor?.let { colorFromToken(it) }
        val pad = node.style?.padding?.dp ?: 0.dp
        val spacing = node.style?.spacing?.dp ?: 0.dp
        val radius = node.style?.cornerRadius?.dp ?: 0.dp
        var mod: Modifier = Modifier.fillMaxWidth()
        if (bg != null) mod = mod.background(bg, RoundedCornerShape(radius))
        if (pad > 0.dp) mod = mod.padding(pad)
        Column(
            modifier = mod,
            verticalArrangement = if (spacing > 0.dp) Arrangement.spacedBy(spacing) else Arrangement.Top,
        ) {
            node.children.forEach { renderNode(it, data, listData, onAction) }
        }
    }

    @Composable
    private fun RenderRow(
        node: ComponentNode,
        data: Map<String, String>,
        listData: Map<String, List<Map<String, String>>>,
        onAction: (String, Map<String, String>) -> Unit,
        renderNode: NodeRenderer,
    ) {
        val bg = node.style?.backgroundColor?.let { colorFromToken(it) }
        val pad = node.style?.padding?.dp ?: 0.dp
        val spacing = node.style?.spacing?.dp ?: DesignTokens.SpacingSm
        val radius = node.style?.cornerRadius?.dp ?: 0.dp
        var mod: Modifier = Modifier.fillMaxWidth()
        if (bg != null) mod = mod.background(bg, RoundedCornerShape(radius))
        if (pad > 0.dp) mod = mod.padding(pad)
        if (node.action != null) mod = mod.clickable { node.action.dispatch(data, onAction) }
        Row(modifier = mod, horizontalArrangement = Arrangement.spacedBy(spacing)) {
            node.children.forEach { renderNode(it, data, listData, onAction) }
        }
    }

    @Composable
    private fun RenderCard(
        node: ComponentNode,
        data: Map<String, String>,
        listData: Map<String, List<Map<String, String>>>,
        onAction: (String, Map<String, String>) -> Unit,
        renderNode: NodeRenderer,
    ) {
        val bg = node.style?.backgroundColor?.let { colorFromToken(it) } ?: DesignTokens.CardBackground
        val pad = node.style?.padding?.dp?.takeIf { it > 0.dp } ?: DesignTokens.SpacingMd
        val radius = node.style?.cornerRadius?.dp ?: DesignTokens.RadiusMd
        var mod = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.SpacingMd, vertical = DesignTokens.SpacingSm)
        if (node.action != null) mod = mod.clickable { node.action.dispatch(data, onAction) }
        Card(
            modifier = mod,
            shape = RoundedCornerShape(radius),
            colors = CardDefaults.cardColors(containerColor = bg),
        ) {
            Column(modifier = Modifier.padding(pad)) {
                node.children.forEach { renderNode(it, data, listData, onAction) }
            }
        }
    }

    @Composable
    private fun RenderSpacer(node: ComponentNode) {
        val h = node.props["height"]?.toFloatOrNull() ?: node.style?.spacing ?: 8f
        Spacer(modifier = Modifier.height(h.dp))
    }

    // ─── Text / Header ────────────────────────────────────────────────────────

    @Composable
    private fun RenderText(node: ComponentNode, data: Map<String, String>) {
        val text = node.template?.let { resolver.resolve(it, data) }
            ?: node.dataBinding?.let { data[it] }
            ?: node.text
            ?: node.props["text"] ?: ""
        val color = (node.style?.foregroundColor ?: node.style?.textColor)
            ?.let { colorFromToken(it) } ?: DesignTokens.PrimaryText
        val fontSize = node.style?.fontSize?.sp ?: DesignTokens.TextMd
        val fontWeight = node.style?.fontWeight.toFontWeight()
        val maxLines = node.style?.lineLimit ?: node.style?.maxLines ?: Int.MAX_VALUE
        val pad = node.style?.padding?.dp ?: 0.dp
        Text(
            text = text,
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = if (pad > 0.dp) Modifier.padding(pad) else Modifier,
        )
    }

    @Composable
    private fun RenderHeader(
        node: ComponentNode,
        data: Map<String, String>,
        onAction: (String, Map<String, String>) -> Unit,
    ) {
        val title = node.titleTemplate?.let { resolver.resolve(it, data) }
            ?: node.props["title"] ?: ""
        val subtitle = node.subtitleTemplate?.let { resolver.resolve(it, data) }
            ?: node.props["subtitle"]
        val hasSearch = node.action?.type == "search"
        val padH = node.style?.padding?.dp ?: DesignTokens.SpacingMd
        val padTop = node.style?.paddingTop?.dp ?: DesignTokens.SpacingSm
        val padBottom = node.style?.paddingBottom?.dp ?: DesignTokens.SpacingSm
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = padH, end = padH, top = padTop, bottom = padBottom),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = DesignTokens.PrimaryText, fontSize = DesignTokens.TextXxl, fontWeight = FontWeight.Bold)
                subtitle?.let { Text(text = it, color = DesignTokens.SecondaryText, fontSize = DesignTokens.TextMd) }
            }
            if (hasSearch) {
                IconButton(onClick = { node.action?.dispatch(data, onAction) }) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = DesignTokens.PrimaryText)
                }
            }
        }
    }

    // ─── Media ────────────────────────────────────────────────────────────────

    @Composable
    private fun RenderImage(node: ComponentNode, data: Map<String, String>) {
        val url = node.dataBinding?.let { data[it] } ?: node.props["url"] ?: ""
        val w = node.style?.frameWidth?.dp
        val h = node.style?.frameHeight?.dp ?: 200.dp
        val radius = node.style?.cornerRadius?.dp ?: 0.dp
        val mod = (if (w != null) Modifier.size(width = w, height = h)
        else Modifier.fillMaxWidth().height(h)).clip(RoundedCornerShape(radius))
        AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = mod)
    }

    @Composable
    private fun RenderIcon(node: ComponentNode) {
        val name = node.icon ?: node.props["icon"] ?: ""
        val color = node.style?.foregroundColor?.let { colorFromToken(it) } ?: DesignTokens.PrimaryText
        val size = node.style?.fontSize?.dp ?: 16.dp
        val icon = when {
            name.contains("search") -> Icons.Default.Search
            name.contains("play") || name.contains("tv") -> Icons.Default.PlayCircle
            else -> Icons.Default.Star
        }
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(size))
    }

    // ─── Input ────────────────────────────────────────────────────────────────

    @Composable
    private fun RenderButton(
        node: ComponentNode,
        data: Map<String, String>,
        onAction: (String, Map<String, String>) -> Unit,
    ) {
        val label = node.template?.let { resolver.resolve(it, data) } ?: node.props["label"] ?: ""
        Box(modifier = Modifier
            .padding(DesignTokens.SpacingMd)
            .clickable { node.action?.dispatch(data, onAction) }
        ) {
            Text(text = label, color = DesignTokens.Accent, fontSize = DesignTokens.TextLg)
        }
    }

    // ─── Data ─────────────────────────────────────────────────────────────────

    @Composable
    private fun RenderList(
        node: ComponentNode,
        data: Map<String, String>,
        listData: Map<String, List<Map<String, String>>>,
        onAction: (String, Map<String, String>) -> Unit,
        renderNode: NodeRenderer,
    ) {
        val binding = node.listDataBinding ?: return
        val items = listData[binding] ?: emptyList()
        val layout = node.itemLayout ?: return
        val spacing = node.style?.spacing?.dp ?: 0.dp
        val spacingPx = with(LocalDensity.current) { spacing.toPx() }

        // Drag state — shared across all items in this list
        var draggingIndex by remember { mutableIntStateOf(-1) }
        var dragOffsetY by remember { mutableStateOf(0f) }
        // Height of each item in px, populated via onGloballyPositioned
        val itemHeights = remember { mutableStateMapOf<Int, Float>() }

        // Index where the dragged item would be dropped at current offset
        val targetIndex = if (draggingIndex >= 0 && itemHeights.isNotEmpty() && items.isNotEmpty()) {
            val cellH = (itemHeights[draggingIndex] ?: itemHeights.values.average().toFloat()) + spacingPx
            (draggingIndex + (dragOffsetY / cellH).roundToInt()).coerceIn(items.indices)
        } else -1

        Column(
            verticalArrangement = if (spacing > 0.dp) Arrangement.spacedBy(spacing) else Arrangement.Top,
        ) {
            items.forEachIndexed { index, itemData ->
                val isDragging = index == draggingIndex
                val cellH = (itemHeights[draggingIndex] ?: 0f) + spacingPx

                // How far to visually shift this item so others "make room" for the dragged item
                val translationY = when {
                    isDragging -> dragOffsetY
                    targetIndex >= 0 && draggingIndex >= 0 -> when {
                        draggingIndex < targetIndex && index in (draggingIndex + 1)..targetIndex -> -cellH
                        draggingIndex > targetIndex && index in targetIndex until draggingIndex -> cellH
                        else -> 0f
                    }
                    else -> 0f
                }

                // key() ensures Compose tracks each item by its identity, not list position —
                // so composable state stays correct when the list reorders.
                key(itemData["id"] ?: index.toString()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { itemHeights[index] = it.size.height.toFloat() }
                            .zIndex(if (isDragging) 1f else 0f)
                            .graphicsLayer {
                                this.translationY = translationY
                                if (isDragging) {
                                    scaleX = 1.03f
                                    scaleY = 1.03f
                                }
                            }
                            .pointerInput(index) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggingIndex = index
                                        dragOffsetY = 0f
                                    },
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
                                                        "from" to draggingIndex.toString(),
                                                        "to" to to.toString(),
                                                    ),
                                                )
                                            }
                                        }
                                        draggingIndex = -1
                                        dragOffsetY = 0f
                                    },
                                    onDragCancel = {
                                        draggingIndex = -1
                                        dragOffsetY = 0f
                                    },
                                )
                            },
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        renderNode(layout, data + itemData, listData, onAction)
                        // Drag handle — subtle visual hint at trailing edge
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = null,
                            tint = DesignTokens.SecondaryText.copy(alpha = 0.35f),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 12.dp)
                                .size(18.dp),
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun RenderGeneratedList(
        node: ComponentNode,
        data: Map<String, String>,
        listData: Map<String, List<Map<String, String>>>,
        onAction: (String, Map<String, String>) -> Unit,
        renderNode: NodeRenderer,
    ) {
        val count = node.countBinding?.let { data[it]?.toIntOrNull() } ?: 0
        val layout = node.itemLayout ?: return
        val spacing = node.style?.spacing?.dp ?: 0.dp
        Column(
            verticalArrangement = if (spacing > 0.dp) Arrangement.spacedBy(spacing) else Arrangement.Top,
        ) {
            (1..count).forEach { i ->
                renderNode(layout, data + mapOf("seasonNumber" to i.toString(), "index" to i.toString()), listData, onAction)
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

/**
 * Resolve any `{{key}}` in [routeTemplate] or [route], build the params map,
 * and invoke [onAction] with (type, params).
 */
private fun ActionModel.dispatch(
    data: Map<String, String>,
    onAction: (String, Map<String, String>) -> Unit,
) {
    val resolvedRoute = routeTemplate?.let { tpl ->
        var r = tpl
        data.forEach { (k, v) -> r = r.replace("{{$k}}", v) }
        r
    } ?: route
    val params = buildMap<String, String> {
        resolvedRoute?.let { put("route", it) }
        putAll(this@dispatch.params)
    }
    onAction(type, params)
}

private fun String?.toFontWeight(): FontWeight = when (this) {
    "bold" -> FontWeight.Bold
    "semibold" -> FontWeight.SemiBold
    "medium" -> FontWeight.Medium
    else -> FontWeight.Normal
}
