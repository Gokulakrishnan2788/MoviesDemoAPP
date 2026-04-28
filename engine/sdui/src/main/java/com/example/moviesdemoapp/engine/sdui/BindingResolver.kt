package com.example.moviesdemoapp.engine.sdui

import com.example.moviesdemoapp.core.network.StringResolver
import com.example.moviesdemoapp.core.network.model.BindingItem

/**
 * Resolves a screen's [BindingItem] map into a flat [Map<String, String>] that
 * can be merged directly into the SDUI data map before rendering.
 *
 * Resolution is strictly ordered to satisfy cross-source dependencies:
 *   1. **api**      — look up an already-fetched API value by its mapped key.
 *   2. **string**   — fetch a localized string via [StringResolver] (dot→underscore key).
 *   3. **template** — interpolate `{{key}}` placeholders using the combined map
 *                     built from steps 1 + 2 + the incoming [apiData].
 *
 * No Android [Context] is required here — string access is delegated to [StringResolver].
 *
 * Backward compatibility:
 *   - Screens without a `bindings` block return an empty map from [resolveAll];
 *     all existing [dataBinding] / [template] node fields continue to work unchanged.
 *   - [resolve] reads from the pre-built cache so existing call-sites in
 *     [SDUIComponentsDispatcher] (titleBinding, button labels) need no changes.
 */
class BindingResolver(private val stringResolver: StringResolver) {

    private var bindings: Map<String, BindingItem> = emptyMap()
    private var resolvedCache: Map<String, String> = emptyMap()

    fun loadBindings(bindings: Map<String, BindingItem>) {
        this.bindings = bindings
    }

    /**
     * Pre-resolve every binding in [bindings] and return the complete result map.
     * Call this once per screen load (or whenever [apiData] changes) and merge the
     * return value into the SDUI data map that is passed to the render engine.
     *
     * @param apiData The flat key→value map produced by [DataSourceExecutor] for this screen.
     */
    fun resolveAll(apiData: Map<String, String>): Map<String, String> {
        val resolved = mutableMapOf<String, String>()

        // Step 1: API — fetch raw API response field by item.path (new) or item.key (legacy)
        bindings.forEach { (bindingKey, item) ->
            if (item.source == "api") {
                val apiField = item.path ?: item.key
                resolved[bindingKey] = apiData[apiField] ?: ""
            }
        }

        // Step 2: STRING — convert dot-notation key to underscore before lookup
        bindings.forEach { (bindingKey, item) ->
            if (item.source == "string") {
                resolved[bindingKey] = stringResolver.resolve(item.key)
            }
        }

        // Step 3: TEMPLATE — interpolate against (apiData + api-resolved + string-resolved)
        val baseForTemplate: Map<String, String> = apiData + resolved
        bindings.forEach { (bindingKey, item) ->
            if (item.source == "template") {
                val tmpl = item.template ?: return@forEach
                var result = tmpl
                baseForTemplate.forEach { (k, v) -> result = result.replace("{{$k}}", v) }
                resolved[bindingKey] = result
            }
        }

        resolvedCache = resolved
        return resolved
    }

    /**
     * Resolve a single binding by its [key].
     *
     * Reads from the pre-built cache populated by [resolveAll] whenever possible.
     * Falls back to live resolution for "string" and "form" sources in case
     * [resolveAll] was not called before this composable renders (e.g. for
     * screens that use [titleBinding] / button labels in legacy components).
     */
    fun resolve(key: String?): String {
        if (key.isNullOrEmpty()) return ""

        // Fast path: use pre-resolved cache
        resolvedCache[key]?.let { return it }

        // Fallback: live resolution from binding definition
        return try {
            val item = bindings[key] ?: return key
            when (item.source) {
                "string" -> stringResolver.resolve(item.key)
                "form"   -> FormDataStorage.readAndSetValue(key)
                else     -> key
            }
        } catch (e: Exception) {
            key
        }
    }
}
