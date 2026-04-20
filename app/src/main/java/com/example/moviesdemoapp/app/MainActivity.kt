package com.example.moviesdemoapp.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.moviesdemoapp.core.ui.MovieAppTheme
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
                MainScreen(navController = navController)
            }
        }
    }
}
