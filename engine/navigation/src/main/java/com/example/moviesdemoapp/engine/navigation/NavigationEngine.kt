package com.example.moviesdemoapp.engine.navigation

import androidx.navigation.NavController

/**
 * Translates [NavigationAction] events from ViewModels into [NavController] calls.
 * Stateless object — call [navigate] from a [LaunchedEffect] in the screen composable.
 */
object NavigationEngine {

    /**
     * Execute [action] on the provided [navController].
     */
    fun navigate(navController: NavController, action: NavigationAction) {
        when (action.type) {
            NavType.PUSH -> navController.navigate(action.destination)
            NavType.REPLACE -> navController.navigate(action.destination) {
                popUpTo(0) { inclusive = true }
            }
            NavType.POP -> navController.popBackStack()
            NavType.DEEP_LINK -> navController.navigate(action.destination)
        }
    }
}
