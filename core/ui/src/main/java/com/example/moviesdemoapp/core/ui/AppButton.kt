package com.example.moviesdemoapp.core.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Full-width primary button styled with MovieApp [DesignTokens].
 *
 * @param text label displayed on the button
 * @param onClick invoked on tap
 * @param modifier optional [Modifier]
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(DesignTokens.RadiusMd),
        colors = ButtonDefaults.buttonColors(
            containerColor = DesignTokens.Accent,
            contentColor = DesignTokens.PrimaryText,
        ),
    ) {
        Text(text = text, fontSize = DesignTokens.TextLg)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0F14)
@Composable
private fun AppButtonPreview() {
    MovieAppTheme {
        AppButton(text = "Watch Now", onClick = {})
    }
}
