package com.example.moviesdemoapp.engine.navigation

/** All navigation route constants for the app. */
object Routes {
    const val SPLASH        = "splash"
    const val MAIN          = "main"
    const val BANKING       = "banking"

    // screenId is passed as a query param so ViewModels can read it from SavedStateHandle
    // without any feature code knowing which JSON file backs the screen.
    const val MOVIES        = "movies?screenId={screenId}"
    const val SERIES_DETAIL = "series_detail/{seriesId}?screenId={screenId}"

    /** Build a concrete series-detail route for [seriesId]. screenId uses its default. */
    fun seriesDetail(seriesId: String) = "series_detail/$seriesId"
}
