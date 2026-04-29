package com.noise.playerscreen.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.noise.shared.NoiseType
import com.noise.shared.defaultNoiseTypes

/**
 * A Composable UI component that allows users to select their desired type of ambient noise
 * (e.g., white noise, waves, rain, etc.) from a dropdown menu.
 *
 * @param state The currently selected NoiseType.
 * @param onChange Callback triggered when a new NoiseType is selected.
 */
@Composable
fun noiseSelector(
    state: NoiseType,
    onChange: (NoiseType) -> Unit,
) {
    // Tracks whether the dropdown menu is visible or collapsed
    var expanded by remember { mutableStateOf(false) }

    // Vertically arranges the title and selector field
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Display label for the selection area
        Text(text = "Noise Type", style = MaterialTheme.typography.subtitle1)

        // Wrapping box to align the dropdown UI
        Box(modifier = Modifier.fillMaxWidth()) {
            // TextField showing the currently selected noise type (disabled for editing)
            OutlinedTextField(
                value = stringResource(id = state.label),
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }, // Opens dropdown on click
                enabled = false, // Prevents manual input
                readOnly = true, // Display-only field
                trailingIcon = {
                    // Arrow icon toggles dropdown visibility
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                },
            )

            // Actual dropdown menu with selectable items
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }, // Close menu on outside click
                modifier = Modifier.fillMaxWidth()
            ) {
                // Loop through available noise types and create a menu item for each
                defaultNoiseTypes.forEach { type ->
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            onChange(type) // Notify parent of the selected type
                        }
                    ) {
                        Text(text = stringResource(id = type.label))
                    }
                }
            }
        }
    }
}
