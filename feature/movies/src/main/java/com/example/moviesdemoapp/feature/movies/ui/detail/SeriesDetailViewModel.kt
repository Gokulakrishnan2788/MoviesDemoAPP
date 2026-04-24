package com.example.moviesdemoapp.feature.movies.ui.detail

import androidx.lifecycle.SavedStateHandle
import com.example.moviesdemoapp.core.data.ScreenRepository
import com.example.moviesdemoapp.core.data.remote.DataSourceExecutor
import com.example.moviesdemoapp.core.domain.BaseViewModel
import com.example.moviesdemoapp.core.network.model.Analytics
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SeriesDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val screenRepository: ScreenRepository,
    private val executeDataSource: DataSourceExecutor
) : BaseViewModel<SeriesDetailState, SeriesDetailIntent, SeriesDetailEffect>() {

    // Both screenId and seriesId come from nav arguments — no hardcoded values in feature code.
    private val screenId: String = savedStateHandle["screenId"] ?: "series_detail"
    private val seriesId: String = savedStateHandle["seriesId"] ?: ""

    override fun initialState() = SeriesDetailState()

    init {
        handleIntent(SeriesDetailIntent.Load)
    }

    override suspend fun reduce(intent: SeriesDetailIntent) {
        when (intent) {
            is SeriesDetailIntent.Load         -> loadDetail()
            is SeriesDetailIntent.NavigateBack -> setEffect(SeriesDetailEffect.GoBack)
        }
    }

    private suspend fun loadDetail() {
        val screenModel = screenRepository.loadScreen(screenId)
            ?: run {
                setState { copy(error = "Screen config not found") }; return
            }

        setState { copy(screenModel = screenModel, isLoading = true, error = null) }

        val dataSource = screenModel.dataSource
            ?: run { setState { copy(isLoading = false) }; return }

        runCatching { executeDataSource.execute(dataSource, mapOf("seriesId" to seriesId)) }
            .onSuccess { items ->
                setState { copy(isLoading = false, data = items.firstOrNull() ?: emptyMap()) }
            }
            .onFailure { error ->
                setState { copy(isLoading = false, error = error.message ?: "Failed to load") }
            }
    }
}
