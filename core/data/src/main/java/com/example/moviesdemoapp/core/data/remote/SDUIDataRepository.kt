package com.example.moviesdemoapp.core.data.remote

// Repository contract for fetching raw JSON from any dynamic URL.
// Used by DataSourceExecutor to fetch SDUI screen data.
interface SDUIDataRepository {
    suspend fun fetch(url: String): String?
}
