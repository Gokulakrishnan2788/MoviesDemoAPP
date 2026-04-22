package com.example.moviesdemoapp.engine.sdui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.moviesdemoapp.core.network.model.ComponentNode
import com.example.moviesdemoapp.engine.sdui.resolveTokens

@Composable
internal fun RenderImage(node: ComponentNode, data: Map<String, String>) {
    val url    = node.dataBinding?.let { data[it] } ?: node.props["url"] ?: ""
    val width  = node.style?.frameWidth?.dp
    val height = node.style?.frameHeight?.dp ?: 200.dp
    val radius = node.style?.cornerRadius?.dp ?: 0.dp

    val sizeMod = if (width != null) Modifier.size(width = width, height = height)
                  else Modifier.fillMaxWidth().height(height)

    // Resolve the label template against current item data BEFORE entering composition.
    val resolvedLabel = node.screenAccessibility?.label?.resolveTokens(data)

    // The outer Box is the only accessibility node TalkBack sees.
    // clearAndSetSemantics {} on AsyncImage removes ALL Coil-internal semantics so
    // the two nodes never compete — there is no ambiguity about which contentDescription
    // TalkBack announces.
    val boxMod = when {
        node.screenAccessibility?.importantForAccessibility == false ->
            sizeMod.clearAndSetSemantics {}
        resolvedLabel != null ->
            sizeMod.semantics {
                contentDescription = resolvedLabel
                role = Role.Image
            }
        else -> sizeMod
    }

    Box(modifier = boxMod) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(radius))
                .clearAndSetSemantics {},
        )
    }
}
