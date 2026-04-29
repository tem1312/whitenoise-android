package com.noise.settings.view

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Waves
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.alorma.compose.settings.storage.base.SettingValueState
import com.alorma.compose.settings.ui.SettingsCheckbox
import com.alorma.compose.settings.ui.SettingsList
import com.noise.R
import com.noise.shared.PREF_PLAY_OVER
import com.noise.shared.PREF_WAVE_INTERVAL_KEY
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.stringArrayResource
import com.alorma.compose.settings.storage.preferences.rememberPreferenceBooleanSettingState
import com.alorma.compose.settings.storage.preferences.rememberPreferenceIntSettingState

/**
 * Main layout for the app’s settings screen.
 * This Composable contains the top navigation bar and delegates the body
 * to the AllSettings() function which handles the actual settings.
 */
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SettingsScreen(
    version: String,
    darkThemeState: SettingValueState<Int>,
    onBackPressed: () -> Unit,
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        AllSettings(
            version = version,
            darkThemeState = darkThemeState,
            playOverState = rememberPreferenceBooleanSettingState(
                key = PREF_PLAY_OVER,
                defaultValue = false
            ),
            waveState = rememberPreferenceIntSettingState(
                key = PREF_WAVE_INTERVAL_KEY,
                defaultValue = 0
            ),
            onViewUsageStats = {
                navController.navigate("usage_stats")
            }
        )
    }
}

/**
 * Displays all configurable options within the settings screen.
 * This includes toggles and lists for theme, playback, oscillation frequency,
 * and navigation to the usage stats screen.
 */
@Composable
private fun AllSettings(
    version: String,
    darkThemeState: SettingValueState<Int>,
    playOverState: SettingValueState<Boolean>,
    waveState: SettingValueState<Int>,
    onViewUsageStats: () -> Unit
) {
    println("AllSettings rendered!")

    Column(Modifier.padding(16.dp)) {

        // Theme selection list with dark/light options
        SettingsList(
            state = darkThemeState,
            title = { Text(stringResource(id = R.string.dark_theme_toggle)) },
            subtitle = { Text(stringResource(id = R.string.dark_theme_summary)) },
            items = stringArrayResource(id = R.array.theme_choices).toList(),
            icon = { Icon(Icons.Default.DarkMode, contentDescription = null) },
        )

        // Switch that allows playback to continue when audio focus is lost
        SettingsCheckbox(
            state = playOverState,
            title = { Text(stringResource(id = R.string.play_over_toggle)) },
            subtitle = { Text(stringResource(id = R.string.play_over_summary)) },
            icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
        )

        // Dropdown menu for configuring how frequently wave oscillation changes volume
        SettingsList(
            state = waveState,
            title = { Text(stringResource(id = R.string.wave_interval_choice_title)) },
            subtitle = { Text(stringResource(id = R.string.oscillate_interval_summary)) },
            items = stringArrayResource(id = R.array.wave_interval_choices).toList(),
            icon = { Icon(Icons.Default.Waves, contentDescription = null) },
        )

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Clickable item to navigate to the usage statistics screen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onViewUsageStats() }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column {
                Text("View App Usage Stats", style = MaterialTheme.typography.body1)
                Text("Track time app has been used", style = MaterialTheme.typography.caption)
            }
        }
    }
}
