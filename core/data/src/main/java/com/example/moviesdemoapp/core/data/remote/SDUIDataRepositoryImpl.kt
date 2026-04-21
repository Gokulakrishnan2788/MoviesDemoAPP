package com.example.moviesdemoapp.core.data.remote

import com.example.moviesdemoapp.core.network.NetworkClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SDUIDataRepositoryImpl @Inject constructor(
    private val networkClient: NetworkClient,
) : SDUIDataRepository {

    override suspend fun fetch(url: String): String? = networkClient.get(url)
}
