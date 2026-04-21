package com.example.moviesdemoapp.core.network

/**
 * Contract for loading a raw SDUI screen JSON string by screen ID.
 *
 * Implementations decide *where* the JSON comes from:
 *   - [LocalScreenSource]  → reads from bundled assets (current)
 *   - [RemoteScreenSource] → fetches from a remote server (swap the binding in DataModule)
 *
 * Switching from local to remote requires only one line change in DataModule — no ViewModel
 * or engine code is touched.
 */
interface ScreenSource {
    suspend fun load(screenId: String): String?
}
