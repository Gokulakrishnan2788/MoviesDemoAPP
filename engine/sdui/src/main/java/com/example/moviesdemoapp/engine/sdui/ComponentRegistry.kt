package com.example.moviesdemoapp.engine.sdui

import androidx.compose.runtime.Composable
import com.example.moviesdemoapp.core.network.model.ComponentNode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory type for a custom SDUI component composable.
 *
 * @param node      the component node carrying style/props/action from JSON
 * @param data      flat key-value map of bound data for the current item
 * @param onAction  (actionId, params) callback when the component is tapped
 */
typealias ComponentFactory =
    @Composable (node: ComponentNode, data: Map<String, String>, onAction: (String, Map<String, String>) -> Unit) -> Unit

/**
 * Singleton registry mapping SDUI type strings to custom [ComponentFactory] composables.
 *
 * HOW IT WORKS:
 *   Hilt collects every [SduiComponentProvider] contributed via @IntoSet across all
 *   feature modules and injects them here as a Set. The [init] block calls each
 *   provider once, populating the registry before any screen is rendered.
 *
 * HOW TO ADD A CUSTOM COMPONENT (from a feature module):
 *   1. Create your Composable in the feature module.
 *   2. Add a Hilt @Module in the feature module and provide a [SduiComponentProvider]:
 *
 *        @Module @InstallIn(SingletonComponent::class)
 *        object MoviesComponentModule {
 *            @Provides @IntoSet
 *            fun provideMovieCard(): SduiComponentProvider = SduiComponentProvider { registry ->
 *                registry.register("movieCard") { node, data, onAction ->
 *                    MovieCardComponent(node = node, data = data, onAction = onAction)
 *                }
 *            }
 *        }
 *
 *   3. In your SDUI JSON use the same type string:  { "type": "movieCard", ... }
 *
 *   That is all. No changes to the engine, no changes to MovieApp.
 *
 * Built-in types (text, image, column, row, card, topBar, list, etc.) are handled
 * by [com.example.moviesdemoapp.engine.sdui.SDUIComponentsDispatcher].
 * Registered custom factories are checked FIRST in [SDUIRenderEngine.RenderNode].
 */
@Singleton
class ComponentRegistry @Inject constructor(
    providers: Set<@JvmSuppressWildcards SduiComponentProvider>,
) {
    private val registry = mutableMapOf<String, ComponentFactory>()

    init {
        // Each feature module's SduiComponentProvider contributes to this set via
        // Hilt @IntoSet multibinding. They are all called once here at construction.
        providers.forEach { it.registerInto(this) }
    }

    /**
     * Register a custom composable [factory] for [componentType].
     * Called by [SduiComponentProvider.registerInto] implementations — not called directly.
     */
    fun register(componentType: String, factory: ComponentFactory) {
        registry[componentType] = factory
    }

    /** Resolve the factory for [componentType], or null if not registered. */
    fun resolve(componentType: String): ComponentFactory? = registry[componentType]
}
