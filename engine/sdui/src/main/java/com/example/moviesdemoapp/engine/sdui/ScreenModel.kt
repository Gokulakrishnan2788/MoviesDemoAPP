package com.example.moviesdemoapp.engine.sdui

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Root model for a server-driven UI screen. */
@Serializable
data class ScreenModel(
    val screenId: String,
    val type: String,
    val children: List<ComponentNode> = emptyList(),
    val dataSource: DataSourceModel? = null,
)

/** A single renderable SDUI node with optional style, children, and actions. */
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
    /** Static text shorthand — equivalent to props["text"]. */
    val text: String? = null,
    /** Icon name shorthand for icon components. */
    val icon: String? = null,
)

/** Visual styling overrides for a [ComponentNode]. */
@Serializable
data class StyleModel(
    val backgroundColor: String? = null,
    /** Alias used in newer JSON — maps to [backgroundColor]. */
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

// ─── Data Source ──────────────────────────────────────────────────────────────

/**
 * Describes where a screen's data comes from.
 * Supports both the legacy flat format and the new request/response structure.
 */
@Serializable
data class DataSourceModel(
    val type: String,
    // ── New structured format ──
    val request: RequestModel? = null,
    val response: ResponseModel? = null,
    /** Optional secondary fetch that enriches each list item with extra fields. */
    val enrichmentDataSource: DataSourceModel? = null,
    // ── Legacy flat format (backward compat) ──
    val method: String? = null,
    val url: String? = null,
    val responseRoot: String? = null,
) {
    /** Effective HTTP method from either format. */
    val effectiveMethod: String get() = request?.method ?: method ?: "GET"
    /** Effective URL from either format. */
    val effectiveUrl: String get() = request?.url ?: url ?: ""
    /** Effective response root key from either format. */
    val effectiveRoot: String? get() = response?.root ?: responseRoot
    /** Field mapping from API keys to SDUI template keys. */
    val fieldMapping: Map<String, String> get() = response?.fieldMapping ?: emptyMap()
}

/** HTTP request parameters for a [DataSourceModel]. */
@Serializable
data class RequestModel(
    val method: String,
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val queryParams: Map<String, String> = emptyMap(),
    val timeout: Int = 10,
)

/**
 * Describes how to extract and map fields from the API response.
 *
 * [fieldMapping] maps SDUI binding keys → API JSON keys.
 * Example: `{ "title": "Title", "posterURL": "Poster" }` means
 * the API field "Title" is bound as "{{title}}" in templates.
 */
@Serializable
data class ResponseModel(
    val root: String? = null,
    val type: String = "object",
    val fieldMapping: Map<String, String> = emptyMap(),
)

// ─── Action ───────────────────────────────────────────────────────────────────

/** Navigation or behavior action attached to a component. */
@Serializable
data class ActionModel(
    val type: String,
    val route: String? = null,
    /** Route with `{{key}}` placeholders resolved at runtime from item data. */
    val routeTemplate: String? = null,
    val params: Map<String, String> = emptyMap(),
)

// ─── Visibility ───────────────────────────────────────────────────────────────

/** Controls component visibility based on data presence. */
@Serializable
data class VisibilityModel(
    val dataBinding: String? = null,
    val isNotEmpty: Boolean = false,
)
