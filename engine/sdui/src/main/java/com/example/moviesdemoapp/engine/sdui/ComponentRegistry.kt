package com.example.moviesdemoapp.engine.sdui

import androidx.compose.runtime.Composable
import com.example.moviesdemoapp.core.network.model.ComponentNode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory type for a custom SDUI component composable.
 *
 * @param node the component node to render
 * @param data flat key-value map of bound data for the current item
 * @param onAction (actionId, params) callback when the component is tapped
 */
typealias ComponentFactory =
    @Composable (node: ComponentNode, data: Map<String, String>, onAction: (String, Map<String, String>) -> Unit) -> Unit

/**
 * Registry mapping SDUI type strings to custom [ComponentFactory] composables.
 *
 * Follows Open/Closed — add new components via [register] without touching [SDUIRenderEngine].
 */
@Singleton
class ComponentRegistry @Inject constructor() {

    private val registry = mutableMapOf<String, ComponentFactory>()

    /** Register a custom composable [factory] for [componentType]. */
    fun register(componentType: String, factory: ComponentFactory) {
        registry[componentType] = factory
    }

    /** Resolve the factory for [componentType], or null if not registered. */
    fun resolve(componentType: String): ComponentFactory? = registry[componentType]
}
