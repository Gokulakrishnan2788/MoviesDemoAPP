package com.example.moviesdemoapp.feature.movies.data.repository

import com.example.moviesdemoapp.core.data.WatchlistDao
import com.example.moviesdemoapp.core.data.WatchlistEntity
import com.example.moviesdemoapp.core.domain.Result
import com.example.moviesdemoapp.feature.movies.domain.model.Series
import com.example.moviesdemoapp.feature.movies.domain.repository.WatchlistRepository
import javax.inject.Inject

class WatchlistRepositoryImpl @Inject constructor(
    private val dao: WatchlistDao,
) : WatchlistRepository {

    override suspend fun getWatchlist(): Result<List<Series>> = runCatching {
        dao.getAll().map { it.toDomain() }
    }.fold(
        onSuccess = { Result.Success(it) },
        onFailure = { Result.Error(message = it.message ?: "DB error") },
    )

    override suspend fun addToWatchlist(series: Series): Result<Unit> = runCatching {
        dao.insert(series.toEntity())
    }.fold(
        onSuccess = { Result.Success(Unit) },
        onFailure = { Result.Error(message = it.message ?: "DB error") },
    )

    override suspend fun removeFromWatchlist(imdbId: String): Result<Unit> = runCatching {
        dao.deleteById(imdbId)
    }.fold(
        onSuccess = { Result.Success(Unit) },
        onFailure = { Result.Error(message = it.message ?: "DB error") },
    )

    override suspend fun isInWatchlist(imdbId: String): Result<Boolean> = runCatching {
        dao.isInWatchlist(imdbId)
    }.fold(
        onSuccess = { Result.Success(it) },
        onFailure = { Result.Error(message = it.message ?: "DB error") },
    )

    private fun WatchlistEntity.toDomain() = Series(
        id = imdbID, title = title, year = year, type = "",
        posterUrl = posterUrl, rating = rating, genre = genre,
    )

    private fun Series.toEntity() = WatchlistEntity(
        imdbID = id, title = title, posterUrl = posterUrl,
        rating = rating, year = year, genre = genre,
    )
}
