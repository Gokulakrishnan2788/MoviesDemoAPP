package com.example.moviesdemoapp.core.network.model

import kotlinx.serialization.Serializable

// Describes where a screen's data comes from and how to map the response.
@Serializable
data class DataSourceModel(
    val type: String,
    val request: RequestModel? = null,
    val response: ResponseModel? = null,
    val enrichmentDataSource: DataSourceModel? = null,
    // Legacy flat fields (backward compat)
    val method: String? = null,
    val url: String? = null,
    val responseRoot: String? = null,
) {
    val effectiveMethod: String get() = request?.method ?: method ?: "GET"
    val effectiveUrl: String get() = request?.url ?: url ?: ""
    val effectiveRoot: String? get() = response?.root ?: responseRoot
    val fieldMapping: Map<String, String> get() = response?.fieldMapping ?: emptyMap()
}

@Serializable
data class RequestModel(
    val method: String,
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val queryParams: Map<String, String> = emptyMap(),
    val timeout: Int = 10,
)

@Serializable
data class ResponseModel(
    val root: String? = null,
    val type: String = "object",
    val fieldMapping: Map<String, String> = emptyMap(),
)
