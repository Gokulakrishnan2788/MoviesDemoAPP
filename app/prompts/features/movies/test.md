# Movies Feature — Tests

## Files to generate:
# feature/movies/test/MoviesViewModelTest.kt
# feature/movies/test/SeriesDetailViewModelTest.kt
# feature/movies/test/GetSeriesListUseCaseTest.kt
# feature/movies/test/GetSeriesDetailUseCaseTest.kt

## MoviesViewModelTest cases:
- given_loadScreen_when_useCaseSucceeds_then_stateHasListData
- given_loadScreen_when_useCaseFails_then_stateHasError
- given_seriesTappedAction_when_handled_then_navigationEffectEmitted
- given_loadScreen_when_called_then_isLoadingTrueInitially

## SeriesDetailViewModelTest cases:
- given_loadScreen_when_detailFetched_then_stateHasDataMap
- given_loadScreen_when_apiFails_then_stateHasError
- given_backAction_when_handled_then_navigateBackEffectEmitted

## GetSeriesListUseCaseTest cases:
- given_validQuery_when_invoked_then_returnsEnrichedList
- given_apiError_when_invoked_then_returnsError
- given_emptySearchResult_when_invoked_then_returnsEmptyList

## GetSeriesDetailUseCaseTest cases:
- given_validImdbID_when_invoked_then_returnsDetailModel
- given_invalidImdbID_when_invoked_then_returnsError

## Mock data helpers
val mockSeriesDto = SeriesDto(
    title = "Squid Game",
    year = "2021-2025",
    imdbID = "tt10919420",
    type = "series",
    poster = "https://example.com/poster.jpg"
)

val mockSeriesDetailDto = SeriesDetailDto(
    title = "Squid Game",
    year = "2021-2025",
    genre = "Action, Drama",
    imdbID = "tt10919420",
    rating = "7.9",
    poster = "https://example.com/poster.jpg",
    runtime = "60 min",
    totalSeasons = "2",
    awards = "Won 6 Primetime Emmy Awards",
    plot = "Hundreds of cash-strapped players...",
    actors = "Lee Jung-jae, Park Hae-soo",
    writer = "Hwang Dong-hyuk",
    director = "Hwang Dong-hyuk",
    response = "True"
)
