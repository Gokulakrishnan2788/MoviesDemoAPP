package com.example.moviesdemoapp.core.network

import kotlinx.serialization.json.JsonElement
import org.json.JSONObject
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.QueryMap
import retrofit2.http.Url

/**
 * A dynamic API interface for the Banking module.
 * Uses the base URL from the Retrofit instance.
 */
interface BankingApi {

    /**
     * Executes a dynamic GET request relative to the base URL.
     * @param endPoint The relative path (e.g., "formStatus").
     * @param queries Optional query parameters.
     * @param headers Optional request headers.
     */
    @GET
    suspend fun get(
        @Url endPoint: String,
        @QueryMap queries: Map<String, String> = emptyMap(),
        @HeaderMap headers: Map<String, String> = emptyMap()
    ): JsonElement

    /**
     * Executes a dynamic POST request relative to the base URL.
     * @param endPoint The relative path.
     * @param body The request body as a [JsonElement].
     * @param headers Optional request headers.
     */
    @POST
    suspend fun post(
        @Url endPoint: String,
        @Body body: JsonElement? = null,
        @HeaderMap headers: Map<String, String> = emptyMap()
    ): JsonElement

    /**
     * Executes a dynamic PUT request relative to the base URL.
     */
    @PUT
    suspend fun put(
        @Url endPoint: String,
        @Body body: JsonElement,
        @HeaderMap headers: Map<String, String> = emptyMap()
    ): JsonElement

    /**
     * Executes a dynamic DELETE request relative to the base URL.
     */
    @DELETE
    suspend fun delete(
        @Url endPoint: String,
        @QueryMap queries: Map<String, String> = emptyMap(),
        @HeaderMap headers: Map<String, String> = emptyMap()
    ): JsonElement
}
