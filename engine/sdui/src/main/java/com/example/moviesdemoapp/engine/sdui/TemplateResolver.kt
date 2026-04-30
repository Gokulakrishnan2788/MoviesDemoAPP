package com.example.moviesdemoapp.engine.sdui

import com.example.moviesdemoapp.core.network.model.ComponentNode
import com.example.moviesdemoapp.core.network.model.VisibilityModel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves `{{key}}` placeholder tokens in SDUI strings using a flat data map.
 */
@Singleton
class TemplateResolver @Inject constructor() {

    /**
     * Replace all `{{key}}` occurrences in [template] with values from [data]
     * or [FormDataStorage.formDataStoreAndValidation].
     * Keys absent from both resolve to an empty string (never crash).
     */
    fun resolve(template: String, data: Map<String, String>, savedKey:String?): String {
        val regex = "\\{\\{(.+?)\\}\\}".toRegex()
        return regex.replace(template) { matchResult ->
            val key = matchResult.groupValues[1]
            data[key] ?: FormDataStorage.formDataStoreAndValidation[key] ?: savedKey ?: ""
        }
    }

    fun resolveKey(screenName:String, template: String, data: Map<String, String>, savedKey:String?): String {
        return FormDataStorage.formData?.get(screenName)?: resolve(template, data, savedKey)
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
