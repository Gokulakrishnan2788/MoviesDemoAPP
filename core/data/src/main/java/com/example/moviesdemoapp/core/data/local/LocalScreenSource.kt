package com.example.moviesdemoapp.core.data.local

import android.content.Context
import com.example.moviesdemoapp.core.network.ScreenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads SDUI screen JSON from bundled app assets (screens/<screenId>.json).
 * Active by default — swap the binding in DataModule to [RemoteScreenSource] when the
 * backend is ready.
 */
@Singleton
class LocalScreenSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : ScreenSource {
    override suspend fun load(screenId: String): String? = runCatching {
        context.assets.open("screens/$screenId.json").bufferedReader().readText()
    }.getOrNull()
}
