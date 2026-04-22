package com.example.moviesdemoapp.engine.sdui

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.example.moviesdemoapp.core.network.model.AccessibilityModel

/**
 * Applies [AccessibilityModel] to this [Modifier].
 *
 * [data] is the same flat key-value map used for all other SDUI template resolution.
 * Any `{{key}}` token in [AccessibilityModel.label] or [AccessibilityModel.hint] is
 * substituted from [data] before the value reaches the semantics tree — e.g.
 * `"label": "{{title}} poster"` with `data["title"] = "Breaking Bad"` produces
 * `contentDescription = "Breaking Bad poster"`.
 *
 * Decision table:
 *  - model == null                      → no-op
 *  - importantForAccessibility == false → clearAndSetSemantics {} (remove from tree)
 *  - otherwise                          → semantics block with resolved label / role / hint / merge
 *
 * label vs. visible text:
 *   Omit label when child Text nodes already carry sufficient meaning and
 *   mergeDescendants = true — TalkBack will read the visible text without duplication.
 */
fun Modifier.applyAccessibility(
    model: AccessibilityModel?,
    data: Map<String, String> = emptyMap(),
): Modifier {
    model ?: return this

    if (model.importantForAccessibility == false) return this.clearAndSetSemantics {}

    val resolvedLabel = model.label?.resolveTokens(data)
    val resolvedHint  = model.hint?.resolveTokens(data)

    return semantics(mergeDescendants = model.mergeDescendants == true) {
        resolvedLabel?.let { contentDescription = it }

        model.role?.let { roleValue ->
            when (roleValue.lowercase()) {
                "button"   -> role = Role.Button
                "image"    -> role = Role.Image
                "checkbox" -> role = Role.Checkbox
                "switch"   -> role = Role.Switch
                "tab"      -> role = Role.Tab
                "header"   -> heading()
            }
        }

        resolvedHint?.let { hint -> onClick(label = hint) { false } }
    }
}

// Replaces every {{key}} token with the corresponding value from [data].
// Absent keys resolve to an empty string, matching TemplateResolver behaviour.
// internal so ImageComponent (and future components) can call it directly
// when they must pass the resolved string to a third-party composable parameter
// rather than relying on modifier semantics.
internal fun String.resolveTokens(data: Map<String, String>): String {
    var result = this
    data.forEach { (key, value) -> result = result.replace("{{$key}}", value) }
    return result
}
