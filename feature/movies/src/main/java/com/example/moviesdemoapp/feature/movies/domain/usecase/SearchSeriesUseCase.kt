package com.example.moviesdemoapp.feature.movies.domain.usecase

import com.example.moviesdemoapp.core.domain.Result
import com.example.moviesdemoapp.feature.movies.domain.model.Series
import com.example.moviesdemoapp.feature.movies.domain.repository.SeriesRepository

class SearchSeriesUseCase(private val repository: SeriesRepository) {
    suspend operator fun invoke(query: String): Result<List<Series>> =
        repository.searchSeries(query)
}
