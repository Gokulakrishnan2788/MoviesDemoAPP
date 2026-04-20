package com.example.moviesdemoapp.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

private val DarkColorScheme = darkColorScheme(
    background = DesignTokens.ScreenBackground,
    surface = DesignTokens.CardBackground,
    primary = DesignTokens.Accent,
    onPrimary = DesignTokens.PrimaryText,
    onBackground = DesignTokens.PrimaryText,
    onSurface = DesignTokens.PrimaryText,
    secondary = DesignTokens.Surface,
    onSecondary = DesignTokens.PrimaryText,
)

/**
 * Root theme for MovieApp. Wrap every screen with this composable.
 */
@Composable
fun MovieAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content,
    )
}

@Preview(showBackground = true)
@Composable
private fun MovieAppThemePreview() {
    MovieAppTheme {}
}
