package com.example.moviesdemoapp.engine.sdui

/**
 * Contract for a feature module to contribute custom SDUI component types
 * into the engine's [ComponentRegistry].
 *
 * Each feature module that needs a custom component type:
 *   1. Implements this interface (or uses the SAM lambda form).
 *   2. Provides it into the Hilt multibinding set via @Provides @IntoSet
 *      inside a @Module in that feature module.
 *
 * The engine collects all providers automatically at startup — no app-level
 * wiring, no MovieApp changes.
 *
 * Example (inside feature:movies di module):
 *
 *   @Provides @IntoSet
 *   fun provideMovieCard(): SduiComponentProvider = SduiComponentProvider { registry ->
 *       registry.register("movieCard") { node, data, onAction ->
 *           MovieCardComponent(node = node, data = data, onAction = onAction)
 *       }
 *   }
 */
fun interface SduiComponentProvider {
    fun registerInto(registry: ComponentRegistry)
}
