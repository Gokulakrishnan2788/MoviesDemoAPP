package com.example.moviesdemoapp.feature.banking.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.moviesdemoapp.engine.navigation.Routes

fun NavGraphBuilder.bankingGraph(navController: NavController) {
    navigation(startDestination = Routes.BANKING, route = "banking_graph") {
        composable(Routes.BANKING) {
            BankingScreen(navController = navController)
        }
    }
}
