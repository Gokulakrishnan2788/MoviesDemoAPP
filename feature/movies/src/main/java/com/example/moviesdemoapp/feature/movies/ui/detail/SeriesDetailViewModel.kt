package com.example.moviesdemoapp.feature.movies.ui.detail

import com.example.moviesdemoapp.core.domain.BaseViewModel
import com.example.moviesdemoapp.core.domain.Result
import com.example.moviesdemoapp.feature.movies.domain.model.Series
import com.example.moviesdemoapp.feature.movies.domain.usecase.AddToWatchlistUseCase
import com.example.moviesdemoapp.feature.movies.domain.usecase.GetSeriesDetailUseCase
import com.example.moviesdemoapp.feature.movies.domain.usecase.IsInWatchlistUseCase
import com.example.moviesdemoapp.feature.movies.domain.usecase.RemoveFromWatchlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SeriesDetailViewModel @Inject constructor(
    private val getSeriesDetail: GetSeriesDetailUseCase,
    private val isInWatchlist: IsInWatchlistUseCase,
    private val addToWatchlist: AddToWatchlistUseCase,
    private val removeFromWatchlist: RemoveFromWatchlistUseCase,
) : BaseViewModel<SeriesDetailState, SeriesDetailIntent, SeriesDetailEffect>() {

    override fun initialState() = SeriesDetailState()

    override suspend fun reduce(intent: SeriesDetailIntent) {
        when (intent) {
            is SeriesDetailIntent.Load          -> loadDetail(intent.imdbId)
            is SeriesDetailIntent.ToggleWatchlist -> toggleWatchlist()
            is SeriesDetailIntent.NavigateBack  -> setEffect(SeriesDetailEffect.GoBack)
        }
    }

    private suspend fun loadDetail(imdbId: String) {
        setState { copy(isLoading = true, error = null) }
        val detailResult = getSeriesDetail(imdbId)
        val watchlistResult = isInWatchlist(imdbId)
        setState {
            copy(
                isLoading = false,
                detail = (detailResult as? Result.Success)?.data,
                isInWatchlist = (watchlistResult as? Result.Success)?.data ?: false,
                error = (detailResult as? Result.Error)?.message,
            )
        }
    }

    private suspend fun toggleWatchlist() {
        val detail = state.value.detail ?: return
        val series = Series(
            id = detail.id, title = detail.title, year = detail.year,
            type = "series", posterUrl = detail.posterUrl,
            rating = detail.rating, genre = detail.genre,
        )
        if (state.value.isInWatchlist) removeFromWatchlist(detail.id)
        else addToWatchlist(series)
        setState { copy(isInWatchlist = !isInWatchlist) }
    }
}
