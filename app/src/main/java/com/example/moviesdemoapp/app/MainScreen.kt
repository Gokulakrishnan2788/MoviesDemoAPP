package com.example.moviesdemoapp.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.moviesdemoapp.app.tab.TabBarViewModel
import com.example.moviesdemoapp.core.ui.DesignTokens

/**
 * Root shell composable.
 *
 * Tab structure and labels are fully driven by tab_config.json via [TabBarViewModel].
 * No tab data is hardcoded here — adding or reordering tabs requires only a JSON change.
 */
@Composable
fun MainScreen(navController: NavHostController) {
    val viewModel: TabBarViewModel = hiltViewModel()
    val tabs = viewModel.tabs

    Scaffold(
        containerColor = DesignTokens.ScreenBackground,
        bottomBar = {
            NavigationBar(containerColor = DesignTokens.CardBackground) {
                val navBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStack?.destination?.route
                tabs.forEach { tab ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = IconMapper.getIcon(tab.icon),
                                contentDescription = tab.title,
                            )
                        },
                        label = { Text(tab.title) },
                        selected = currentRoute == tab.graphRoute,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DesignTokens.Accent,
                            selectedTextColor = DesignTokens.Accent,
                            unselectedIconColor = DesignTokens.SecondaryText,
                            unselectedTextColor = DesignTokens.SecondaryText,
                            indicatorColor = DesignTokens.Surface,
                        ),
                        onClick = {
                            navController.navigate(tab.graphRoute) {
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
            startDestination = viewModel.startDestination,
            modifier = Modifier.padding(paddingValues),
        )
    }
}
