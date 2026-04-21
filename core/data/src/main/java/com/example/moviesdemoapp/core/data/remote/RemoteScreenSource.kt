package com.example.moviesdemoapp.core.data.remote

import com.example.moviesdemoapp.core.network.NetworkClient
import com.example.moviesdemoapp.core.network.ScreenSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fetches SDUI screen JSON from a remote server.
 * To activate: change the @Binds in DataModule from LocalScreenSource to this class.
 * No ViewModel or engine code needs to change — only that one line.
 */
@Singleton
class RemoteScreenSource @Inject constructor(
    private val networkClient: NetworkClient,
) : ScreenSource {
    override suspend fun load(screenId: String): String? {
        // TODO: replace with actual CMS/BFF endpoint when backend is ready
        // e.g. networkClient.get("https://api.example.com/screens/$screenId")
        return null
    }
}
