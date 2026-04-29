package com.noise.shared

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.alorma.compose.settings.storage.preferences.IntPreferenceSettingValueState

@Composable

// Returns true if the user's preference or system setting indicates dark mode should be used.
fun IntPreferenceSettingValueState.isDarkMode(): Boolean {
    return when (this.value) {
        DarkModeSetting.AUTO.key -> isSystemInDarkTheme()  // Follows system-wide dark mode setting
        DarkModeSetting.LIGHT.key -> false                 // User explicitly chose light mode
        DarkModeSetting.DARK.key -> true                   // User explicitly chose dark mode
        else -> error("Invalid value found for theme preference")
    }
}
