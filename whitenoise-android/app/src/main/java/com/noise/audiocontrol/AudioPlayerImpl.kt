package com.noise.audiocontrol

import android.content.Context
import androidx.annotation.RawRes
import com.google.android.exoplayer2.C.CONTENT_TYPE_MUSIC
import com.google.android.exoplayer2.C.USAGE_MEDIA
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.Util
import com.noise.shared.UsageTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Interface defining the core functionality of an audio player used in the app.
 */
interface AudioPlayer {

    /**
     * Sets a new sound file from the raw resources to be played by the audio player.
     * @param resource the raw resource ID of the audio file.
     */
    fun setFile(@RawRes resource: Int)

    /**
     * Begins playback of the currently loaded sound file.
     */
    fun play()

    /**
     * Pauses the currently playing audio and tracks usage data.
     */
    fun pause()

    /**
     * Checks whether audio is actively playing.
     * @return true if playing, false otherwise.
     */
    fun isPlaying(): Boolean

    /**
     * Adjusts the current playback volume.
     * @param volume Float value between 0.0 and 1.0 representing desired volume level.
     */
    fun setVolume(volume: Float)
}

/**
 * Implementation of the AudioPlayer interface using ExoPlayer.
 * Handles loading and playing audio from raw resources, as well as
 * volume control and session tracking.
 */
class AudioPlayerImpl(private val context: Context) : AudioPlayer {

    // Main media playback engine based on ExoPlayer
    private val player: SimpleExoPlayer = SimpleExoPlayer.Builder(context).build()

    // Keeps track of the most recently loaded audio file to avoid reloading the same one
    @RawRes
    private var lastFile: Int = 0

    // Used to track if a listening session is currently in progress
    private var isSessionActive = false

    /**
     * Loads a new audio file from raw resources if it hasn't already been set.
     * Prevents redundant loading and prepares the media source.
     */
    override fun setFile(@RawRes resource: Int) {
        if (lastFile == resource) return  // Skip reload if it's the same file
        lastFile = resource

        val rawDataSource = RawResourceDataSource(context)
        rawDataSource.open(DataSpec(RawResourceDataSource.buildRawResourceUri(resource)))

        val mediaSource = Util.getUserAgent(context, context.packageName).let { userAgent ->
            ProgressiveMediaSource.Factory(DefaultDataSourceFactory(context, userAgent))
                .createMediaSource(rawDataSource.uri)
        }

        // Define how the system should treat this audio (e.g., media/music)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(CONTENT_TYPE_MUSIC)
            .setUsage(USAGE_MEDIA)
            .build()

        player.audioAttributes = audioAttributes
        player.repeatMode = Player.REPEAT_MODE_ALL  // Enable looping
        player.prepare(mediaSource) // Ready the player for playback
    }

    /**
     * Starts playback and records session start if not already active.
     */
    override fun play() {
        if (!isSessionActive) {
            UsageTracker.startSession()  // Begin tracking usage
            isSessionActive = true
        }
        player.playWhenReady = true
    }

    /**
     * Stops audio playback and triggers session tracking completion.
     */
    override fun pause() {
        if (isSessionActive) {
            CoroutineScope(Dispatchers.IO).launch {
                UsageTracker.endSession(context)  // End the current session and persist data
            }
            isSessionActive = false
        }
        player.playWhenReady = false
    }

    /**
     * Returns whether the player is actively playing audio.
     */
    override fun isPlaying(): Boolean = player.playWhenReady

    /**
     * Updates the current playback volume to the specified level.
     */
    override fun setVolume(volume: Float) {
        player.volume = volume
    }
}
