package com.example.moviesdemoapp.feature.movies.ui.list

import androidx.lifecycle.SavedStateHandle
import com.example.moviesdemoapp.core.data.ScreenRepository
import com.example.moviesdemoapp.core.data.remote.DataSourceExecutor
import com.example.moviesdemoapp.core.domain.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val screenRepository: ScreenRepository,
    private val executeDataSource: DataSourceExecutor,
) : BaseViewModel<MoviesState, MoviesIntent, MoviesEffect>() {

    // screenId comes from the nav argument — no hardcoded screen name in feature code.
    private val screenId: String = savedStateHandle["screenId"] ?: "tv_series_list"

    override fun initialState() = MoviesState()

    init {
        handleIntent(MoviesIntent.LoadScreen)
    }

    override suspend fun reduce(intent: MoviesIntent) {
        when (intent) {
            is MoviesIntent.LoadScreen -> loadScreen()
            is MoviesIntent.OnAction   -> handleAction(intent.actionId, intent.params)
        }
    }

    // ─── Intent handlers ─────────────────────────────────────────────────────

    private suspend fun loadScreen() {
        val screenModel = screenRepository.loadScreen(screenId)
            ?: run { setState { copy(error = "Screen config not found") }; return }

        setState { copy(screenModel = screenModel, isLoading = true, error = null) }

        val dataSource = screenModel.dataSource
            ?: run { setState { copy(isLoading = false) }; return }

        runCatching { executeDataSource.execute(dataSource) }
            .onSuccess { items ->
                setState { copy(isLoading = false, listData = mapOf("series" to items)) }
            }
            .onFailure { error ->
                setState { copy(isLoading = false, error = error.message ?: "Failed to load") }
            }
    }

    private fun handleAction(actionId: String, params: Map<String, String>) {
        when (actionId) {
            "navigate" -> params["route"]?.let { setEffect(MoviesEffect.Navigate(it)) }
            "search"   -> { /* Phase 3 */ }
            "reorder"  -> {
                val binding = params["binding"] ?: return
                val from    = params["from"]?.toIntOrNull() ?: return
                val to      = params["to"]?.toIntOrNull() ?: return
                reorderList(binding, from, to)
            }
        }
    }

    // ─── Order ───────────────────────────────────────────────────────────────

    private fun reorderList(binding: String, from: Int, to: Int) {
        if (from == to) return
        setState {
            val current = listData[binding]?.toMutableList()
            if (current == null || from !in current.indices || to !in current.indices) {
                this
            } else {
                current.add(to, current.removeAt(from))
                copy(listData = listData + (binding to current.toList()))
            }
        }
    }
}
