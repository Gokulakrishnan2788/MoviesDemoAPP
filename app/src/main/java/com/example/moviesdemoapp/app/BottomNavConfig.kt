package com.example.moviesdemoapp.app

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a bottom navigation item from the JSON configuration.
 */
@Serializable
data class BottomNavItemConfig(
    val route: String,
    val label: String,
    val icon: String,
)

/**
 * Represents the entire bottom navigation configuration from JSON.
 */
@Serializable
data class BottomNavConfig(
    @SerialName("bottomNavItems")
    val items: List<BottomNavItemConfig>,
)

