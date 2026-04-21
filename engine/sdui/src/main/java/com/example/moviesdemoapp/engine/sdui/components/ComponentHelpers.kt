package com.example.moviesdemoapp.engine.sdui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.example.moviesdemoapp.core.network.model.ActionModel
import com.example.moviesdemoapp.core.network.model.ComponentNode

/**
 * Recursive render callback passed into every layout component so it can render its children.
 * Must be invoked from a @Composable context.
 */
typealias NodeRenderer =
    @Composable (node: ComponentNode, data: Map<String, String>, listData: Map<String, List<Map<String, String>>>, onAction: (String, Map<String, String>) -> Unit) -> Unit

/**
 * Resolves route template, builds the params map, and fires [onAction].
 * Internal — used by component files in this package only.
 */
internal fun ActionModel.dispatch(
    data: Map<String, String>,
    onAction: (String, Map<String, String>) -> Unit,
) {
    val resolvedRoute = routeTemplate?.let { tpl ->
        var r = tpl
        data.forEach { (k, v) -> r = r.replace("{{$k}}", v) }
        r
    } ?: route
    val params = buildMap<String, String> {
        resolvedRoute?.let { put("route", it) }
        putAll(this@dispatch.params)
    }
    onAction(type, params)
}

/** Maps a JSON fontWeight string to a Compose [FontWeight]. */
internal fun String?.toFontWeight(): FontWeight = when (this) {
    "bold"     -> FontWeight.Bold
    "semibold" -> FontWeight.SemiBold
    "medium"   -> FontWeight.Medium
    else       -> FontWeight.Normal
}
