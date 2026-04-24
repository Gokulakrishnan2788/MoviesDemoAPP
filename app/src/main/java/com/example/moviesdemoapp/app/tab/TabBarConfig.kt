package com.example.moviesdemoapp.app.tab

import com.example.moviesdemoapp.core.network.model.BindingItem
import kotlinx.serialization.Serializable

/** Parsed representation of tab_config.json. */
@Serializable
data class TabBarConfig(
    val type: String = "tabBar",
    val defaultSelectedTab: String = "",
    val bindings: Map<String, BindingItem> = emptyMap(),
    val tabs: List<TabItem> = emptyList(),
)

/** A single tab entry from JSON — titleBinding is a key into [TabBarConfig.bindings]. */
@Serializable
data class TabItem(
    val id: String,
    val titleBinding: String = "",
    val icon: String = "",
    val rootScreenId: String = "",
)

/**
 * UI-ready tab — all binding tokens already resolved into display strings.
 * [graphRoute] is derived from [TabItem.id] as "${id}_graph", which matches
 * the navigation graph route convention used in [ArchitectNavHost].
 */
data class ResolvedTab(
    val id: String,
    val title: String,
    val icon: String,
    val graphRoute: String,
)
