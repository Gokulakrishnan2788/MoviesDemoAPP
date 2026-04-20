package com.example.moviesdemoapp.feature.movies.domain.usecase

import com.example.moviesdemoapp.core.domain.Result
import com.example.moviesdemoapp.feature.movies.domain.model.Series
import com.example.moviesdemoapp.feature.movies.domain.repository.SeriesRepository

class GetSeriesListUseCase(private val repository: SeriesRepository) {
    suspend operator fun invoke(query: String = "game"): Result<List<Series>> =
        repository.searchSeries(query)
}
