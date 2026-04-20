package com.example.moviesdemoapp.engine.sdui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.moviesdemoapp.core.ui.DesignTokens
import com.example.moviesdemoapp.core.ui.colorFromToken
import com.example.moviesdemoapp.engine.sdui.ActionModel
import com.example.moviesdemoapp.engine.sdui.ComponentNode
import com.example.moviesdemoapp.engine.sdui.TemplateResolver
import javax.inject.Inject
import javax.inject.Singleton

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
        val hasSearch = node.props["trailingIcon"] == "search"

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignTokens.SpacingMd, vertical = DesignTokens.SpacingSm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.weight(1f))
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignTokens.SpacingMd, vertical = DesignTokens.SpacingSm),
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
        val icon = if (name.contains("search")) Icons.Default.Search else Icons.Default.Star
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
        val items = node.listDataBinding?.let { listData[it] } ?: emptyList()
        val layout = node.itemLayout ?: return
        val spacing = node.style?.spacing?.dp ?: 0.dp
        // Column instead of LazyColumn — the root "scroll" container provides scrolling
        Column(
            verticalArrangement = if (spacing > 0.dp) Arrangement.spacedBy(spacing) else Arrangement.Top,
        ) {
            items.forEach { itemData ->
                renderNode(layout, data + itemData, listData, onAction)
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
        Column {
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
