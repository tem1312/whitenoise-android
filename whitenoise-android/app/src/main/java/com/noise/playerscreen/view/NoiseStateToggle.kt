package com.noise.playerscreen.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.noise.shared.WhiteNoiseTypography

/**
 * A reusable Composable that displays a label with an adjacent toggle switch.
 * This is used to enable or disable sound settings such as "Fade" or "Waves".
 *
 * @param text The label shown next to the switch.
 * @param checked Boolean value determining the switch state.
 * @param onCheckedChange Callback to update state when the switch is toggled.
 */
@Composable
fun NoiseStateToggle(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    // Arranges the label and switch horizontally in a row
    Row(
        verticalAlignment = Alignment.CenterVertically,           // Aligns items along the center vertically
        modifier = Modifier.fillMaxWidth(),                       // Ensures the row stretches full width
        horizontalArrangement = Arrangement.SpaceBetween,         // Positions label and switch at opposite ends
    ) {
        // Displays the provided text using the app's heading style
        Text(
            text = text,
            style = WhiteNoiseTypography.h6,
        )
        // Switch component that reflects and modifies the toggle state
        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange(it) }
        )
    }
}

/**
 * Preview function for Compose UI tooling.
 * Shows what the NoiseStateToggle would look like with default values.
 */
@Preview
@Composable
private fun NoiseStateTogglePreview() {
    NoiseStateToggle(text = "Fade", checked = false) {}
}
