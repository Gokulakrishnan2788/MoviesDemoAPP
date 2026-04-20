package com.example.moviesdemoapp.feature.movies.domain.repository

import com.example.moviesdemoapp.core.domain.Result
import com.example.moviesdemoapp.feature.movies.domain.model.Series

interface WatchlistRepository {
    suspend fun getWatchlist(): Result<List<Series>>
    suspend fun addToWatchlist(series: Series): Result<Unit>
    suspend fun removeFromWatchlist(imdbId: String): Result<Unit>
    suspend fun isInWatchlist(imdbId: String): Result<Boolean>
}
