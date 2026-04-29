package com.noise.playerscreen.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.noise.R
import com.noise.shared.WhiteNoiseTypography

/**
 * Represents the possible visual and logical states of the timer toggle.
 * It is sealed to ensure type safety when rendering conditionally based on the timer status.
 *
 * @property icon The icon that will be displayed next to the toggle.
 */
sealed class TimerToggleState(
    open val icon: ImageVector
) {
    /**
     * Represents a state where no timer is currently set.
     * A "+" icon is displayed to prompt the user to add a timer.
     */
    object Disabled : TimerToggleState(icon = Icons.Default.Add)

    /**
     * Represents an active timer state, showing the remaining or preset time.
     * A delete icon is shown to allow removal of the current timer.
     *
     * @property displayedTime A user-friendly string showing the set time.
     */
    data class Saved(
        val displayedTime: String,
    ) : TimerToggleState(icon = Icons.Default.Delete)
}

@Composable
        /**
         * Displays a timer toggle UI component. Depending on the state, this will show
         * either a button to set a new timer or to delete an existing one.
         *
         * @param timeState The current state of the timer (disabled or saved).
         * @param onToggle Callback triggered when the user presses the toggle button.
         */
fun TimerToggle(
    timeState: TimerToggleState,
    onToggle: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Timer label (e.g., "Sleep Timer")
            Text(
                text = stringResource(id = R.string.timer_label),
                style = WhiteNoiseTypography.h6,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // If the timer is set, display the remaining time text
                if (timeState is TimerToggleState.Saved) {
                    Text(
                        text = timeState.displayedTime,
                        style = WhiteNoiseTypography.h6,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                // Show the icon button, either add or delete
                Button(onClick = { onToggle() }) {
                    Icon(timeState.icon, contentDescription = null)
                }
            }
        }
    }
}

@Composable
@Preview
/**
 * Provides a preview of the timer toggle component in its default "disabled" state.
 */
private fun TimerSetterPreview_Disabled() {
    TimerToggle(
        TimerToggleState.Disabled,
        onToggle = {},
    )
}

@Composable
@Preview
/**
 * Provides a preview of the timer toggle component when a time has been set.
 * Displays a simulated formatted time value.
 */
private fun TimerSetterPreview_Saved() {
    TimerToggle(
        TimerToggleState.Saved("1:23:45"),
        onToggle = {},
    )
}
