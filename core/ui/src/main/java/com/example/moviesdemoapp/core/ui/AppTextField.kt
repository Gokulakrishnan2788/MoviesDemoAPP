package com.example.moviesdemoapp.core.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/**
 * Styled outlined text field using MovieApp [DesignTokens].
 *
 * @param value current text value
 * @param onValueChange callback when the text changes
 * @param modifier optional [Modifier]
 * @param label optional floating label
 * @param placeholder optional hint text
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = if (label.isNotEmpty()) ({ Text(label) }) else null,
        placeholder = if (placeholder.isNotEmpty()) ({ Text(placeholder) }) else null,
        shape = RoundedCornerShape(DesignTokens.RadiusMd),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DesignTokens.Accent,
            unfocusedBorderColor = DesignTokens.SecondaryText,
            focusedTextColor = DesignTokens.PrimaryText,
            unfocusedTextColor = DesignTokens.PrimaryText,
            cursorColor = DesignTokens.Accent,
        ),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0F14)
@Composable
private fun AppTextFieldPreview() {
    MovieAppTheme {
        AppTextField(
            value = "",
            onValueChange = {},
            label = "Search",
            placeholder = "Search movies...",
        )
    }
}
