package com.example.moviesdemoapp.engine.sdui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Executes a [DataSourceModel] and returns a flat list of data maps ready
 * for SDUI binding.
 *
 * Flow:
 *  1. Hit main [DataSourceModel.request] → parse [DataSourceModel.response]
 *     → produce List<Map<String,String>> (one map per collection item, or a
 *       single-item list for object responses).
 *  2. If [DataSourceModel.enrichmentDataSource] is present, hit it once per
 *     item (in parallel), substituting `{{key}}` in the URL, then merge the
 *     enriched fields into each item's map.
 */
@Singleton
class DataSourceExecutor @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json,
) {

    suspend fun execute(dataSource: DataSourceModel): List<Map<String, String>> =
        withContext(Dispatchers.IO) {
            // ── 1. Main fetch ──────────────────────────────────────────────
            val mainItems = fetchAndMap(dataSource)

            // ── 2. Enrichment (parallel per item) ─────────────────────────
            val enrichment = dataSource.enrichmentDataSource ?: return@withContext mainItems

            mainItems.map { item ->
                async {
                    val enriched = fetchEnrichment(enrichment, item)
                    item + enriched
                }
            }.awaitAll()
        }

    // ─────────────────────────────────────────────────────────────────────────

    private fun fetchAndMap(ds: DataSourceModel): List<Map<String, String>> {
        val url = ds.effectiveUrl.ifEmpty { return emptyList() }
        val rawJson = get(url) ?: return emptyList()
        val root = runCatching { json.parseToJsonElement(rawJson).jsonObject }.getOrNull()
            ?: return emptyList()

        val fieldMapping = ds.fieldMapping       // SDUI key → API key
        val responseRoot = ds.effectiveRoot

        return when (ds.response?.type) {
            "collection" -> {
                val array = responseRoot?.let { root[it]?.jsonArray } ?: return emptyList()
                array.mapNotNull { element ->
                    val obj = runCatching { element.jsonObject }.getOrNull() ?: return@mapNotNull null
                    mapFields(obj, fieldMapping)
                }
            }
            else -> {
                // Single object response
                listOf(mapFields(root, fieldMapping))
            }
        }
    }

    private fun fetchEnrichment(
        enrichment: DataSourceModel,
        itemData: Map<String, String>,
    ): Map<String, String> {
        // Resolve {{key}} placeholders in the URL using item data
        var url = enrichment.effectiveUrl
        itemData.forEach { (k, v) -> url = url.replace("{{$k}}", v) }

        val rawJson = get(url) ?: return emptyMap()
        val root = runCatching { json.parseToJsonElement(rawJson).jsonObject }.getOrNull()
            ?: return emptyMap()

        return mapFields(root, enrichment.fieldMapping)
    }

    /**
     * Map API JSON fields → SDUI binding keys.
     * [fieldMapping] is `{ sduiKey: apiKey }`, e.g. `{ "title": "Title" }`.
     */
    private fun mapFields(obj: JsonObject, fieldMapping: Map<String, String>): Map<String, String> =
        buildMap {
            if (fieldMapping.isEmpty()) {
                // No mapping defined — pass all fields through as-is
                obj.forEach { (k, v) -> put(k, runCatching { v.jsonPrimitive.content }.getOrDefault("")) }
            } else {
                fieldMapping.forEach { (sduiKey, apiKey) ->
                    val value = obj[apiKey]?.let { runCatching { it.jsonPrimitive.content }.getOrDefault("") } ?: ""
                    put(sduiKey, value)
                }
            }
        }

    private fun get(url: String): String? = runCatching {
        val request = Request.Builder().url(url).get().build()
        okHttpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) response.body?.string() else null
        }
    }.getOrNull()
}
