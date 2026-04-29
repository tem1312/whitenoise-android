package com.noise.shared

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable

        /**
         * Composable screen that displays the total and session-based usage history
         * of the app's noise feature. Pulls data from the local Room database and formats it.
         */
fun UsageSummaryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var totalMillis by remember { mutableStateOf(0L) }
    var sessions by remember { mutableStateOf(emptyList<NoiseUsage>()) }

    // Load usage statistics when the screen is first launched
    LaunchedEffect(Unit) {
        scope.launch {
            val dao = AppDatabase.getDatabase(context).noiseUsageDao()
            totalMillis = dao.getTotalUsageMillis() ?: 0
            sessions = dao.getAllUsage()
        }
    }

    // Convert total usage time into hours, minutes, and seconds
    val hours = TimeUnit.MILLISECONDS.toHours(totalMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(totalMillis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(totalMillis) % 60

    // Display UI elements: toolbar, usage stats, and session breakdown
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Usage Statistics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Navigate Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Show total accumulated usage time
            Text("Total Usage Time:", style = MaterialTheme.typography.titleMedium)
            Text("$hours h $minutes m $seconds s\n", style = MaterialTheme.typography.bodyLarge)

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // List each individual session
            Text("Sessions:", style = MaterialTheme.typography.titleSmall)
            LazyColumn {
                items(sessions) { session ->
                    val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        .format(Date(session.timestamp))

                    val sessionHours = TimeUnit.MILLISECONDS.toHours(session.durationMillis)
                    val sessionMinutes = TimeUnit.MILLISECONDS.toMinutes(session.durationMillis) % 60
                    val sessionSeconds = TimeUnit.MILLISECONDS.toSeconds(session.durationMillis) % 60

                    Text("• $formattedDate – $sessionHours h $sessionMinutes m $sessionSeconds s",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
