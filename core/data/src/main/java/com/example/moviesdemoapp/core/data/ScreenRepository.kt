package com.example.moviesdemoapp.core.data

import com.example.moviesdemoapp.core.network.ScreenSource
import com.example.moviesdemoapp.core.network.model.ScreenModel
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for loading parsed SDUI screen definitions.
 *
 * Delegates raw JSON loading to [ScreenSource] (local assets or remote API).
 * The binding in DataModule decides which source is active — no code change required
 * in the caller when switching from local to remote.
 */
@Singleton
class ScreenRepository @Inject constructor(
    private val source: ScreenSource,
    private val json: Json,
) {
    suspend fun loadScreen(screenId: String): ScreenModel? = runCatching {
        val raw = source.load(screenId) ?: return@runCatching null
        json.decodeFromString<ScreenModel>(raw)
    }.getOrNull()
}
