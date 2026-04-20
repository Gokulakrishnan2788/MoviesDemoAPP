package com.example.moviesdemoapp.engine.sdui

import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parses a raw JSON string into a [ScreenModel].
 * Inject [Json] from :core:network NetworkModule.
 */
@Singleton
class SDUIParser @Inject constructor(private val json: Json) {

    /**
     * Decode [jsonString] into a [ScreenModel].
     *
     * @throws kotlinx.serialization.SerializationException on malformed JSON
     */
    fun parse(jsonString: String): ScreenModel = json.decodeFromString(jsonString)
}
