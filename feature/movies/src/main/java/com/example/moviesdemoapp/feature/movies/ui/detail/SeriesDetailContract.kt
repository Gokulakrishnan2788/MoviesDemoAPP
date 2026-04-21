package com.example.moviesdemoapp.feature.movies.ui.detail

import com.example.moviesdemoapp.core.domain.UiEffect
import com.example.moviesdemoapp.core.domain.UiIntent
import com.example.moviesdemoapp.core.domain.UiState
import com.example.moviesdemoapp.engine.sdui.ScreenModel

data class SeriesDetailState(
    val screenModel: ScreenModel? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val data: Map<String, String> = emptyMap(),
) : UiState

sealed interface SeriesDetailIntent : UiIntent {
    data class Load(val seriesId: String) : SeriesDetailIntent
    data object NavigateBack : SeriesDetailIntent
}

sealed interface SeriesDetailEffect : UiEffect {
    data object GoBack : SeriesDetailEffect
}
