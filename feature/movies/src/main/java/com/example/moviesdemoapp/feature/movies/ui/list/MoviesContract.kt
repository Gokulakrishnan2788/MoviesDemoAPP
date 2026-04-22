package com.example.moviesdemoapp.feature.movies.ui.list

import com.example.moviesdemoapp.core.domain.UiEffect
import com.example.moviesdemoapp.core.domain.UiIntent
import com.example.moviesdemoapp.core.domain.UiState
import com.example.moviesdemoapp.core.network.model.ScreenModel

data class MoviesState(
    val screenModel: ScreenModel? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val dataMap: Map<String, String> = emptyMap(),
    val listData: Map<String, List<Map<String, String>>> = emptyMap(),
) : UiState

sealed interface MoviesIntent : UiIntent {
    data object LoadScreen : MoviesIntent
    data class OnAction(val actionId: String, val params: Map<String, String>) : MoviesIntent
}

sealed interface MoviesEffect : UiEffect {
    data class Navigate(val route: String) : MoviesEffect
}
