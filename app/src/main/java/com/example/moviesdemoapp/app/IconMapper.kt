package com.example.moviesdemoapp.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Maps icon names from JSON (SDUI tab_config / screen configs) to Material3 icons.
 * Handles both legacy names (movie, account_balance) and new SDUI names
 * (film, building.columns) that mirror SF Symbol / icon-kit naming conventions.
 */
object IconMapper {
    fun getIcon(iconName: String): ImageVector {
        return when (iconName.lowercase()) {
            // New SDUI icon names (tab_config.json)
            "film"              -> Icons.Default.VideoLibrary
            "building.columns"  -> Icons.Default.AccountBalance
            // Legacy names (backward compat)
            "movie"             -> Icons.Default.Movie
            "account_balance"   -> Icons.Default.AccountBalance
            "wallet"            -> Icons.Default.AccountBalanceWallet
            else                -> Icons.Default.Movie
        }
    }
}

