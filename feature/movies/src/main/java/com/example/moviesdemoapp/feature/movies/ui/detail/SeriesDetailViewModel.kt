package com.example.moviesdemoapp.feature.movies.ui.detail

import com.example.moviesdemoapp.core.data.remote.DataSourceExecutor
import com.example.moviesdemoapp.core.domain.BaseViewModel
import com.example.moviesdemoapp.engine.sdui.usecase.LoadSDUIScreenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SeriesDetailViewModel @Inject constructor(
    private val loadSDUIScreen: LoadSDUIScreenUseCase,
    private val executeDataSource: DataSourceExecutor,
) : BaseViewModel<SeriesDetailState, SeriesDetailIntent, SeriesDetailEffect>() {

    override fun initialState() = SeriesDetailState()

    override suspend fun reduce(intent: SeriesDetailIntent) {
        when (intent) {
            is SeriesDetailIntent.Load         -> loadDetail(intent.seriesId)
            is SeriesDetailIntent.NavigateBack -> setEffect(SeriesDetailEffect.GoBack)
        }
    }

    private suspend fun loadDetail(seriesId: String) {
        val screenModel = loadSDUIScreen("series_detail")
            ?: run { setState { copy(error = "Screen config not found") }; return }

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
