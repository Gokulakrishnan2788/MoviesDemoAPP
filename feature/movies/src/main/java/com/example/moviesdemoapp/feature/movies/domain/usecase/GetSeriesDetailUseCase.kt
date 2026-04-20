package com.example.moviesdemoapp.feature.movies.domain.usecase

import com.example.moviesdemoapp.core.domain.Result
import com.example.moviesdemoapp.feature.movies.domain.model.SeriesDetail
import com.example.moviesdemoapp.feature.movies.domain.repository.SeriesRepository

class GetSeriesDetailUseCase(private val repository: SeriesRepository) {
    suspend operator fun invoke(imdbId: String): Result<SeriesDetail> =
        repository.getSeriesDetail(imdbId)
}
