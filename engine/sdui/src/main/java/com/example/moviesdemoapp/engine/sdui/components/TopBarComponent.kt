package com.example.moviesdemoapp.engine.sdui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.moviesdemoapp.core.network.model.ComponentNode
import com.example.moviesdemoapp.core.ui.DesignTokens
import com.example.moviesdemoapp.engine.sdui.TemplateResolver

@Composable
internal fun RenderTopBar(
    node: ComponentNode,
    data: Map<String, String>,
    onAction: (String, Map<String, String>) -> Unit,
    resolver: TemplateResolver,
) {
    val title    = node.props["title"] ?: node.titleTemplate?.let { resolver.resolve(it, data) } ?: ""
    val subtitle = node.props["subtitle"] ?: node.subtitleTemplate?.let { resolver.resolve(it, data) }
    val hasBack   = node.props["leadingIcon"] == "back"
    val hasSearch = node.props["trailingIcon"] == "search"
    val padH      = node.style?.padding?.dp ?: DesignTokens.SpacingMd
    val padTop    = node.style?.paddingTop?.dp ?: DesignTokens.SpacingSm
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = DesignTokens.SpacingXs),
            )
        }
    }
}
