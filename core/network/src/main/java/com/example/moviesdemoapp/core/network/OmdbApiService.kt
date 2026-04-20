package com.example.moviesdemoapp.core.network

import retrofit2.http.GET
import retrofit2.http.Query

private const val API_KEY = "8170cd9d"

/**
 * Retrofit service interface for the OMDb API.
 * Base URL: https://www.omdbapi.com/
 */
interface OmdbApiService {

    /**
     * Search for TV series by [query] title keyword.
     * Returns up to 10 results per page.
     */
    @GET("/")
    suspend fun searchSeries(
        @Query("s") query: String,
        @Query("type") type: String = "series",
        @Query("apikey") apiKey: String = API_KEY,
    ): OmdbListResponseDto

    /**
     * Fetch full detail for a series identified by [imdbId].
     */
    @GET("/")
    suspend fun getSeriesDetail(
        @Query("i") imdbId: String,
        @Query("plot") plot: String = "full",
        @Query("apikey") apiKey: String = API_KEY,
    ): SeriesDetailDto
}
