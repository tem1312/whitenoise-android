package com.noise.playerscreen.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.noise.R
import com.noise.shared.WhiteNoiseTypography

/**
 * A composable that displays a horizontal slider for adjusting volume levels,
 * accompanied by a descriptive label. This is typically used to give users control
 * over the playback volume within the player interface.
 *
 * @param value The current volume level as a float between 0.0 and 1.0.
 * @param onValueChange A lambda function that will be triggered with the updated volume.
 */
@Composable
fun VolumeControl(
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Label that describes what the slider controls
        Text(
            text = stringResource(id = R.string.volume_label),
            modifier = Modifier.padding(end = 8.dp),
            style = WhiteNoiseTypography.h6,
        )

        // Slider component to adjust the playback volume
        Slider(
            value = value,
            onValueChange = { onValueChange(it) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * A preview function to visualize how the VolumeControl UI will render at design time.
 * This sets the slider at 50% volume for illustrative purposes.
 */
@Preview
@Composable
private fun VolumeControlPreview() {
    VolumeControl(value = 0.5f) {}
}
