package com.example.moviesdemoapp.feature.movies.data.repository

import com.example.moviesdemoapp.core.domain.Result
import com.example.moviesdemoapp.core.network.OmdbApiService
import com.example.moviesdemoapp.feature.movies.domain.model.Series
import com.example.moviesdemoapp.feature.movies.domain.model.SeriesDetail
import com.example.moviesdemoapp.feature.movies.domain.repository.SeriesRepository
import javax.inject.Inject

class SeriesRepositoryImpl @Inject constructor(
    private val api: OmdbApiService,
) : SeriesRepository {

    override suspend fun searchSeries(query: String): Result<List<Series>> = runCatching {
        val dto = api.searchSeries(query)
        if (dto.response != "True") return Result.Error(message = dto.error ?: "No results")
        dto.search.orEmpty().map { s ->
            Series(id = s.imdbID, title = s.title, year = s.year, type = s.type, posterUrl = s.poster)
        }
    }.fold(
        onSuccess = { Result.Success(it) },
        onFailure = { Result.Error(message = it.message ?: "Network error") },
    )

    override suspend fun getSeriesDetail(imdbId: String): Result<SeriesDetail> = runCatching {
        val dto = api.getSeriesDetail(imdbId)
        if (dto.response != "True") return Result.Error(message = "Series not found")
        SeriesDetail(
            id = dto.imdbID, title = dto.title, year = dto.year, genre = dto.genre,
            rating = dto.rating, posterUrl = dto.poster, runtime = dto.runtime,
            totalSeasons = dto.totalSeasons, awards = dto.awards, plot = dto.plot,
            actors = dto.actors, writer = dto.writer, director = dto.director,
        )
    }.fold(
        onSuccess = { Result.Success(it) },
        onFailure = { Result.Error(message = it.message ?: "Network error") },
    )
}
