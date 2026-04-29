// Prevents a duplicate JVM class name error during compilation by assigning a unique class name
@file:JvmName("PlayerUnique")

package com.noise.playerscreen.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.noise.R
import com.noise.playerscreen.model.PlayerScreenState
import com.noise.shared.NoiseType

/**
 * This Composable constructs the main player screen UI.
 * It includes controls for selecting the noise type, toggling audio effects (fade and waves),
 * adjusting the volume, and enabling or disabling the timer.
 *
 * @param state The current state of the player, containing user settings and playback flags.
 * @param modifier Modifier for customizing layout or styling externally.
 * @param noiseTypeChanged Callback triggered when a new noise type is selected.
 * @param fadeChanged Callback for toggling the fade effect.
 * @param wavesChanged Callback for toggling wave oscillation.
 * @param volumeChanged Callback to update the volume slider.
 * @param onTimerToggled Callback when the timer toggle is activated.
 */
@Composable
fun Player(
    state: PlayerScreenState,
    modifier: Modifier = Modifier,
    noiseTypeChanged: (NoiseType) -> Unit,
    fadeChanged: (Boolean) -> Unit,
    wavesChanged: (Boolean) -> Unit,
    volumeChanged: (Float) -> Unit,
    onTimerToggled: () -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        // Dropdown menu for choosing the type of ambient noise
        noiseSelector(state = state.noiseType) {
            noiseTypeChanged(it)
        }

        // Toggle control to activate or deactivate audio fade-out
        NoiseStateToggle(
            text = stringResource(id = R.string.fade_label),
            checked = state.fadeEnabled,
        ) {
            fadeChanged(it)
        }

        // Toggle switch to enable or disable oscillating wave audio effect
        NoiseStateToggle(
            text = stringResource(id = R.string.wave_label),
            checked = state.wavesEnabled,
        ) {
            wavesChanged(it)
        }

        // Slider control that adjusts the volume level
        VolumeControl(value = state.volume) {
            volumeChanged(it)
        }

        // Switches the timer functionality on or off
        TimerToggle(
            timeState = state.timerToggleState,
            onToggle = { onTimerToggled() },
        )
    }
}
