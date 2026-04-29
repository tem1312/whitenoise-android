package com.noise.playerscreen.model

import com.noise.playerscreen.view.TimerPickerState
import com.noise.playerscreen.view.TimerToggleState
import com.noise.shared.NoiseType

/**
 * Represents the full UI state of the Player Screen.
 * This model is used to manage all user-configurable options and playback status.
 *
 * @property noiseType The currently selected ambient noise (e.g., white noise, waves, etc.)
 * @property fadeEnabled Indicates if fade-out effect is active.
 * @property wavesEnabled Indicates if oscillation/wave effect is active.
 * @property volume The current volume level (0.0 to 1.0).
 * @property timerToggleState Represents the state of the timer (enabled, disabled).
 * @property showTimerPicker Controls visibility of the timer duration picker.
 * @property timerPickerState Current selected duration in the timer UI.
 * @property playing Indicates whether audio is currently being played.
 */
data class PlayerScreenState(
    val noiseType: NoiseType,
    val fadeEnabled: Boolean,
    val wavesEnabled: Boolean,
    val volume: Float,
    val timerToggleState: TimerToggleState,
    val showTimerPicker: Boolean,
    val timerPickerState: TimerPickerState,
    val playing: Boolean,
) {
    companion object {
        /**
         * Provides a clean, default configuration for the player screen.
         * Used during first launch or to reset UI state.
         */
        val default = PlayerScreenState(
            noiseType = NoiseType.WAVES,
            fadeEnabled = false,
            wavesEnabled = false,
            timerToggleState = TimerToggleState.Disabled,
            showTimerPicker = false,
            timerPickerState = TimerPickerState.zero,
            volume = 1f,
            playing = false,
        )
    }
}
