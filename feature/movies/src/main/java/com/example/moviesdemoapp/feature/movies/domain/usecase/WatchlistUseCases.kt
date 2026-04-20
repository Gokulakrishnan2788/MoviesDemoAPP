package com.example.moviesdemoapp.feature.movies.domain.usecase

import com.example.moviesdemoapp.core.domain.Result
import com.example.moviesdemoapp.feature.movies.domain.model.Series
import com.example.moviesdemoapp.feature.movies.domain.repository.WatchlistRepository

class AddToWatchlistUseCase(private val repository: WatchlistRepository) {
    suspend operator fun invoke(series: Series): Result<Unit> = repository.addToWatchlist(series)
}

class RemoveFromWatchlistUseCase(private val repository: WatchlistRepository) {
    suspend operator fun invoke(imdbId: String): Result<Unit> = repository.removeFromWatchlist(imdbId)
}

class IsInWatchlistUseCase(private val repository: WatchlistRepository) {
    suspend operator fun invoke(imdbId: String): Result<Boolean> = repository.isInWatchlist(imdbId)
}
