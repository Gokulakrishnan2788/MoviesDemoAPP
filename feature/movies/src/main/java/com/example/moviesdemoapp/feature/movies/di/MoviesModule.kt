package com.example.moviesdemoapp.feature.movies.di

import com.example.moviesdemoapp.feature.movies.data.repository.SeriesRepositoryImpl
import com.example.moviesdemoapp.feature.movies.data.repository.WatchlistRepositoryImpl
import com.example.moviesdemoapp.feature.movies.domain.repository.SeriesRepository
import com.example.moviesdemoapp.feature.movies.domain.repository.WatchlistRepository
import com.example.moviesdemoapp.feature.movies.domain.usecase.AddToWatchlistUseCase
import com.example.moviesdemoapp.feature.movies.domain.usecase.GetSeriesDetailUseCase
import com.example.moviesdemoapp.feature.movies.domain.usecase.GetSeriesListUseCase
import com.example.moviesdemoapp.feature.movies.domain.usecase.IsInWatchlistUseCase
import com.example.moviesdemoapp.feature.movies.domain.usecase.RemoveFromWatchlistUseCase
import com.example.moviesdemoapp.feature.movies.domain.usecase.SearchSeriesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MoviesModule {

    @Provides
    @Singleton
    fun provideSeriesRepository(impl: SeriesRepositoryImpl): SeriesRepository = impl

    @Provides
    @Singleton
    fun provideWatchlistRepository(impl: WatchlistRepositoryImpl): WatchlistRepository = impl

    @Provides
    fun provideGetSeriesListUseCase(repo: SeriesRepository) = GetSeriesListUseCase(repo)

    @Provides
    fun provideSearchSeriesUseCase(repo: SeriesRepository) = SearchSeriesUseCase(repo)

    @Provides
    fun provideGetSeriesDetailUseCase(repo: SeriesRepository) = GetSeriesDetailUseCase(repo)

    @Provides
    fun provideAddToWatchlistUseCase(repo: WatchlistRepository) = AddToWatchlistUseCase(repo)

    @Provides
    fun provideRemoveFromWatchlistUseCase(repo: WatchlistRepository) = RemoveFromWatchlistUseCase(repo)

    @Provides
    fun provideIsInWatchlistUseCase(repo: WatchlistRepository) = IsInWatchlistUseCase(repo)
}
