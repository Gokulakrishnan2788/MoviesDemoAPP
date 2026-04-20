package com.example.moviesdemoapp.feature.movies.ui.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.moviesdemoapp.engine.sdui.SDUIRenderer
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MoviesScreen(
    navController: NavController,
    viewModel: MoviesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is MoviesEffect.Navigate -> navController.navigate(effect.route)
            }
        }
    }

    SDUIRenderer(
        screenModel = state.screenModel,
        isLoading = state.isLoading,
        error = state.error,
        dataMap = state.dataMap,
        listData = state.listData,
        onAction = { actionId, params ->
            viewModel.handleIntent(MoviesIntent.OnAction(actionId, params))
        },
    )
}
