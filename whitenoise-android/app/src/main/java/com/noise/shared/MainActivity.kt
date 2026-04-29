// MainActivity integrating login logic, navigation, and audio service binding
package com.noise.shared

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.alorma.compose.settings.storage.preferences.rememberPreferenceIntSettingState
import com.noise.playerscreen.view.PlayerScreen
import com.noise.playerscreen.viewmodel.PlayerScreenViewModel
import com.noise.service.AudioPlayerService
import com.noise.settings.view.SettingsScreen
import com.noise.shared.UsageSummaryScreen

/**
 * Main launcher activity responsible for setting up UI, navigation, and binding to the audio service.
 */
class MainActivity : AppCompatActivity() {

    // User preference interface used throughout the app
    private lateinit var userPreferences: UserPreferences

    // Provides application version info
    private val versionProvider = VersionProvider(this)

    // ViewModel scoped to this activity and initialized with user preferences
    private val playerViewModel by viewModels<PlayerScreenViewModel> {
        WhiteNoiseViewModelFactory(
            this,
            userPreferences,
            intent.extras,
        )
    }

    // Service that handles background audio playback
    private var service: AudioPlayerService? = null

    /**
     * Establishes the connection between the app and the background service.
     * Binds the ViewModel to the AudioController once service is connected.
     */
    private val playerConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            val audioPlayerBinder = binder as AudioPlayerService.AudioPlayerBinder
            service = audioPlayerBinder.service
            playerViewModel.bindAudioController(audioPlayerBinder.service.audioController)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            playerViewModel.clearAudioController()
        }
    }

    /**
     * Called once when the activity is first created.
     * Starts and binds the audio service, and initializes the UI with login and navigation logic.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launch and bind to the audio playback service
        val serviceIntent = Intent(this, AudioPlayerService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, playerConnection, BIND_AUTO_CREATE)

        // Load and migrate stored settings if needed
        userPreferences = UserPreferencesImpl(PreferenceManager.getDefaultSharedPreferences(this))
        userPreferences.migrateLegacyPreferences()

        setContent {
            var isLoggedIn by remember { mutableStateOf(false) }

            // Load theme preference from shared storage
            val darkState = rememberPreferenceIntSettingState(
                key = PREF_USE_DARK_MODE_KEY,
                defaultValue = DarkModeSetting.AUTO.key,
            )

            val navController = rememberNavController()

            // Apply app-wide theming based on preference
            WhiteNoiseTheme(darkTheme = darkState.isDarkMode()) {
                if (!isLoggedIn) {
                    AppEntryPoint(onLoginSuccess = { isLoggedIn = true })
                } else {
                    NavHost(
                        navController = navController,
                        startDestination = NavigationDestination.PLAYER.key,
                        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right) },
                        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) },
                    ) {
                        composable(NavigationDestination.PLAYER.key) {
                            PlayerScreen(playerViewModel) {
                                navController.navigate(NavigationDestination.SETTINGS.key)
                            }
                        }

                        composable(NavigationDestination.SETTINGS.key) {
                            SettingsScreen(
                                version = versionProvider.getVersion(),
                                darkThemeState = darkState,
                                navController = navController,
                                onBackPressed = { navController.popBackStack() }
                            )
                        }

                        composable("usage_stats") {
                            UsageSummaryScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Triggered when the app is moved to the background.
     * Delegates control of the audio system to the service for notification handling.
     */
    override fun onPause() {
        super.onPause()
        service?.handoffControl()
    }

    /**
     * Called when the activity becomes visible.
     * Ensures UI is in sync with the playback service.
     */
    override fun onStart() {
        super.onStart()
        service?.run {
            dismissNotification()
            playerViewModel.bindAudioController(audioController)
        }
    }

    /**
     * Cleanup logic for when the activity is destroyed.
     * Unbinds from the service to avoid memory leaks.
     */
    override fun onDestroy() {
        super.onDestroy()
        service = null
        unbindService(playerConnection)
    }
}
