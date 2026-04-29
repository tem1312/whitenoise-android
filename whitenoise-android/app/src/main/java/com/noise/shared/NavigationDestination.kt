package com.noise.shared

/**
 * Represents the different destinations available for navigation in the app.
 * Each entry includes a string key used for identifying routes in the NavHost.
 */
enum class NavigationDestination(val key: String) {
    // Navigation route to the main player screen
    PLAYER("player"),

    // Navigation route to the settings screen
    SETTINGS("settings")
}
