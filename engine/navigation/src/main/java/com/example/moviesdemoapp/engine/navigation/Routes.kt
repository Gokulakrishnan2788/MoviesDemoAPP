package com.example.moviesdemoapp.engine.navigation

/** All navigation route constants for the app. */
object Routes {
    const val SPLASH         = "splash"
    const val MAIN           = "main"
    const val MOVIES         = "movies"
    const val SERIES_DETAIL  = "series_detail/{seriesId}"
    const val BANKING        = "banking"

    /** Build a concrete series-detail route for [seriesId]. */
    fun seriesDetail(seriesId: String) = "series_detail/$seriesId"
}
