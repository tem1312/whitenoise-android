package com.noise.playerscreen.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.noise.R
import com.noise.shared.WhiteNoiseTypography

/**
 * Represents the time selected in the timer picker in terms of hours,
 * tens of minutes, and single minutes. Used for internal timer configuration.
 */
data class TimerPickerState(
    val hours: Int,
    val minutesTens: Int,
    val minutes: Int,
) {
    companion object {
        // Predefined zero state for resetting or initializing the timer
        val zero = TimerPickerState(0, 0, 0)
    }
}

@Preview
@Composable
        /**
         * Preview function that allows testing the TimerPicker layout in isolation using default values.
         */
fun TimerPickerPreview() {
    TimerPicker(
        pickerState = TimerPickerState.zero,
        onChange = {},
        onSet = {},
        onCancel = {},
    )
}

@Composable
        /**
         * Composable function that renders a full timer picker UI.
         * Allows users to increment/decrement hours and minutes and apply or cancel the selection.
         *
         * @param pickerState The current state of the timer in structured format.
         * @param onChange Callback that adjusts the timer in minutes (positive or negative).
         * @param onSet Callback triggered when the user confirms the time.
         * @param onCancel Callback triggered when the user cancels the action.
         */
fun TimerPicker(
    pickerState: TimerPickerState,
    modifier: Modifier = Modifier,
    onChange: (Int) -> Unit,
    onSet: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Controls for incrementing/decrementing the hour value
            TimeUnitPicker(
                value = pickerState.hours,
                digitPositions = 2,
                onIncrement = { onChange(60) },
                onDecrement = { onChange(-60) },
            )

            // ":" separator text between hours and minutes
            Text(
                text = stringResource(id = R.string.time_divider),
                style = WhiteNoiseTypography.h1,
            )

            // Controls for modifying the tens place of the minutes
            TimeUnitPicker(
                value = pickerState.minutesTens,
                digitPositions = 1,
                onIncrement = { onChange(10) },
                onDecrement = { onChange(-10) },
                modifier = Modifier.padding(end = 4.dp),
            )

            // Controls for modifying the units place of the minutes
            TimeUnitPicker(
                value = pickerState.minutes,
                digitPositions = 1,
                onIncrement = { onChange(1) },
                onDecrement = { onChange(-1) },
            )
        }

        // "Set" button confirms the timer configuration
        Button(
            onClick = { onSet() },
            modifier = Modifier.padding(top = 8.dp).fillMaxWidth(0.5F),
        ) {
            Text(text = stringResource(id = R.string.time_set))
        }

        // "Cancel" button reverts or hides the timer configuration
        Button(
            onClick = { onCancel() },
            modifier = Modifier.fillMaxWidth(0.5F),
            colors = ButtonDefaults.outlinedButtonColors()
        ) {
            Text(text = stringResource(id = R.string.time_cancel))
        }
    }
}

@Composable
/**
 * A reusable component that handles the UI for incrementing or decrementing a single time unit
 * (e.g., hours, tens of minutes, or single minutes).
 *
 * @param value The numeric value to display (formatted with leading zeroes).
 * @param digitPositions Number of digits to display (e.g., 1 or 2).
 * @param onIncrement Called when the "+" button is pressed.
 * @param onDecrement Called when the "−" button is pressed.
 */
private fun TimeUnitPicker(
    value: Int,
    digitPositions: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Button(
            onClick = { onIncrement() },
            modifier = Modifier.width(48.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increment")
        }

        Text(
            text = String.format("%1$" + digitPositions + "s", value.toString()).replace(' ', '0'),
            style = WhiteNoiseTypography.h1,
        )

        Button(
            onClick = { onDecrement() },
            modifier = Modifier.width(48.dp),
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrement")
        }
    }
}

@Preview
@Composable
/**
 * Preview function for the individual TimeUnitPicker component, useful for UI development.
 */
private fun TimeUnitPickerPreview() {
    TimeUnitPicker(0, 1, {}, {})
}
