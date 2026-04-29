package com.noise.audiocontrol

import android.media.AudioManager
import android.media.AudioManager.AUDIOFOCUS_LOSS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Enum representing the possible states of audio focus the app may experience.
 * These are used to control playback behavior based on whether the app is
 * allowed to play audio in the foreground or background.
 */
enum class AudioFocusState {
    FOCUS_GAINED,          // Full audio focus has been acquired.
    FOCUS_LOST_TRANSIENT,  // Temporarily lost focus (e.g., phone call).
    FOCUS_LOST_UNKNOWN,    // Lost focus for unknown or permanent reasons.
}

/**
 * Interface that defines audio focus operations. Implementations of this
 * interface will handle requesting and abandoning audio focus, as well as
 * exposing a reactive state for focus changes.
 */
interface AudioFocusManager {
    val focusState: StateFlow<AudioFocusState>  // A stream of current audio focus status.

    /**
     * Call this method to stop holding audio focus when playback ends or is paused.
     */
    fun abandon()

    /**
     * Request audio focus from the system before beginning playback.
     */
    fun request()
}

/**
 * Concrete implementation of AudioFocusManager using Android's AudioManager.
 * Responsible for handling platform-specific focus change events and
 * converting them into application-friendly states.
 */
class AudioFocusManagerImpl(
    private val audioManager: AudioManager, // System audio service instance
) : AudioFocusManager {

    // Internal mutable state used to track the current audio focus.
    private val _focusState = MutableStateFlow(AudioFocusState.FOCUS_LOST_UNKNOWN)

    // Exposed read-only version of the focus state for external use.
    override val focusState: StateFlow<AudioFocusState> = _focusState

    /**
     * Relinquish audio focus, typically called when playback is paused or stopped.
     * Prevents conflicts with other apps that may require audio focus.
     */
    override fun abandon() {
        audioManager.abandonAudioFocus(focusChangeListener)
    }

    /**
     * Request exclusive audio focus for music playback.
     * Required before starting audio to ensure proper user experience.
     */
    override fun request() {
        audioManager.requestAudioFocus(
            focusChangeListener,
            AudioManager.STREAM_MUSIC,        // Target the music stream
            AudioManager.AUDIOFOCUS_GAIN      // Request permanent gain of focus
        )
    }

    /**
     * Listener triggered by the system when audio focus changes.
     * Converts Android-specific focus change integers into app-friendly enum values.
     */
    private var focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        val audioFocusState = when {
            focusChange > 0 -> AudioFocusState.FOCUS_GAINED
            focusChange == AUDIOFOCUS_LOSS -> AudioFocusState.FOCUS_LOST_UNKNOWN
            else -> AudioFocusState.FOCUS_LOST_TRANSIENT
        }
        _focusState.value = audioFocusState
    }
}
