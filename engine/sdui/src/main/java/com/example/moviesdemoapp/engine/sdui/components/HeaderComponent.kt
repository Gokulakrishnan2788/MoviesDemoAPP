package com.example.moviesdemoapp.engine.sdui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.moviesdemoapp.core.network.model.ComponentNode
import com.example.moviesdemoapp.core.ui.DesignTokens
import com.example.moviesdemoapp.engine.sdui.TemplateResolver
import com.example.moviesdemoapp.engine.sdui.applyAccessibility

@Composable
internal fun RenderHeader(
    node: ComponentNode,
    data: Map<String, String>,
    onAction: (String, Map<String, String>) -> Unit,
    resolver: TemplateResolver,
) {
    val title     = node.titleTemplate?.let { resolver.resolve(it, data) } ?: node.props["title"] ?: ""
    val subtitle  = node.subtitleTemplate?.let { resolver.resolve(it, data) } ?: node.props["subtitle"]
    val hasSearch = node.action?.type == "search"
    val padH      = node.style?.padding?.dp ?: DesignTokens.SpacingMd
    val padTop    = node.style?.paddingTop?.dp ?: DesignTokens.SpacingSm
    val padBottom = node.style?.paddingBottom?.dp ?: DesignTokens.SpacingSm

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = padH, end = padH, top = padTop, bottom = padBottom)
            .applyAccessibility(node.screenAccessibility, data),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = DesignTokens.PrimaryText,
                fontSize = DesignTokens.TextXxl,
                fontWeight = FontWeight.Bold,
            )
            subtitle?.let {
                Text(text = it, color = DesignTokens.SecondaryText, fontSize = DesignTokens.TextMd)
            }
        }
        if (hasSearch) {
            IconButton(onClick = { node.action?.dispatch(data, onAction) }) {
                Icon(Icons.Default.Search,
                    contentDescription = "Search",
                    tint = DesignTokens.PrimaryText)
            }
        }
    }
}
