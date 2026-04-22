package com.example.moviesdemoapp.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.example.moviesdemoapp.core.ui.MovieAppTheme
import com.example.moviesdemoapp.engine.navigation.Routes
import dagger.hilt.android.AndroidEntryPoint

/** Main activity — entry point for the primary app experience after splash. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovieAppTheme {
                val navController = rememberNavController()

                // Handle deep link navigation
                val initialRoute = intent.getStringExtra("route")
                if (initialRoute != null && savedInstanceState == null) {
                    // Navigate to the appropriate graph based on the route
                    val targetGraph = when (initialRoute) {
                        "banking", Routes.BANKING, Routes.BANKING_ADDRESS,
                        Routes.BANKING_FINENCIAL_DETAIL, Routes.BANKING_REVIEW_SUBMIT,
                        Routes.BANKING_PERSONAL_DETAIL -> "banking_graph"
                        else -> "movies_graph" // default to movies graph
                    }

                    // First navigate to the target graph
                    navController.navigate(targetGraph) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }

                    // If it's a specific screen route (not just the graph), navigate to it
                    if (initialRoute != "banking" && initialRoute != targetGraph) {
                        navController.navigate(initialRoute) {
                            launchSingleTop = true
                        }
                    }
                }

                MainScreen(navController = navController)
            }
        }
    }
}
