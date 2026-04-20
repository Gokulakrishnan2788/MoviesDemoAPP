package com.example.moviesdemoapp.engine.sdui

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SDUIRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) {
    fun loadScreen(screenId: String): ScreenModel? = runCatching {
        val raw = context.assets.open("screens/$screenId.json").bufferedReader().readText()
        json.decodeFromString<ScreenModel>(raw)
    }.getOrNull()
}
