package com.noise.shared

import android.content.SharedPreferences

// Legacy preference keys used by earlier versions of the application
@Deprecated("Legacy key no longer in active use")
const val PREF_USE_DARK_MODE_KEY_LEGACY = "pref_use_dark_mode"
const val PREF_USE_DARK_MODE_KEY = "pref_use_dark_mode_v2"
@Deprecated("Legacy key for wave interval preference")
const val PREF_WAVE_INTERVAL_KEY_LEGACY = "pref_oscillate_interval"
const val PREF_WAVE_INTERVAL_KEY = "pref_wave_interval"

// Current preference keys used by the application
const val PREF_PLAY_OVER = "pref_play_over"
const val PREF_LAST_USED_COLOR = "last_used_color"
const val PREF_LAST_VOLUME = "last_volume"
const val PREF_LAST_USED_WAVY = "last_used_wavy"
const val PREF_LAST_USED_FADE = "last_used_fade"
const val PREF_LAST_TIMER_TIME = "last_timer_time"
const val PREF_TIMER_ENABLED = "PREF_TIMER_ENABLED"

/**
 * Enum representing user-selectable themes for the UI.
 */
enum class DarkModeSetting(val key: Int) {
    AUTO(0),
    LIGHT(1),
    DARK(2),
}

/**
 * Interface that defines methods and properties for accessing user preferences.
 */
interface UserPreferences {
    fun playOver(): Boolean
    fun waveIntervalMillis(): Int

    var lastUsedColor: NoiseType
    var lastUsedVolume: Float
    var lastUsedWavy: Boolean
    var lastUsedFade: Boolean
    var lastTimerTimeMillis: Long
    var timerEnabled: Boolean

    fun migrateLegacyPreferences()
}

/**
 * Implementation of the UserPreferences interface using SharedPreferences for persistence.
 */
class UserPreferencesImpl(
    private val sharedPreferences: SharedPreferences,
) : UserPreferences {

    // Retrieve the user's preference for playing audio over other sources
    override fun playOver(): Boolean = sharedPreferences.getBoolean(PREF_PLAY_OVER, false)

    // Determine the selected wave interval in milliseconds based on the stored integer value
    override fun waveIntervalMillis(): Int {
        return when (sharedPreferences.getInt(PREF_WAVE_INTERVAL_KEY, 0)) {
            0 -> 8000
            1 -> 10000
            2 -> 12000
            3 -> 15000
            4 -> 30000
            else -> error("Unexpected preference value for wave interval")
        }
    }

    // Getter and setter for the last chosen noise type
    override var lastUsedColor: NoiseType
        get() {
            val prefString = sharedPreferences.getString(PREF_LAST_USED_COLOR, NoiseType.WAVES.prefValue)
            return NoiseType.fromPrefValue(prefString ?: NoiseType.WAVES.prefValue)
        }
        set(value) {
            sharedPreferences.edit().putString(PREF_LAST_USED_COLOR, value.prefValue).apply()
        }

    // Retrieve or update the last selected volume level
    override var lastUsedVolume: Float
        get() = sharedPreferences.getFloat(PREF_LAST_VOLUME, 1.0f)
        set(value) {
            sharedPreferences.edit().putFloat(PREF_LAST_VOLUME, value).apply()
        }

    // Determine or save whether wave oscillation was previously enabled
    override var lastUsedWavy: Boolean
        get() = sharedPreferences.getBoolean(PREF_LAST_USED_WAVY, false)
        set(value) {
            sharedPreferences.edit().putBoolean(PREF_LAST_USED_WAVY, value).apply()
        }

    // Retrieve or update the fade effect setting
    override var lastUsedFade: Boolean
        get() = sharedPreferences.getBoolean(PREF_LAST_USED_FADE, false)
        set(value) {
            sharedPreferences.edit().putBoolean(PREF_LAST_USED_FADE, value).apply()
        }

    // Access or modify the stored timer duration
    override var lastTimerTimeMillis: Long
        get() = sharedPreferences.getLong(PREF_LAST_TIMER_TIME, 0)
        set(value) {
            sharedPreferences.edit().putLong(PREF_LAST_TIMER_TIME, value).apply()
        }

    // Boolean flag indicating whether the timer feature is active
    override var timerEnabled: Boolean
        get() = sharedPreferences.getBoolean(PREF_TIMER_ENABLED, false)
        set(value) {
            sharedPreferences.edit().putBoolean(PREF_TIMER_ENABLED, value).apply()
        }

    /**
     * Transfers data from outdated preference keys to newer equivalents,
     * preserving user settings during application upgrades.
     */
    override fun migrateLegacyPreferences() {
        val legacyWavePref = sharedPreferences.getString(PREF_WAVE_INTERVAL_KEY_LEGACY, "") ?: ""
        if (legacyWavePref.isNotEmpty()) {
            val newWaveIndex = when (legacyWavePref.toIntOrNull()) {
                8 -> 0
                10 -> 1
                12 -> 2
                15 -> 3
                30 -> 4
                else -> 0
            }
            sharedPreferences.edit().putInt(PREF_WAVE_INTERVAL_KEY, newWaveIndex).apply()
        }

        val legacyDarkMode = sharedPreferences.getBoolean(PREF_USE_DARK_MODE_KEY_LEGACY, false)
        if (legacyDarkMode) {
            sharedPreferences.edit().putInt(PREF_USE_DARK_MODE_KEY, DarkModeSetting.DARK.key).apply()
        }
    }
}
