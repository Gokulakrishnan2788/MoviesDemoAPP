package com.example.moviesdemoapp.core.network

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads SDUI screen JSON definitions from the app's `assets/screens/` directory.
 */
@Singleton
class ScreenAssetLoader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /**
     * Read and return the raw JSON string for the given [fileName].
     *
     * @param fileName file name with extension, e.g. `tv_series_list.json`
     * @return raw JSON content of the asset
     * @throws java.io.IOException if the file is not found in assets
     */
    fun load(fileName: String): String =
        context.assets.open("screens/$fileName")
            .bufferedReader()
            .use { it.readText() }
}
