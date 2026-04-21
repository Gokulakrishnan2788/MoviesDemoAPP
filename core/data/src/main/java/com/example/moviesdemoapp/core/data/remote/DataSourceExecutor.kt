package com.example.moviesdemoapp.core.data.remote

import com.example.moviesdemoapp.core.network.model.DataSourceModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

// Use case: executes a DataSourceModel and returns enriched data ready for SDUI binding.
//
// Responsibility chain:
//   DataSourceExecutor → SDUIDataRepository → NetworkClient → OkHttp → API
//
// This class owns ONLY parsing and mapping logic.
// It has zero knowledge of OkHttp, Retrofit, or any HTTP detail.
@Singleton
class DataSourceExecutor @Inject constructor(
    private val repository: SDUIDataRepository,
    private val json: Json,
) {

    suspend fun execute(
        dataSource: DataSourceModel,
        params: Map<String, String> = emptyMap(),
    ): List<Map<String, String>> = coroutineScope {
        // Step 1: fetch main URL (with optional URL-template params) and map response fields
        val mainItems = fetchAndMap(dataSource, params)

        // Step 2: for each item fetch enrichmentDataSource in parallel and merge
        val enrichment = dataSource.enrichmentDataSource ?: return@coroutineScope mainItems

        mainItems.map { item ->
            async {
                val enriched = fetchEnrichment(enrichment, item)
                item + enriched
            }
        }.awaitAll()
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private suspend fun fetchAndMap(
        ds: DataSourceModel,
        params: Map<String, String> = emptyMap(),
    ): List<Map<String, String>> {
        val url = params.entries.fold(ds.effectiveUrl) { acc, (k, v) -> acc.replace("{{$k}}", v) }
            .ifEmpty { return emptyList() }
        val rawJson = repository.fetch(url) ?: return emptyList()
        val root = runCatching { json.parseToJsonElement(rawJson).jsonObject }.getOrNull()
            ?: return emptyList()

        return when (ds.response?.type) {
            "collection" -> {
                val array = ds.effectiveRoot?.let { root[it]?.jsonArray } ?: return emptyList()
                array.mapNotNull { element ->
                    runCatching { element.jsonObject }.getOrNull()
                        ?.let { mapFields(it, ds.fieldMapping) }
                }
            }
            else -> listOf(mapFields(root, ds.fieldMapping))
        }
    }

    private suspend fun fetchEnrichment(
        enrichment: DataSourceModel,
        itemData: Map<String, String>,
    ): Map<String, String> {
        // Substitute {{key}} placeholders with values from the current item
        var url = enrichment.effectiveUrl
        itemData.forEach { (k, v) -> url = url.replace("{{$k}}", v) }

        val rawJson = repository.fetch(url) ?: return emptyMap()
        val root = runCatching { json.parseToJsonElement(rawJson).jsonObject }.getOrNull()
            ?: return emptyMap()

        return mapFields(root, enrichment.fieldMapping)
    }

    // fieldMapping: { sduiKey -> apiKey }  e.g. { "title" -> "Title" }
    // Reads each apiKey from the JSON object and stores it under the sduiKey in the result map
    private fun mapFields(obj: JsonObject, fieldMapping: Map<String, String>): Map<String, String> =
        buildMap {
            if (fieldMapping.isEmpty()) {
                obj.forEach { (k, v) ->
                    put(k, runCatching { v.jsonPrimitive.content }.getOrDefault(""))
                }
            } else {
                fieldMapping.forEach { (sduiKey, apiKey) ->
                    put(sduiKey, obj[apiKey]?.let {
                        runCatching { it.jsonPrimitive.content }.getOrDefault("")
                    } ?: "")
                }
            }
        }
}
