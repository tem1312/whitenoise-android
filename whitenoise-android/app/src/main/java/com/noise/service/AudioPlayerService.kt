package com.noise.service

import android.app.*
import android.os.PowerManager.WakeLock
import android.content.Intent
import android.media.AudioManager
import android.graphics.BitmapFactory
import android.os.*
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat as MediaNotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.preference.PreferenceManager
import com.noise.AudioPlayerButton
import com.noise.AudioPlayerViewModel
import com.noise.R
import com.noise.audiocontrol.AudioController
import com.noise.audiocontrol.AudioFocusManagerImpl
import com.noise.audiocontrol.AudioPlayerImpl
import com.noise.service.model.AudioPlayerScreenState
import com.noise.shared.MainActivity
import com.noise.shared.UserPreferencesImpl

/**
 * Foreground service that handles audio playback in the background.
 * Maintains system audio focus, handles notification control, and ensures
 * audio persists even when the app is not in the foreground.
 */
class AudioPlayerService : LifecycleService() {
    private val binder: IBinder = AudioPlayerBinder()
    lateinit var audioController: AudioController
    private var wakeLock: WakeLock? = null
    private lateinit var viewModel: AudioPlayerViewModel

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize audio controller with playback, focus handling, and preferences
        audioController = AudioController(
            AudioPlayerImpl(this),
            AudioFocusManagerImpl(getSystemService(AUDIO_SERVICE) as AudioManager),
            mainLooper,
            UserPreferencesImpl(PreferenceManager.getDefaultSharedPreferences(baseContext)),
        )

        // Initialize ViewModel for audio player screen state
        viewModel = AudioPlayerViewModel(audioController)

        // Observe and react to state changes to control notification visibility
        viewModel.stateLiveData.observe(this) {
            when (it) {
                is AudioPlayerScreenState.Shown -> showNotification(it)
                else -> dismissNotification()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioController.pause() // Ensure audio stops when service is terminated
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        // Acquire partial wake lock to keep CPU running during playback
        val pm = getSystemService(POWER_SERVICE) as? PowerManager
        wakeLock = pm?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "$WAKE_LOCK_TAG:AudioPlayerService"
        )

        // If a control action was triggered via notification
        safeValueOf<NotificationAction>(intent?.extras?.getString(DO_ACTION).orEmpty())?.let {
            viewModel.handleNotificationAction(it)
        }

        // Ensure notification channel exists
        createNotificationChannel()
        return START_STICKY
    }

    /**
     * Removes foreground status and cancels the current notification.
     */
    fun dismissNotification() {
        stopForeground(true)
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    /**
     * Triggers notification state via ViewModel to allow external control.
     */
    fun handoffControl() {
        viewModel.enableNotification()
    }

    /**
     * Builds and displays a media-style notification with playback buttons.
     * Keeps the service in the foreground to prevent the system from killing it.
     */
    private fun showNotification(screenState: AudioPlayerScreenState.Shown) {
        val icon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        val mediaSession = MediaSessionCompat(applicationContext, mediaSessionTag)

        val openAppIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("startedFromNotification", true)
        }

        val openAppPendingIntent = PendingIntent.getActivity(
            applicationContext,
            screenState.titleResource,
            openAppIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, notificationChannel)
            .setSmallIcon(R.drawable.ic_statusbar2)
            .setContentTitle(getString(screenState.titleResource))
            .setContentText(getString(screenState.subtitleResource))
            .setLargeIcon(icon)
            .setStyle(MediaNotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setOngoing(true)
            .addAudioPlayerButton(screenState.firstButton)
            .addAudioPlayerButton(screenState.secondButton)
            .setContentIntent(openAppPendingIntent)
            .setPriority(Notification.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        wakeLock?.acquire()
    }

    /**
     * Helper function to attach audio action buttons to the notification.
     */
    private fun NotificationCompat.Builder.addAudioPlayerButton(
        button: AudioPlayerButton
    ): NotificationCompat.Builder {
        val actionIntent = Intent(this@AudioPlayerService, AudioPlayerService::class.java).apply {
            putExtra(DO_ACTION, button.action.name)
        }

        val pendingIntent = PendingIntent.getService(
            this@AudioPlayerService,
            button.textResource,
            actionIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        return addAction(button.iconResource, getString(button.textResource), pendingIntent)
    }

    /**
     * Creates a notification channel required by Android 8.0+ for showing notifications.
     */
    private fun createNotificationChannel() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.createNotificationChannel(
            NotificationChannel(
                notificationChannel,
                "Noise Playing",
                NotificationManager.IMPORTANCE_LOW
            )
        )
    }

    /**
     * Binder class for external services or activities to interact with this service.
     */
    inner class AudioPlayerBinder : Binder() {
        val service: AudioPlayerService
            get() = this@AudioPlayerService
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val DO_ACTION = "do"
        private const val WAKE_LOCK_TAG = "noise.wakelock"
        private const val notificationChannel = "com.whitenoise.Notifications"
        private const val mediaSessionTag = "media.session"
    }
}

/**
 * Enum defining the possible actions that can be taken from the audio notification.
 */
enum class NotificationAction {
    PAUSE_ACTION,
    CLOSE_ACTION,
    PLAY_ACTION,
}

/**
 * Utility function to safely convert a string into an enum constant, returning null if invalid.
 */
inline fun <reified T : Enum<T>> safeValueOf(type: String): T? {
    return try {
        java.lang.Enum.valueOf(T::class.java, type)
    } catch (e: IllegalArgumentException) {
        null
    }
}
