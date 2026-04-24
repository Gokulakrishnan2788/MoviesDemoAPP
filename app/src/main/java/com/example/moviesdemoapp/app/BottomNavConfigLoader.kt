package com.example.moviesdemoapp.app

import android.content.Context
import kotlinx.serialization.json.Json

/**
 * Utility to load bottom navigation configuration from assets.
 */
object BottomNavConfigLoader {
    private val json = Json { ignoreUnknownKeys = true }

    fun loadConfig(context: Context): BottomNavConfig {
        return try {
            val configString = context.assets.open("bottom_nav_config.json").bufferedReader().use { it.readText() }
            json.decodeFromString<BottomNavConfig>(configString)
        } catch (_: Exception) {
            // Fallback to default configuration if loading fails
            BottomNavConfig(emptyList())
        }
    }
}

