package com.example.moviesdemoapp.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/**
 * Styled top app bar using MovieApp [DesignTokens].
 *
 * @param title text shown in the bar
 * @param navigationIcon optional back/hamburger icon slot
 * @param actions optional action icons slot (right side)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                color = DesignTokens.PrimaryText,
                fontSize = DesignTokens.TextXl,
            )
        },
        navigationIcon = { navigationIcon?.invoke() },
        actions = { actions?.invoke() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DesignTokens.ScreenBackground,
            titleContentColor = DesignTokens.PrimaryText,
        ),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0F14)
@Composable
private fun AppTopBarPreview() {
    MovieAppTheme {
        AppTopBar(title = "Movies")
    }
}
