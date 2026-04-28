package com.example.moviesdemoapp.app.tab

import android.content.Context
import com.example.moviesdemoapp.core.network.StringResolver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads [TabBarConfig] from bundled assets and resolves every tab's [TabItem.titleBinding]
 * into a display string using [StringResolver].
 *
 * The asset path mirrors the SDUI screen convention: screens/tab_config.json.
 * [StringResolver] handles the dot → underscore key conversion
 * (e.g. "tab.movies.title" → R.string.tab_movies_title).
 *
 * Results are computed once and cached in [resolvedTabs] / [defaultTabRoute]
 * so repeated access from recompositions is zero-cost.
 */
@Singleton
class TabConfigLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stringResolver: StringResolver,
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val config: TabBarConfig? by lazy {
        runCatching {
            val raw = context.assets
                .open("screens/tab_config.json")
                .bufferedReader()
                .readText()
            json.decodeFromString<TabBarConfig>(raw)
        }.getOrNull()
    }

    /** Navigation graph route for the initially selected tab, e.g. "movies_graph". */
    val defaultTabRoute: String
        get() = config?.defaultSelectedTab
            ?.takeIf { it.isNotEmpty() }
            ?.let { "${it}_graph" }
            ?: "movies_graph"

    /**
     * Returns the list of tabs with bindings fully resolved.
     * Falls back to an empty list if the config file is missing or malformed.
     */
    val resolvedTabs: List<ResolvedTab> by lazy {
        val cfg = config ?: return@lazy emptyList()
        cfg.tabs.map { tab ->
            ResolvedTab(
                id = tab.id,
                title = resolveTitle(cfg, tab.titleBinding),
                icon = tab.icon,
                graphRoute = "${tab.id}_graph",
            )
        }
    }

    private fun resolveTitle(cfg: TabBarConfig, titleBinding: String): String {
        val binding = cfg.bindings[titleBinding] ?: return titleBinding
        return when (binding.source) {
            "string" -> stringResolver.resolve(binding.key)
            else     -> titleBinding
        }
    }
}
