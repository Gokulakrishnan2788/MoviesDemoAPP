package com.example.moviesdemoapp.engine.sdui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.moviesdemoapp.core.network.model.ComponentNode
import com.example.moviesdemoapp.core.ui.DesignTokens
import com.example.moviesdemoapp.core.ui.colorFromToken
import com.example.moviesdemoapp.engine.sdui.applyAccessibility

@Composable
internal fun RenderIcon(node: ComponentNode, data: Map<String, String> = emptyMap()) {
    val name  = node.icon ?: node.props["icon"] ?: ""
    val color = node.style?.foregroundColor?.let { colorFromToken(it) } ?: DesignTokens.PrimaryText
    val size  = node.style?.fontSize?.dp ?: 16.dp

    val icon = when {
        name.contains("search")                       -> Icons.Default.Search
        name.contains("play") || name.contains("tv")  -> Icons.Default.PlayCircle
        else                                           -> Icons.Default.Star
    }

    Icon(
        imageVector = icon,
        contentDescription = null, // driven by AccessibilityModel via modifier semantics
        tint = color,
        modifier = Modifier.size(size).applyAccessibility(node.accessibility, data),
    )
}
