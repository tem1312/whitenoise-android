package com.noise.playerscreen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noise.audiocontrol.AudioController
import com.noise.audiocontrol.SoundState
import com.noise.playerscreen.model.PlayerScreenState
import com.noise.playerscreen.view.TimerPickerState
import com.noise.playerscreen.view.TimerToggleState
import com.noise.shared.NoiseType
import com.noise.shared.UserPreferences
import kotlinx.coroutines.launch

/**
 * ViewModel managing all logic and state for the player screen.
 * It interacts with the AudioController and user preferences,
 * keeping the UI state consistent with audio behavior and settings.
 */
class PlayerScreenViewModel(
    private val userPreferences: UserPreferences,
) : ViewModel() {

    private var _playerScreenState = MutableLiveData<PlayerScreenState>()
    val playerScreenState: LiveData<PlayerScreenState> = _playerScreenState

    // Controller responsible for managing audio playback and behaviors
    private var audioController: AudioController? = null

    /**
     * Binds the audio controller to the ViewModel, allowing playback management and state observation.
     */
    fun bindAudioController(audioController: AudioController) {
        this.audioController = audioController
        loadPastPreferences()

        // Observe updates from AudioController and map them to the UI state
        viewModelScope.launch {
            audioController.stateFlow.collect {
                _playerScreenState.value = mapSoundStateToPlayerScreenState(it)
            }
        }
    }

    /**
     * Translates raw audio state data into the screen-specific PlayerScreenState structure.
     */
    private fun mapSoundStateToPlayerScreenState(soundState: SoundState): PlayerScreenState {
        val previousTimerState = _playerScreenState.value?.timerToggleState ?: TimerToggleState.Disabled
        val timerState = if (previousTimerState is TimerToggleState.Saved) {
            if (soundState.millisLeft == 0L) TimerToggleState.Disabled
            else previousTimerState.copy(displayedTime = soundState.millisLeft.millisToTimerState())
        } else previousTimerState

        return PlayerScreenState(
            noiseType = soundState.noiseType,
            fadeEnabled = soundState.fadeEnabled,
            playing = soundState.playing,
            wavesEnabled = soundState.wavesEnabled,
            timerToggleState = timerState,
            showTimerPicker = _playerScreenState.value?.showTimerPicker ?: false,
            timerPickerState = _playerScreenState.value?.timerPickerState ?: TimerPickerState.zero,
            volume = soundState.volume,
        )
    }

    /**
     * Restores the previously saved preferences and updates the audio controller with them.
     */
    private fun loadPastPreferences() {
        _playerScreenState.value = _playerScreenState.value?.copy(
            noiseType = userPreferences.lastUsedColor,
            volume = userPreferences.lastUsedVolume,
            wavesEnabled = userPreferences.lastUsedWavy,
            fadeEnabled = userPreferences.lastUsedFade,
        )

        audioController?.setNoiseType(userPreferences.lastUsedColor)
        audioController?.setVolume(userPreferences.lastUsedVolume)
        audioController?.setWaves(userPreferences.lastUsedWavy)
        audioController?.setFade(userPreferences.lastUsedFade)

        val currentState = audioController?.stateFlow?.value
        if (currentState?.playing == false &&
            currentState.millisLeft == 0L &&
            userPreferences.lastTimerTimeMillis != 0L &&
            userPreferences.timerEnabled) {
            _playerScreenState.value = _playerScreenState.value?.copy(
                timerToggleState = TimerToggleState.Saved(userPreferences.lastTimerTimeMillis.millisToTimerState())
            )
            audioController?.setTimer(userPreferences.lastTimerTimeMillis)
        }
    }

    /**
     * Unbinds the audio controller, used when the ViewModel is no longer managing playback.
     */
    fun clearAudioController() {
        audioController = null
    }

    fun changeNoiseType(noiseType: NoiseType) {
        userPreferences.lastUsedColor = noiseType
        audioController?.setNoiseType(noiseType)
    }

    fun toggleFade(enabled: Boolean) {
        userPreferences.lastUsedFade = enabled
        audioController?.setFade(enabled)
    }

    fun toggleWaves(enabled: Boolean) {
        userPreferences.lastUsedWavy = enabled
        audioController?.setWaves(enabled)
    }

    fun changeVolume(newVolume: Float) {
        userPreferences.lastUsedVolume = newVolume
        audioController?.setVolume(newVolume)
    }

    /**
     * Adjusts the timer by modifying the total minutes based on user interaction.
     */
    fun updateTimer(timerChange: Int) {
        val timerState = _playerScreenState.value?.timerPickerState ?: return
        val currentMinutes = timerState.minutes + timerState.minutesTens * 10 + timerState.hours * 60
        val newMinutes = currentMinutes + timerChange
        if (newMinutes < 0) return
        _playerScreenState.value = _playerScreenState.value?.copy(
            timerPickerState = newMinutes.minutesToTimerPickerState()
        )
    }

    /**
     * Finalizes the timer configuration and updates both preferences and controller.
     */
    fun setTimer() {
        if (_playerScreenState.value?.showTimerPicker != true) return
        val timeState = _playerScreenState.value?.timerPickerState ?: return

        userPreferences.lastTimerTimeMillis = timeState.toMillis()

        val timerState = if (timeState != TimerPickerState.zero) {
            audioController?.setTimer(timeState.toMillis())
            userPreferences.timerEnabled = true
            TimerToggleState.Saved(timeState.toFormattedString())
        } else {
            userPreferences.timerEnabled = false
            TimerToggleState.Disabled
        }

        _playerScreenState.value = _playerScreenState.value?.copy(
            timerToggleState = timerState,
            showTimerPicker = false
        )
    }

    /**
     * Disables the current timer and clears related stored data.
     */
    fun cancelTimer() {
        userPreferences.lastTimerTimeMillis = 0L
        userPreferences.timerEnabled = false
        _playerScreenState.value = _playerScreenState.value?.copy(
            timerToggleState = TimerToggleState.Disabled,
            showTimerPicker = false
        )
    }

    /**
     * Toggles the display of the timer picker or disables the timer if already enabled.
     */
    fun toggleTimer() {
        val current = _playerScreenState.value?.timerToggleState
        if (current is TimerToggleState.Disabled) {
            _playerScreenState.value = _playerScreenState.value?.copy(
                showTimerPicker = true,
                timerPickerState = userPreferences.lastTimerTimeMillis.millisToTimerPickerState()
            )
        } else {
            audioController?.setTimer(0)
            userPreferences.timerEnabled = false
            _playerScreenState.value = _playerScreenState.value?.copy(
                timerToggleState = TimerToggleState.Disabled
            )
        }
    }

    /**
     * Manages the playback toggle interaction (play/pause).
     */
    fun togglePlay(playing: Boolean) {
        if (playing) audioController?.play()
        else audioController?.pause()
    }
}

