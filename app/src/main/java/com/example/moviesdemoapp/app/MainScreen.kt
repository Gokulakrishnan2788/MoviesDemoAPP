package com.example.moviesdemoapp.app

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.moviesdemoapp.core.ui.DesignTokens
import com.example.moviesdemoapp.engine.navigation.Routes

/** Describes a single bottom navigation tab. */
data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

/**
 * Root shell composable — houses the bottom navigation bar and the nav host.
 * Loads bottom navigation configuration dynamically from JSON.
 */
@Composable
fun MainScreen(navController: NavHostController) {
    val context = LocalContext.current
    
    // Load bottom navigation items from JSON configuration
    val bottomNavItems = remember(context) {
        val config = BottomNavConfigLoader.loadConfig(context)
        config.items.map { item ->
            BottomNavItem(
                route = item.route,
                label = item.label,
                icon = IconMapper.getIcon(item.icon),
            )
        }
    }

    Scaffold(
        containerColor = DesignTokens.ScreenBackground,
        bottomBar = {
            NavigationBar(containerColor = DesignTokens.CardBackground) {
                val navBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStack?.destination?.route
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DesignTokens.Accent,
                            selectedTextColor = DesignTokens.Accent,
                            unselectedIconColor = DesignTokens.SecondaryText,
                            unselectedTextColor = DesignTokens.SecondaryText,
                            indicatorColor = DesignTokens.Surface,
                        ),
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        },
    ) { paddingValues ->
        ArchitectNavHost(
            navController = navController,
            modifier = Modifier.padding(paddingValues),
        )
    }
}
