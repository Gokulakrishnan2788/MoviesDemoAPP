# Naming Conventions
# Apply to all generated files

## Files
XScreen.kt            → Composable screen entry point
XViewModel.kt         → MVI ViewModel
XState.kt             → State + Intent + Effect (one file)
XRepository.kt        → Repository interface (in :core:domain)
XRepositoryImpl.kt    → Repository implementation
XUseCase.kt           → Single use case (one invoke())
XApiService.kt        → Retrofit service interface
XEntity.kt            → Room entity
XDao.kt               → Room DAO
XDto.kt               → API response data class
XMapper.kt            → DTO ↔ Domain model mapper
XModule.kt            → Hilt module

## Classes
- ViewModels:    MoviesViewModel, SeriesDetailViewModel, BankingViewModel
- States:        MoviesState, SeriesDetailState, BankingState
- Intents:       MoviesIntent, SeriesDetailIntent, BankingIntent
- Effects:       MoviesEffect, SeriesDetailEffect, BankingEffect
- UseCases:      GetSeriesListUseCase, GetSeriesDetailUseCase (verb + noun)
- Repositories:  MoviesRepository (interface), MoviesRepositoryImpl
- Entities:      WatchlistEntity
- DTOs:          SeriesDto, SeriesDetailDto, OmdbListResponseDto
- SDUI Models:   ScreenModel, ComponentNode, DataSourceModel, ActionModel

## Packages
com.example.moviesdemoapp.core.ui
com.example.moviesdemoapp.core.network
com.example.moviesdemoapp.core.data
com.example.moviesdemoapp.core.domain
com.example.moviesdemoapp.engine.sdui
com.example.moviesdemoapp.engine.navigation
com.example.moviesdemoapp.feature.movies
com.example.moviesdemoapp.feature.banking

## Functions
- Composables:  PascalCase — MoviesScreen(), SeriesCard(), SeriesDetailScreen()
- ViewModels:   camelCase — handleIntent(), reduceState()
- UseCases:     operator fun invoke() always
- Mappers:      toModel(), toEntity(), toDto()

## Constants
- Design tokens:  DesignTokens.ScreenBackground, DesignTokens.Accent
- Routes:         Routes.MOVIES, Routes.SERIES_DETAIL, Routes.BANKING
- Color tokens:   "screenBackground", "cardBackground", "primaryText", "secondaryText", "accent", "surface"
