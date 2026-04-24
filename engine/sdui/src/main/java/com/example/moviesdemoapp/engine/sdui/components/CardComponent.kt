package com.example.moviesdemoapp.engine.sdui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.moviesdemoapp.core.network.model.ComponentNode
import com.example.moviesdemoapp.core.ui.DesignTokens
import com.example.moviesdemoapp.core.ui.colorFromToken
import com.example.moviesdemoapp.engine.sdui.applyAccessibility

@Composable
internal fun RenderCard(
    node: ComponentNode,
    data: Map<String, String>,
    listData: Map<String, List<Map<String, String>>>,
    onAction: (String, Map<String, String>) -> Unit,
    renderNode: NodeRenderer,
) {
    val bg     = node.style?.backgroundColor?.let { colorFromToken(it) } ?: DesignTokens.CardBackground
    val pad    = node.style?.padding?.dp?.takeIf { it > 0.dp } ?: DesignTokens.SpacingMd
    val radius = node.style?.cornerRadius?.dp ?: DesignTokens.RadiusMd

    val action = node.action
    var mod = Modifier
        .fillMaxWidth()
        .padding(horizontal = DesignTokens.SpacingMd, vertical = DesignTokens.SpacingSm)
        .applyAccessibility(node.screenAccessibility, data)
    if (action != null) mod = mod.clickable { action.dispatch(data, onAction) }

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
