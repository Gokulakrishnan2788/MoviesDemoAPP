package com.example.moviesdemoapp.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

// OkHttp-backed implementation of NetworkClient.
// All blocking IO is confined to Dispatchers.IO here — callers stay dispatcher-agnostic.
@Singleton
class OkHttpNetworkClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
) : NetworkClient {

    override suspend fun get(url: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(url).get().build()
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) response.body?.string() else null
            }
        }.getOrNull()
    }
}
