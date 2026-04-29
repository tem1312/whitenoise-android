package com.noise.service.model

import androidx.annotation.StringRes
import com.noise.AudioPlayerButton

/**
 * Represents the visibility state of the audio player's screen in the UI layer.
 * It is modeled as a sealed class, allowing the UI to either display content (Shown)
 * or remain hidden (Hidden).
 */
sealed class AudioPlayerScreenState {

    /**
     * Represents a visible state of the player screen, containing the UI text and control buttons.
     *
     * @property titleResource A string resource ID for the title displayed in the player.
     * @property subtitleResource A string resource ID for the subtitle or description.
     * @property firstButton The primary button to appear on the screen.
     * @property secondButton The secondary button to appear on the screen.
     */
    data class Shown(
        @StringRes val titleResource: Int,
        @StringRes val subtitleResource: Int,
        val firstButton: AudioPlayerButton,
        val secondButton: AudioPlayerButton,
    ) : AudioPlayerScreenState()

    /**
     * Represents a state where the player UI is not shown or has been dismissed.
     */
    object Hidden : AudioPlayerScreenState()
}
