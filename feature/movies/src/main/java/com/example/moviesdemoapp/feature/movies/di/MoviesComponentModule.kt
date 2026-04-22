package com.example.moviesdemoapp.feature.movies.di

// ─── HOW TO REGISTER A CUSTOM SDUI COMPONENT FOR THIS FEATURE ────────────────
//
// 1. Create your Composable anywhere inside feature:movies, e.g.:
//
//    @Composable
//    fun MovieCardComponent(
//        node: ComponentNode,
//        data: Map<String, String>,
//        onAction: (String, Map<String, String>) -> Unit,
//    ) {
//        // your custom UI here
//    }
//
// 2. Uncomment the module below and add a @Provides @IntoSet entry for it:
//
// import com.example.moviesdemoapp.engine.sdui.SduiComponentProvider
// import dagger.Module
// import dagger.Provides
// import dagger.hilt.InstallIn
// import dagger.hilt.components.SingletonComponent
// import dagger.multibindings.IntoSet
//
// @Module
// @InstallIn(SingletonComponent::class)
// object MoviesComponentModule {
//
//     @Provides
//     @IntoSet
//     fun provideMovieCard(): SduiComponentProvider = SduiComponentProvider { registry ->
//         registry.register("movieCard") { node, data, onAction ->
//             MovieCardComponent(node = node, data = data, onAction = onAction)
//         }
//     }
//
//     // Add more feature-specific components here following the same pattern.
//     // Each @Provides @IntoSet entry is one custom component type.
// }
//
// 3. In your SDUI JSON use the matching type string:
//    { "type": "movieCard", "dataBinding": "posterURL", "style": { ... } }
//
// Nothing else changes — no MovieApp, no engine, no SDUIRenderer modifications.
// ─────────────────────────────────────────────────────────────────────────────
