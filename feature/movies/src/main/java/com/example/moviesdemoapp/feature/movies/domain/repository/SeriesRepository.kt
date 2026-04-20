package com.example.moviesdemoapp.feature.movies.domain.repository

import com.example.moviesdemoapp.core.domain.Result
import com.example.moviesdemoapp.feature.movies.domain.model.Series
import com.example.moviesdemoapp.feature.movies.domain.model.SeriesDetail

interface SeriesRepository {
    suspend fun searchSeries(query: String): Result<List<Series>>
    suspend fun getSeriesDetail(imdbId: String): Result<SeriesDetail>
}
