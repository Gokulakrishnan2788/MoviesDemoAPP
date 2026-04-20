package com.example.moviesdemoapp.core.network

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject
import javax.inject.Singleton

// OkHttp interceptor for /banking/* paths.
// Serves mock responses from assets/mock/banking/{endpoint}.json.
// Per ADR-010 - banking has no real backend.
@Singleton
class MockInterceptor @Inject constructor(
    @ApplicationContext private val context: Context,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        if (!path.startsWith("/banking")) return chain.proceed(request)

        val fileName = path.removePrefix("/banking/").ifEmpty { "home" }
        val json = runCatching {
            context.assets.open("mock/banking/$fileName.json").bufferedReader().readText()
        }.getOrDefault("{}")

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(json.toResponseBody("application/json".toMediaType()))
            .build()
    }
}
