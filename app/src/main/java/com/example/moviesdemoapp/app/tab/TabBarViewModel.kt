package com.example.moviesdemoapp.app.tab

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Exposes SDUI-driven tab configuration to [MainScreen].
 *
 * Both properties delegate to [TabConfigLoader] which is a @Singleton —
 * the work is done once and cached, so the ViewModel itself is stateless.
 */
@HiltViewModel
class TabBarViewModel @Inject constructor(
    loader: TabConfigLoader,
) : ViewModel() {

    /** Fully resolved tabs ready for direct use in Compose UI. */
    val tabs: List<ResolvedTab> = loader.resolvedTabs

    /** The navigation graph route that should be selected on first launch. */
    val startDestination: String = loader.defaultTabRoute
}
