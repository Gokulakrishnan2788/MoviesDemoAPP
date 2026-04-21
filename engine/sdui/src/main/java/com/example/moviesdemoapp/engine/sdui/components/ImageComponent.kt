package com.example.moviesdemoapp.engine.sdui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.moviesdemoapp.core.network.model.ComponentNode

@Composable
internal fun RenderImage(node: ComponentNode, data: Map<String, String>) {
    val url    = node.dataBinding?.let { data[it] } ?: node.props["url"] ?: ""
    val width  = node.style?.frameWidth?.dp
    val height = node.style?.frameHeight?.dp ?: 200.dp
    val radius = node.style?.cornerRadius?.dp ?: 0.dp

    val mod = (if (width != null) Modifier.size(width = width, height = height)
               else Modifier.fillMaxWidth().height(height))
        .clip(RoundedCornerShape(radius))

    AsyncImage(
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = mod,
    )
}
