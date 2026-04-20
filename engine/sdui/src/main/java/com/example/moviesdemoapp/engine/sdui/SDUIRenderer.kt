package com.example.moviesdemoapp.engine.sdui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.moviesdemoapp.core.ui.DesignTokens
import com.example.moviesdemoapp.engine.sdui.components.SDUIComponents

// ─── Public composable entry-point ───────────────────────────────────────────

/**
 * Render a server-driven screen.
 *
 * Shows [CircularProgressIndicator] while [isLoading], an error [Text] when
 * [error] is non-null, or delegates to [SDUIRenderEngine] once [screenModel] is ready.
 *
 * @param onAction invoked with (actionId, params) whenever the user interacts with
 *   a tappable SDUI component. The ViewModel converts this into a [UiIntent].
 */
@Composable
fun SDUIRenderer(
    screenModel: ScreenModel?,
    isLoading: Boolean,
    error: String?,
    dataMap: Map<String, String> = emptyMap(),
    listData: Map<String, List<Map<String, String>>> = emptyMap(),
    onAction: (actionId: String, params: Map<String, String>) -> Unit,
) {
    val resolver = remember { TemplateResolver() }
    val registry = remember { ComponentRegistry() }
    val components = remember { SDUIComponents(resolver) }
    val engine = remember { SDUIRenderEngine(registry, components) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTokens.ScreenBackground),
    ) {
        when {
            isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = DesignTokens.Accent,
            )
            error != null -> Text(
                text = error,
                color = DesignTokens.SecondaryText,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(DesignTokens.SpacingMd),
            )
            screenModel != null -> engine.Render(
                screenModel = screenModel,
                data = dataMap,
                listData = listData,
                onAction = onAction,
            )
        }
    }
}

// ─── Internal render engine ───────────────────────────────────────────────────

/**
 * Internal engine that translates a [ScreenModel] into a Compose tree.
 * Created once per [SDUIRenderer] invocation via [remember].
 *
 * - Never fetches data — that is the ViewModel's responsibility.
 * - Custom components registered in [ComponentRegistry] take precedence over built-ins.
 * - Unknown types render a visible placeholder — never crash.
 */
class SDUIRenderEngine(
    private val registry: ComponentRegistry,
    private val components: SDUIComponents,
) {

    @Composable
    fun Render(
        screenModel: ScreenModel,
        data: Map<String, String> = emptyMap(),
        listData: Map<String, List<Map<String, String>>> = emptyMap(),
        onAction: (actionId: String, params: Map<String, String>) -> Unit = { _, _ -> },
    ) {
        val base = Modifier
            .fillMaxSize()
            .background(DesignTokens.ScreenBackground)

        when (screenModel.type) {
            "scroll" -> Column(modifier = base.verticalScroll(rememberScrollState())) {
                screenModel.children.forEach { RenderNode(it, data, listData, onAction) }
            }
            else -> Column(modifier = base) {
                screenModel.children.forEach { RenderNode(it, data, listData, onAction) }
            }
        }
    }

    @Composable
    fun RenderNode(
        node: ComponentNode,
        data: Map<String, String>,
        listData: Map<String, List<Map<String, String>>> = emptyMap(),
        onAction: (actionId: String, params: Map<String, String>) -> Unit = { _, _ -> },
    ) {
        val custom = registry.resolve(node.type)
        if (custom != null) {
            custom(node, data, onAction)
            return
        }
        components.RenderBuiltIn(
            node = node,
            data = data,
            listData = listData,
            onAction = onAction,
            renderNode = { n, d, l, a -> RenderNode(n, d, l, a) },
        )
    }
}
