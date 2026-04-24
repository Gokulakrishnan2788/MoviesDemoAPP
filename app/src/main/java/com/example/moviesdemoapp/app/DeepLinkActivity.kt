package com.example.moviesdemoapp.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.deeplink.DeepLinkScreen
import com.example.moviesdemoapp.core.ui.MovieAppTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DeepLinkActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent?.data
        val screenName = uri?.lastPathSegment ?: "home"
        val symbol = uri?.getQueryParameter("symbol")
        enableEdgeToEdge()
        setContent {
            MovieAppTheme {
                DeepLinkScreen(
                    page = screenName,
                    indexPage = symbol,
                    onNavigate = { route ->
                        // Handle navigation in DeepLinkActivity context
                        // For example, start MainActivity with the route
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra("route", route)
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
    }
}