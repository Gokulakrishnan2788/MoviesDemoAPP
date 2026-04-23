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
    val title: String? = null,
    val state: State? = null,
    val accessibility: Accessibility? = null,
    val children: List<ComponentNode> = emptyList(),
    val dataSource: DataSourceModel? = null,
)

// A single renderable UI node: type + optional style, children, data bindings, action.
@Serializable
data class ComponentNode(
    val id: String? = null,
    val type: String,
    val title: String? = null,
    val displayTemplate: String? = null,
    val currencySymbol: String? = null,
    val valueTemplate: String? = null,
    val minValue: Int? = null,
    val maxValue: Int? = null,
    val step: Int? = null,
    val leadingIcon: String? = null,
    val variant: String? = null,
    val label: String? = null,
    val placeholder: String? = null,
    val options: List<DropdownOption>? = emptyList(),
    val validation: Validation? = null,
    val accessibility: AccessibilityNodeModel? = null,
    val props: Map<String, String> = emptyMap(),
    val style: StyleModel? = null,
    val children: List<ComponentNode> = emptyList(),
    val action: ActionModel? = null,
    val analytics: Analytics? = null,
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
    val screenAccessibility: AccessibilityModel? = null,
)


@Serializable
data class Analytics(
    val event: String,
    val params: Map<String, String>? = emptyMap())


@Serializable
data class DropdownOption(
    val title: String,
    val value: String
)

// UI action attached to a tappable component (navigate, search, etc.)
@Serializable
data class Validation(
    val required: Boolean? = null,
    val minLength: Int? = null,
    val min: Int? = null
)



// UI action attached to a tappable component (navigate, search, etc.)
@Serializable
data class AccessibilityNodeModel(
    val label: String? = null,
    val traits: List<String> = emptyList(),
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
    val destination: String? = null,
    val routeTemplate: String? = null,
    val params: Map<String, String> = emptyMap(),
)

// Controls component visibility based on data presence.
@Serializable
data class VisibilityModel(
    val dataBinding: String? = null,
    val isNotEmpty: Boolean = false,
)

// Controls component visibility based on data presence.
@Serializable
data class State(
    val formId: String? = null,
    val step: Int? = null,
)

// Controls component visibility based on data presence.
@Serializable
data class Accessibility(
    val supportsDynamicType: Boolean? = null,
    val supportsBoldText: Boolean? = null,
    val voiceOverEnabled: Boolean? = null
)

// Declarative accessibility contract for a ComponentNode.
// All fields are optional; missing fields are treated as "no override".
@Serializable
data class AccessibilityModel(
    // Overrides what TalkBack announces for this node.
    // Omit when visible Text children already carry sufficient meaning.
    val label: String? = null,
    // Action label announced as "double-tap to <hint>".
    val hint: String? = null,
    // Semantic role: "button" | "image" | "checkbox" | "switch" | "tab" | "header"
    val role: String? = null,
    // When true, all descendant semantics are merged into this node's single focus target.
    val mergeDescendants: Boolean? = null,
    // When false the node is excluded from the accessibility tree entirely.
    val importantForAccessibility: Boolean? = null,
)
