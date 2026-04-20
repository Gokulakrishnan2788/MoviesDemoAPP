package com.example.moviesdemoapp.feature.movies.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.moviesdemoapp.engine.navigation.Routes
import com.example.moviesdemoapp.feature.movies.ui.detail.SeriesDetailScreen
import com.example.moviesdemoapp.feature.movies.ui.list.MoviesScreen

fun NavGraphBuilder.moviesGraph(navController: NavController) {
    navigation(startDestination = Routes.MOVIES, route = "movies_graph") {
        composable(Routes.MOVIES) {
            MoviesScreen(navController = navController)
        }
        composable(Routes.SERIES_DETAIL) { backStackEntry ->
            val seriesId = backStackEntry.arguments?.getString("seriesId").orEmpty()
            SeriesDetailScreen(seriesId = seriesId, navController = navController)
        }
    }
}
