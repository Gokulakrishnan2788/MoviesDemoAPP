package com.example.moviesdemoapp.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.moviesdemoapp.feature.banking.ui.bankingGraph
import com.example.moviesdemoapp.feature.movies.ui.moviesGraph

@Composable
fun ArchitectNavHost(
    navController: NavHostController,
    startDestination: String = "movies_graph",
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        moviesGraph(navController)
        bankingGraph(navController)
    }
}
