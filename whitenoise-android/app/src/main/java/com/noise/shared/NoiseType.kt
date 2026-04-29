package com.noise.shared

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.noise.R
import java.lang.IllegalStateException

// Predefined list of noise types used throughout the app for playback and selection
val defaultNoiseTypes = listOf(
    NoiseType.WAVES,
    NoiseType.CAR,
    NoiseType.BIRDS,
    NoiseType.WIND,
)

/**
 * Enumeration representing different ambient noise types available in the app.
 * Each type is linked to a specific audio file, label for display, and preference identifier.
 */
enum class NoiseType(
    @RawRes val soundFile: Int,              // Audio resource associated with this noise type
    @StringRes val notificationTitle: Int,   // Title shown in the notification for this sound
    val prefValue: String,                   // String used to persist user preference
    @StringRes val label: Int,               // String label for UI components
) {
    WAVES(
        soundFile = R.raw.waves,
        notificationTitle = R.string.notification_waves_type,
        prefValue = "last_color_was_waves",
        label = R.string.waves_label,
    ),
    CAR(
        soundFile = R.raw.car_noise,
        notificationTitle = R.string.notification_car_type,
        prefValue = "last_color_was_car",
        label = R.string.car_label,
    ),
    BIRDS(
        soundFile = R.raw.birds,
        notificationTitle = R.string.notification_birds_type,
        prefValue = "last_color_was_birds",
        label = R.string.birds_label,
    ),
    WIND(
        soundFile = R.raw.wind,
        notificationTitle = R.string.notification_wind_type,
        prefValue = "last_color_was_wind",
        label = R.string.wind_label,
    ),
    NONE(0, 0, "", 0); // Represents the absence of a selected noise type

    companion object {
        /**
         * Converts a saved preference string back into the corresponding [NoiseType].
         * If the string doesn't match any known value, an exception is thrown.
         *
         * @param prefValue The string saved in user preferences.
         * @return The corresponding [NoiseType].
         */
        @JvmStatic
        fun fromPrefValue(prefValue: String): NoiseType {
            return values().find { it.prefValue == prefValue }
                ?: throw IllegalStateException("Unknown preference value: $prefValue")
        }
    }
}
