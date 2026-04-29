package com.noise.audiocontrol

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import com.noise.shared.NoiseType
import com.noise.shared.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.min

// Frequency at which audio behavior is updated (in milliseconds)
const val tickPeriod: Long = 100

// Fallback fade-out duration if none is explicitly specified
val DEFAULT_DECREASE_LENGTH = TimeUnit.HOURS.toMillis(1)

// The quietest volume allowed, expressed as a fraction of the max
const val minVolumePercent = 0.2f

/**
 * A simple data class representing the current audio playback state.
 */
data class SoundState(
    val fadeEnabled: Boolean,
    val wavesEnabled: Boolean,
    val millisLeft: Long,
    val noiseType: NoiseType,
    val playing: Boolean,
    val volume: Float,
) {
    companion object {
        // Default state when the app starts
        val default = SoundState(
            fadeEnabled = false,
            wavesEnabled = false,
            millisLeft = 0L,
            noiseType = NoiseType.WAVES,
            playing = false,
            volume = 1f,
        )
    }
}

/**
 * This class acts as the central manager for playback features like sound type, volume fading,
 * wave oscillation, and interaction with system audio focus.
 */
class AudioController(
    private val player: AudioPlayer,
    private val audioFocusManager: AudioFocusManager,
    private val looper: Looper,
    private val userPreferences: UserPreferences,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
) {
    private var _stateFlow = MutableStateFlow(SoundState.default)
    val stateFlow: StateFlow<SoundState> = _stateFlow

    private var volume = 1f
    private var oscillatingDown = true
    private var countDownTimer: CountDownTimer? = null
    private var startPlayingWhenFocusRegained = false
    private val handler = Handler(looper)

    private var oscillatePeriod: Int = 8000
    private var decreaseLength: Long = -1
    private var maxVolume = 1.0f
    private var minVolume = minVolumePercent

    init {
        // Start timer that runs tick() logic at fixed intervals
        val volumeChangerTimer = Timer()
        volumeChangerTimer.schedule(object : TimerTask() {
            override fun run() {
                handler.post { tick() }
            }
        }, 0, tickPeriod)

        // React to audio focus changes (e.g. incoming calls)
        coroutineScope.launch {
            audioFocusManager.focusState.collect {
                onAudioFocusChange(it)
            }
        }

        // Initialize with default noise type
        setNoiseType(_stateFlow.value.noiseType)
    }

    fun setNoiseType(noiseType: NoiseType) {
        _stateFlow.value = _stateFlow.value.copy(noiseType = noiseType)
        setFile(noiseType)
    }

    fun setFade(fadeEnabled: Boolean) {
        _stateFlow.value = _stateFlow.value.copy(fadeEnabled = fadeEnabled)
        if (!fadeEnabled && !_stateFlow.value.wavesEnabled) {
            volume = _stateFlow.value.volume
            player.setVolume(volume)
            maxVolume = _stateFlow.value.volume
        }
    }

    fun setWaves(wavesEnabled: Boolean) {
        _stateFlow.value = _stateFlow.value.copy(wavesEnabled = wavesEnabled)
        if (!wavesEnabled) {
            volume = min(_stateFlow.value.volume, maxVolume)
            player.setVolume(volume)
        }
    }

    fun setVolume(volume: Float) {
        maxVolume = volume
        this.volume = volume
        minVolume = volume * minVolumePercent
        oscillatingDown = true
        _stateFlow.value = _stateFlow.value.copy(volume = volume)
        player.setVolume(maxVolume)
    }

    private fun setFile(noiseType: NoiseType) {
        player.setFile(noiseType.soundFile)
        player.setVolume(maxVolume)
    }

    fun tick() {
        oscillatePeriod = userPreferences.waveIntervalMillis()
        val state = _stateFlow.value
        if (player.isPlaying()) {
            when {
                state.wavesEnabled && state.fadeEnabled -> waveAndFadeForTick()
                state.wavesEnabled -> waveForTick()
                state.fadeEnabled -> fadeForTick()
            }
            player.setVolume(volume)
        }
    }

    fun play() {
        player.play()
        countDownTimer?.start()
        _stateFlow.value = _stateFlow.value.copy(playing = true)

        if (!userPreferences.playOver()) {
            audioFocusManager.request()
        }
    }

    fun pause() {
        player.pause()
        _stateFlow.value = _stateFlow.value.copy(playing = false)
        volume = _stateFlow.value.volume
        maxVolume = volume
        player.setVolume(maxVolume)
        audioFocusManager.abandon()
    }

    fun setTimer(millis: Long) {
        _stateFlow.value = _stateFlow.value.copy(millisLeft = millis)
        decreaseLength = millis
        countDownTimer?.cancel()

        if (millis == 0L) return

        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _stateFlow.value = _stateFlow.value.copy(millisLeft = millisUntilFinished)
            }

            override fun onFinish() {
                _stateFlow.value = _stateFlow.value.copy(millisLeft = 0)
                pause()
            }
        }.apply {
            if (_stateFlow.value.playing) {
                start()
            }
        }
    }

    fun cancelTimer() {
        countDownTimer?.cancel()
    }

    fun stopTimer() {
        cancelTimer()
        decreaseLength = -1
        _stateFlow.value = _stateFlow.value.copy(millisLeft = 0)
    }

    private fun fadeForTick() {
        fadeMaxVolumeForTick()
        volume = maxVolume
    }

    private fun fadeMaxVolumeForTick() {
        if (maxVolume > minVolume) {
            val length = if (decreaseLength == -1L) DEFAULT_DECREASE_LENGTH else decreaseLength
            if (length == 0L) return
            val delta = -1 * (_stateFlow.value.volume - minVolume) / (length / tickPeriod)
            maxVolume += delta
        }
    }

    private fun waveAndFadeForTick() {
        fadeMaxVolumeForTick()
        waveForTick()
    }

    private fun waveForTick() {
        var delta = (maxVolume - minVolume) / (oscillatePeriod / 2 / tickPeriod)
        if (oscillatingDown) delta *= -1
        volume += delta

        if (volume <= minVolume) {
            volume = minVolume
            oscillatingDown = false
        }

        if (volume >= maxVolume) {
            volume = maxVolume
            oscillatingDown = true
        }
    }

    private fun onAudioFocusChange(focusState: AudioFocusState) {
        if (focusState == AudioFocusState.FOCUS_GAINED && startPlayingWhenFocusRegained) {
            play()
            startPlayingWhenFocusRegained = false
        } else if (!userPreferences.playOver() &&
            focusState != AudioFocusState.FOCUS_GAINED &&
            _stateFlow.value.playing
        ) {
            pause()
            if (focusState == AudioFocusState.FOCUS_LOST_TRANSIENT) {
                startPlayingWhenFocusRegained = true
            }
        }
    }
}
