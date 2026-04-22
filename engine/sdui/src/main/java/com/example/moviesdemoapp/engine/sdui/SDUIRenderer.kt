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
import androidx.compose.ui.platform.LocalContext
import com.example.moviesdemoapp.core.network.model.ComponentNode
import com.example.moviesdemoapp.core.network.model.ScreenModel
import com.example.moviesdemoapp.core.ui.DesignTokens
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

// ─── Hilt entry point ─────────────────────────────────────────────────────────
// Lets a @Composable reach into Hilt's SingletonComponent to get the shared
// ComponentRegistry. This is the standard pattern for accessing a Hilt singleton
// from code that cannot use @Inject (Composable functions).
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ComponentRegistryEntryPoint {
    fun componentRegistry(): ComponentRegistry
}

// ─── Public composable entry-point ───────────────────────────────────────────

/**
 * Render a server-driven screen.
 *
 * Uses the singleton [ComponentRegistry] populated in MovieApp.registerSduiComponents().
 * Any type registered there is automatically available here — no extra wiring needed.
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
    val context = LocalContext.current

    // Pull the singleton ComponentRegistry from Hilt.
    // This is the SAME instance that MovieApp.registerSduiComponents() populated at startup,
    // so all registered custom components are already available here.
    val registry = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            ComponentRegistryEntryPoint::class.java,
        ).componentRegistry()
    }

    val resolver = remember { TemplateResolver() }
    val components = remember { SDUIComponentsDispatcher(resolver) }
    val engine = remember(registry) { SDUIRenderEngine(registry, components) }

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
    private val components: SDUIComponentsDispatcher,
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
        // Visibility is evaluated once here — applies to BOTH custom and built-in components.
        // Keeping it here prevents custom components from bypassing JSON visibility rules.
        if (!components.isVisible(node, data)) return

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
