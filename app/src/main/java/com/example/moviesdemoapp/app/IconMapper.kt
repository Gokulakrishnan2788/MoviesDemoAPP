package com.example.moviesdemoapp.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Movie
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Maps icon names from JSON to Material3 Icons.
 */
object IconMapper {
    fun getIcon(iconName: String): ImageVector {
        return when (iconName.lowercase()) {
            "movie" -> Icons.Default.Movie
            "account_balance" -> Icons.Default.AccountBalance
            else -> Icons.Default.Movie // Default icon fallback
        }
    }
}

