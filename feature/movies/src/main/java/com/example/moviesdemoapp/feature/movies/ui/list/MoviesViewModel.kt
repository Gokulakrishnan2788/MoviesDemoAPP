package com.example.moviesdemoapp.feature.movies.ui.list

import androidx.lifecycle.viewModelScope
import com.example.moviesdemoapp.core.domain.BaseViewModel
import com.example.moviesdemoapp.engine.sdui.DataSourceExecutor
import com.example.moviesdemoapp.engine.sdui.SDUIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val sduiRepository: SDUIRepository,
    private val dataSourceExecutor: DataSourceExecutor,
) : BaseViewModel<MoviesState, MoviesIntent, MoviesEffect>() {

    override fun initialState() = MoviesState()

    init {
        handleIntent(MoviesIntent.LoadScreen)
    }

    override suspend fun reduce(intent: MoviesIntent) {
        when (intent) {
            is MoviesIntent.LoadScreen  -> loadScreen()
            is MoviesIntent.OnAction    -> handleAction(intent.actionId, intent.params)
        }
    }

    private fun loadScreen() {
        viewModelScope.launch {
            // 1. Parse JSON → ScreenModel; render UI skeleton immediately
            val screenModel = sduiRepository.loadScreen("tv_series_list")
                ?: run { setState { copy(error = "Screen config not found") }; return@launch }
            setState { copy(screenModel = screenModel, isLoading = true, error = null) }

            // 2. Execute main dataSource + enrichmentDataSource
            val dataSource = screenModel.dataSource
                ?: run { setState { copy(isLoading = false) }; return@launch }

            runCatching { dataSourceExecutor.execute(dataSource) }
                .fold(
                    onSuccess = { items ->
                        setState { copy(isLoading = false, listData = mapOf("series" to items)) }
                    },
                    onFailure = { e ->
                        setState { copy(isLoading = false, error = e.message ?: "Failed to load") }
                    },
                )
        }
    }

    private fun handleAction(actionId: String, params: Map<String, String>) {
        when (actionId) {
            "navigate" -> params["route"]?.let { setEffect(MoviesEffect.Navigate(it)) }
            "search"   -> { /* Phase 3 */ }
        }
    }
}
