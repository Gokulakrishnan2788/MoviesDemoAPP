package com.example.moviesdemoapp.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.moviesdemoapp.core.ui.DesignTokens
import com.example.moviesdemoapp.core.ui.MovieAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

/** Splash screen activity — displays branding then navigates to [MainActivity]. */
@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovieAppTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DesignTokens.ScreenBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "MovieApp",
                        color = DesignTokens.PrimaryText,
                        fontSize = DesignTokens.TextXxl,
                        fontWeight = FontWeight.Bold,
                    )
                }
                LaunchedEffect(Unit) {
                    delay(1500L)
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}
