package com.example.moviesdemoapp.engine.sdui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.moviesdemoapp.core.network.model.ComponentNode
import com.example.moviesdemoapp.core.ui.DesignTokens
import com.example.moviesdemoapp.core.ui.colorFromToken
import com.example.moviesdemoapp.engine.sdui.applyAccessibility

@Composable
internal fun RenderRow(
    node: ComponentNode,
    data: Map<String, String>,
    listData: Map<String, List<Map<String, String>>>,
    onAction: (String, Map<String, String>) -> Unit,
    renderNode: NodeRenderer,
) {
    val bg      = node.style?.backgroundColor?.let { colorFromToken(it) }
    val pad     = node.style?.padding?.dp ?: 0.dp
    val spacing = node.style?.spacing?.dp ?: DesignTokens.SpacingSm
    val radius  = node.style?.cornerRadius?.dp ?: 0.dp

    val action = node.action
    var mod: Modifier = Modifier
        .fillMaxWidth()
        .applyAccessibility(node.screenAccessibility, data)
    if (bg != null) mod = mod.background(bg, RoundedCornerShape(radius))
    if (pad > 0.dp) mod = mod.padding(pad)
    if (action != null) mod = mod.clickable { action.dispatch(data, onAction) }

    Row(modifier = mod, horizontalArrangement = Arrangement.spacedBy(spacing)) {
        node.children.forEach { renderNode(it, data, listData, onAction) }
    }
}
