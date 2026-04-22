package com.example.moviesdemoapp.engine.sdui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moviesdemoapp.core.network.model.ComponentNode
import com.example.moviesdemoapp.core.ui.DesignTokens
import com.example.moviesdemoapp.core.ui.colorFromToken
import com.example.moviesdemoapp.engine.sdui.TemplateResolver
import com.example.moviesdemoapp.engine.sdui.applyAccessibility
import androidx.compose.foundation.layout.padding

@Composable
internal fun RenderText(
    node: ComponentNode,
    data: Map<String, String>,
    resolver: TemplateResolver,
) {
    val text = node.template?.let { resolver.resolve(it, data) }
        ?: node.dataBinding?.let { data[it] }
        ?: node.text
        ?: node.props["text"] ?: ""

    val color      = (node.style?.foregroundColor ?: node.style?.textColor)
        ?.let { colorFromToken(it) } ?: DesignTokens.PrimaryText
    val fontSize   = node.style?.fontSize?.sp ?: DesignTokens.TextMd
    val fontWeight = node.style?.fontWeight.toFontWeight()
    val maxLines   = node.style?.lineLimit ?: node.style?.maxLines ?: Int.MAX_VALUE
    val pad        = node.style?.padding?.dp ?: 0.dp

    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = (if (pad > 0.dp) Modifier.padding(pad) else Modifier)
            .applyAccessibility(node.screenAccessibility, data),
    )
}
