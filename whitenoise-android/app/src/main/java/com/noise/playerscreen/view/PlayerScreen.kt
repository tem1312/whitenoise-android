@file:JvmName("PlayerKt") // Ensures a unique JVM class name to avoid build conflicts

package com.noise.playerscreen.view

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.noise.playerscreen.model.PlayerScreenState
import com.noise.playerscreen.viewmodel.PlayerScreenViewModel

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
        /**
         * Main screen of the app that hosts the player UI and optional timer picker in a modal sheet.
         *
         * @param viewModel The ViewModel that manages the app’s logic and LiveData state.
         * @param onSettingsClicked Callback triggered when the settings icon is pressed.
         */
fun PlayerScreen(
    viewModel: PlayerScreenViewModel,
    onSettingsClicked: () -> Unit,
) {
    // Observing player screen UI state from ViewModel
    val state = viewModel.playerScreenState.observeAsState(initial = PlayerScreenState.default)

    // Bottom sheet that appears when setting a timer
    ModalBottomSheetLayout(
        sheetContent = {
            TimerPicker(
                pickerState = state.value.timerPickerState,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
                onChange = { viewModel.updateTimer(it) },
                onSet = { viewModel.setTimer() },
                onCancel = { viewModel.cancelTimer() },
            )
        },
        showSheet = state.value.showTimerPicker,
        onSheetDismissed = { viewModel.cancelTimer() }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    state.value,
                    onPlayToggled = { viewModel.togglePlay(it) },
                    onSettingsClicked = { onSettingsClicked() }
                )
            },
        ) {
            // Main body of the screen with controls for audio settings
            Column(modifier = Modifier.padding(16.dp)) {
                Player(
                    state = state.value,
                    noiseTypeChanged = { viewModel.changeNoiseType(it) },
                    fadeChanged = { viewModel.toggleFade(it) },
                    wavesChanged = { viewModel.toggleWaves(it) },
                    volumeChanged = { viewModel.changeVolume(it) },
                    onTimerToggled = { viewModel.toggleTimer() },
                )
            }
        }
    }
}

@Composable
@ExperimentalMaterialApi
        /**
         * Wrapper composable around Jetpack Compose's ModalBottomSheetLayout with custom visibility handling.
         *
         * @param sheetContent The Composable content displayed inside the sheet.
         * @param showSheet Boolean flag to control sheet visibility.
         * @param onSheetDismissed Callback triggered when the sheet is dismissed.
         * @param sheetShape, sheetElevation, sheetBackgroundColor, etc. Optional styling for the sheet.
         * @param content Main body UI shown behind the bottom sheet.
         */
fun ModalBottomSheetLayout(
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    showSheet: Boolean = false,
    onSheetDismissed: () -> Unit = {},
    sheetShape: Shape = MaterialTheme.shapes.large,
    sheetElevation: Dp = ModalBottomSheetDefaults.Elevation,
    sheetBackgroundColor: Color = MaterialTheme.colors.surface,
    sheetContentColor: Color = contentColorFor(sheetBackgroundColor),
    scrimColor: Color = ModalBottomSheetDefaults.scrimColor,
    content: @Composable () -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    // Automatically dismisses sheet when no longer visible
    LaunchedEffect(modalBottomSheetState.currentValue) {
        if (!modalBottomSheetState.isVisible && showSheet) {
            onSheetDismissed()
        }
    }

    // Show/hide logic for the sheet based on external state
    LaunchedEffect(showSheet) {
        if (showSheet) {
            modalBottomSheetState.show()
        } else {
            modalBottomSheetState.hide()
        }
    }

    // Render modal bottom sheet with customized styling and content
    ModalBottomSheetLayout(
        sheetContent = sheetContent,
        modifier = modifier,
        sheetState = modalBottomSheetState,
        sheetShape = sheetShape,
        sheetElevation = sheetElevation,
        sheetBackgroundColor = sheetBackgroundColor,
        sheetContentColor = sheetContentColor,
        scrimColor = scrimColor,
        content = content
    )
}

@Composable
/**
 * App bar displayed at the top of the screen with play/pause toggle and settings button.
 *
 * @param state Current player state to determine play/pause icon.
 * @param onPlayToggled Callback for play/pause button.
 * @param onSettingsClicked Callback for navigating to settings.
 */
private fun TopAppBar(
    state: PlayerScreenState,
    onPlayToggled: (Boolean) -> Unit,
    onSettingsClicked: () -> Unit,
) {
    TopAppBar(
        title = { Text("Waves") },
        actions = {
            // Toggle between Play and Pause icons based on current playback state
            IconButton(onClick = { onPlayToggled(!state.playing) }) {
                val icon = if (state.playing) Icons.Default.Pause else Icons.Default.PlayArrow
                Icon(icon, contentDescription = null)
            }
            // Open settings screen when this icon is pressed
            IconButton(onClick = { onSettingsClicked() }) {
                Icon(Icons.Default.Settings, contentDescription = null)
            }
        }
    )
}
