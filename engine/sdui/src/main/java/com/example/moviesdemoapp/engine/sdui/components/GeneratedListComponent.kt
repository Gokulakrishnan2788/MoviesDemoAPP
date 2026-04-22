package com.example.moviesdemoapp.engine.sdui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.moviesdemoapp.core.network.model.ComponentNode
import com.example.moviesdemoapp.engine.sdui.applyAccessibility

@Composable
internal fun RenderGeneratedList(
    node: ComponentNode,
    data: Map<String, String>,
    listData: Map<String, List<Map<String, String>>>,
    onAction: (String, Map<String, String>) -> Unit,
    renderNode: NodeRenderer,
) {
    val count   = node.countBinding?.let { data[it]?.toIntOrNull() } ?: 0
    val layout  = node.itemLayout ?: return
    val spacing = node.style?.spacing?.dp ?: 0.dp

    Column(
        modifier = Modifier.applyAccessibility(node.accessibility, data),
        verticalArrangement = if (spacing > 0.dp) Arrangement.spacedBy(spacing) else Arrangement.Top,
    ) {
        (1..count).forEach { i ->
            renderNode(
                layout,
                data + mapOf("seasonNumber" to i.toString(), "index" to i.toString()),
                listData,
                onAction,
            )
        }
    }
}
