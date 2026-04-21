package com.example.moviesdemoapp.feature.movies.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.moviesdemoapp.engine.navigation.Routes
import com.example.moviesdemoapp.feature.movies.ui.detail.SeriesDetailScreen
import com.example.moviesdemoapp.feature.movies.ui.list.MoviesScreen

fun NavGraphBuilder.moviesGraph(navController: NavController) {
    navigation(startDestination = "movies", route = "movies_graph") {
        composable(
            route = Routes.MOVIES,
            arguments = listOf(
                navArgument("screenId") { defaultValue = "tv_series_list" },
            ),
        ) {
            MoviesScreen(navController = navController)
        }
        composable(
            route = Routes.SERIES_DETAIL,
            arguments = listOf(
                navArgument("seriesId") { type = NavType.StringType },
                navArgument("screenId") { defaultValue = "series_detail" },
            ),
        ) {
            SeriesDetailScreen(navController = navController)
        }
    }
}
