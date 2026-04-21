package com.example.moviesdemoapp.engine.sdui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.moviesdemoapp.core.network.model.ComponentNode
import com.example.moviesdemoapp.core.ui.DesignTokens

@Composable
internal fun RenderSpacer(node: ComponentNode) {
    val height = node.props["height"]?.toFloatOrNull() ?: node.style?.spacing ?: 8f
    Spacer(modifier = Modifier.height(height.dp))
}

@Composable
internal fun RenderDivider() {
    HorizontalDivider(color = DesignTokens.Surface, thickness = 1.dp)
}