// Extension to convert milliseconds to a timer string (HH:mm:ss format)
private fun Long.millisToTimerState(): String {
    val seconds = (this / 1000).toInt()
    return seconds.secondsToString()
}

// Extension to transform milliseconds into a structured timer picker state
private fun Long.millisToTimerPickerState(): TimerPickerState {
    return (this / 60000L).toInt().minutesToTimerPickerState()
}

// Extension to convert total minutes into a TimerPickerState object
private fun Int.minutesToTimerPickerState(): TimerPickerState {
    return TimerPickerState(
        hours = this / 60,
        minutesTens = (this % 60) / 10,
        minutes = this % 10,
    )
}

// Converts a TimerPickerState into a total duration in milliseconds
private fun TimerPickerState.toMillis(): Long {
    return (hours * 60 * 60L + minutesTens * 10 * 60L + minutes * 60L) * 1000
}

// Formats TimerPickerState into a display-friendly timer string
private fun TimerPickerState.toFormattedString(): String {
    return "$hours:${(minutesTens * 10 + minutes).withTensPadding()}:00"
}

// Converts seconds into HH:mm:ss format
private fun Int.secondsToString(): String {
    val hours = this / 3600
    val minutes = (this / 60) % 60
    val seconds = this % 60
    return "$hours:${minutes.withTensPadding()}:${seconds.withTensPadding()}"
}

// Pads single-digit numbers with a leading zero
private fun Int.withTensPadding(): String {
    return if (this < 10) "0$this" else this.toString()
}
