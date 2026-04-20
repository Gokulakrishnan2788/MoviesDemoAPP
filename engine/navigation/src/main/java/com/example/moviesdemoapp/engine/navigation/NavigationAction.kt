package com.example.moviesdemoapp.engine.navigation

/** Navigation behaviour types. */
enum class NavType {
    /** Push destination onto the back stack. */
    PUSH,
    /** Replace entire back stack with destination. */
    REPLACE,
    /** Pop the current screen. */
    POP,
    /** Navigate via deep link URI. */
    DEEP_LINK,
}

/**
 * Describes a navigation event emitted by a ViewModel as a [UiEffect].
 *
 * @param type how the navigation should be performed
 * @param destination route string or deep-link URI
 * @param params additional key-value pairs passed to the destination
 */
data class NavigationAction(
    val type: NavType,
    val destination: String = "",
    val params: Map<String, String> = emptyMap(),
)
