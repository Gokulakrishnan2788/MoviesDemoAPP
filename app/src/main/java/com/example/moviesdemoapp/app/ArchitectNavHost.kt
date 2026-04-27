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
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = "movies_graph",
        modifier = modifier,
    ) {
        moviesGraph(navController)
        bankingGraph(navController)
    }
}
