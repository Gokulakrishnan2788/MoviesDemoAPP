package com.example.moviesdemoapp.feature.movies.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.moviesdemoapp.engine.sdui.SDUIRenderer
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SeriesDetailScreen(
    navController: NavController,
    viewModel: SeriesDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SeriesDetailEffect.GoBack -> navController.popBackStack()
            }
        }
    }

    SDUIRenderer(
        screenModel = state.screenModel,
        isLoading = state.isLoading,
        error = state.error,
        dataMap = state.data,
        onAction = { actionId, _ ->
            when (actionId) {
                "back" -> viewModel.handleIntent(SeriesDetailIntent.NavigateBack)
            }
        },
    )
}
