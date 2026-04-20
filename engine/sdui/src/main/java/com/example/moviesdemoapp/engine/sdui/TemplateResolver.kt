package com.example.moviesdemoapp.engine.sdui

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves `{{key}}` placeholder tokens in SDUI strings using a flat data map.
 */
@Singleton
class TemplateResolver @Inject constructor() {

    /**
     * Replace all `{{key}}` occurrences in [template] with values from [data].
     * Keys absent from [data] resolve to an empty string (never crash).
     */
    fun resolve(template: String, data: Map<String, String>): String {
        var result = template
        data.forEach { (key, value) -> result = result.replace("{{$key}}", value) }
        return result
    }

    /** Return the value for [key] in [data], or null if absent. */
    fun resolveBinding(key: String?, data: Map<String, String>): String? =
        key?.let { data[it] }

    /**
     * Evaluate the [node]'s [VisibilityModel] against [data].
     * Returns `true` (visible) when no visibility rule is defined.
     */
    fun isVisible(node: ComponentNode, data: Map<String, String>): Boolean {
        val v = node.visibility ?: return true
        val value = v.dataBinding?.let { data[it] } ?: return true
        return if (v.isNotEmpty) value.isNotEmpty() else true
    }
}
