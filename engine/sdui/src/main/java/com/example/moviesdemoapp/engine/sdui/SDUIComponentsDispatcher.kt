package com.example.moviesdemoapp.engine.sdui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.moviesdemoapp.core.network.model.ComponentNode
import com.example.moviesdemoapp.core.ui.DesignTokens
import com.example.moviesdemoapp.engine.sdui.components.NodeRenderer
import com.example.moviesdemoapp.engine.sdui.components.RenderButton
import com.example.moviesdemoapp.engine.sdui.components.RenderCard
import com.example.moviesdemoapp.engine.sdui.components.RenderColumn
import com.example.moviesdemoapp.engine.sdui.components.RenderDivider
import com.example.moviesdemoapp.engine.sdui.components.RenderGeneratedList
import com.example.moviesdemoapp.engine.sdui.components.RenderHeader
import com.example.moviesdemoapp.engine.sdui.components.RenderIcon
import com.example.moviesdemoapp.engine.sdui.components.RenderImage
import com.example.moviesdemoapp.engine.sdui.components.RenderList
import com.example.moviesdemoapp.engine.sdui.components.RenderRow
import com.example.moviesdemoapp.engine.sdui.components.RenderSpacer
import com.example.moviesdemoapp.engine.sdui.components.RenderText
import com.example.moviesdemoapp.engine.sdui.components.RenderTopBar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Routes each SDUI node type to its dedicated component file in the components/ folder.
 *
 * Visibility is NOT checked here — it is checked once in [SDUIRenderEngine.RenderNode]
 * before this dispatcher is called, so the rule applies equally to custom and built-in
 * components.
 *
 * Adding a new built-in component:
 *   1. Create a new file inside components/ (e.g. RatingComponent.kt) with an
 *      internal @Composable fun RenderRating(...).
 *   2. Add one line in the when block below.
 *
 * Adding a feature-specific custom component:
 *   → Use [SduiComponentProvider] + Hilt @IntoSet in the feature module instead.
 *      See MoviesComponentModule.kt for the template.
 */
@Singleton
class SDUIComponentsDispatcher @Inject constructor(private val resolver: TemplateResolver) {

    /** Delegates to [TemplateResolver.isVisible] — called by [SDUIRenderEngine.RenderNode]. */
    fun isVisible(node: ComponentNode, data: Map<String, String>): Boolean =
        resolver.isVisible(node, data)

    @Composable
    fun RenderBuiltIn(
        node: ComponentNode,
        data: Map<String, String>,
        listData: Map<String, List<Map<String, String>>>,
        onAction: (String, Map<String, String>) -> Unit,
        renderNode: NodeRenderer,
    ) {
        when (node.type) {
            "topBar"        -> RenderTopBar(node, data, onAction, resolver)
            "column"        -> RenderColumn(node, data, listData, onAction, renderNode)
            "row"           -> RenderRow(node, data, listData, onAction, renderNode)
            "card"          -> RenderCard(node, data, listData, onAction, renderNode)
            "spacer"        -> RenderSpacer(node)
            "divider"       -> RenderDivider()
            "text"          -> RenderText(node, data, resolver)
            "header"        -> RenderHeader(node, data, onAction, resolver)
            "image"         -> RenderImage(node, data)
            "icon"          -> RenderIcon(node)
            "button"        -> RenderButton(node, data, onAction, resolver)
            "list"          -> RenderList(node, data, listData, onAction, renderNode)
            "generatedList" -> RenderGeneratedList(node, data, listData, onAction, renderNode)
            else            -> UnknownComponent(node.type)
        }
    }
}

@Composable
private fun UnknownComponent(type: String) {
    Box(Modifier.padding(DesignTokens.SpacingMd)) {
        Text("[unknown: $type]", color = DesignTokens.Accent)
    }
}
