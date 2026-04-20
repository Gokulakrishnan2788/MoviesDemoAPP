package com.example.moviesdemoapp.feature.movies.ui.detail

import com.example.moviesdemoapp.core.domain.UiEffect
import com.example.moviesdemoapp.core.domain.UiIntent
import com.example.moviesdemoapp.core.domain.UiState
import com.example.moviesdemoapp.feature.movies.domain.model.SeriesDetail

data class SeriesDetailState(
    val detail: SeriesDetail? = null,
    val isLoading: Boolean = false,
    val isInWatchlist: Boolean = false,
    val error: String? = null,
) : UiState

sealed interface SeriesDetailIntent : UiIntent {
    data class Load(val imdbId: String) : SeriesDetailIntent
    data object ToggleWatchlist : SeriesDetailIntent
    data object NavigateBack : SeriesDetailIntent
}

sealed interface SeriesDetailEffect : UiEffect {
    data object GoBack : SeriesDetailEffect
}
