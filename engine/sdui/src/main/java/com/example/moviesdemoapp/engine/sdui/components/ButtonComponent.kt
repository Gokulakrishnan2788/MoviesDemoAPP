package com.example.moviesdemoapp.engine.sdui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.example.moviesdemoapp.core.network.model.ComponentNode
import com.example.moviesdemoapp.core.ui.DesignTokens
import com.example.moviesdemoapp.engine.sdui.TemplateResolver

@Composable
internal fun RenderButton(
    node: ComponentNode,
    data: Map<String, String>,
    onAction: (String, Map<String, String>) -> Unit,
    resolver: TemplateResolver,
) {
    val label = node.template?.let { resolver.resolve(it, data) } ?: node.props["label"] ?: ""

    Box(
        modifier = Modifier
            .padding(DesignTokens.SpacingMd)
            .semantics {
                role = Role.Button
                contentDescription = label
            }
            .clickable { node.action?.dispatch(data, onAction) },
    ) {
        Text(text = label, color = DesignTokens.Accent, fontSize = DesignTokens.TextLg)
    }
}
