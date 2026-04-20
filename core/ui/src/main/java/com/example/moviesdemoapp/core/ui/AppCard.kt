package com.example.moviesdemoapp.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/**
 * Surface card styled with MovieApp [DesignTokens].
 *
 * @param modifier optional [Modifier]
 * @param content slot rendered inside the card
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(DesignTokens.RadiusMd),
        colors = CardDefaults.cardColors(
            containerColor = DesignTokens.CardBackground,
        ),
    ) {
        Box(content = content)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0F14)
@Composable
private fun AppCardPreview() {
    MovieAppTheme {
        AppCard {
            Text(
                text = "Card content",
                modifier = Modifier.padding(DesignTokens.SpacingMd),
                color = DesignTokens.PrimaryText,
            )
        }
    }
}
