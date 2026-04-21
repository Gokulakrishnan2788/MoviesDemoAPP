package com.example.moviesdemoapp.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Root model for a server-driven UI screen.
// Lives in core:network so both core:data (parsing) and engine:sdui (rendering) can use it
// without any circular dependency.
@Serializable
data class ScreenModel(
    val screenId: String,
    val type: String,
    val children: List<ComponentNode> = emptyList(),
    val dataSource: DataSourceModel? = null,
)

// A single renderable UI node: type + optional style, children, data bindings, action.
@Serializable
data class ComponentNode(
    val id: String? = null,
    val type: String,
    val props: Map<String, String> = emptyMap(),
    val style: StyleModel? = null,
    val children: List<ComponentNode> = emptyList(),
    val action: ActionModel? = null,
    val itemLayout: ComponentNode? = null,
    @SerialName("dataBinding") val dataBinding: String? = null,
    @SerialName("template") val template: String? = null,
    @SerialName("titleTemplate") val titleTemplate: String? = null,
    @SerialName("subtitleTemplate") val subtitleTemplate: String? = null,
    @SerialName("listDataBinding") val listDataBinding: String? = null,
    @SerialName("countBinding") val countBinding: String? = null,
    val visibility: VisibilityModel? = null,
    val text: String? = null,
    val icon: String? = null,
)

// Visual styling overrides for a ComponentNode.
@Serializable
data class StyleModel(
    val backgroundColor: String? = null,
    val foregroundColor: String? = null,
    val textColor: String? = null,
    val fontSize: Float? = null,
    val fontWeight: String? = null,
    val padding: Float? = null,
    val paddingTop: Float? = null,
    val paddingBottom: Float? = null,
    val paddingStart: Float? = null,
    val paddingEnd: Float? = null,
    val spacing: Float? = null,
    val cornerRadius: Float? = null,
    val width: String? = null,
    val height: String? = null,
    val frameWidth: Float? = null,
    val frameHeight: Float? = null,
    val weight: Float? = null,
    val maxLines: Int? = null,
    val lineLimit: Int? = null,
)

// UI action attached to a tappable component (navigate, search, etc.)
@Serializable
data class ActionModel(
    val type: String,
    val route: String? = null,
    val routeTemplate: String? = null,
    val params: Map<String, String> = emptyMap(),
)

// Controls component visibility based on data presence.
@Serializable
data class VisibilityModel(
    val dataBinding: String? = null,
    val isNotEmpty: Boolean = false,
)
