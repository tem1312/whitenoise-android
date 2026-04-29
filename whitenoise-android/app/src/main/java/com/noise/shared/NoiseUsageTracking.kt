package com.noise.shared

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// -------------------------
// Database entity for tracking individual usage sessions
// -------------------------
@Entity(tableName = "noise_usage")
data class NoiseUsage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,            // When the session began
    val durationMillis: Long        // How long the session lasted in milliseconds
)

// -------------------------
// Data access interface for interacting with noise usage data
// -------------------------
@Dao
interface NoiseUsageDao {

    // Inserts a new usage record into the table
    @Insert
    suspend fun insert(usage: NoiseUsage)

    // Fetches all usage entries ordered from most recent to oldest
    @Query("SELECT * FROM noise_usage ORDER BY timestamp DESC")
    suspend fun getAllUsage(): List<NoiseUsage>

    // Calculates total playtime by summing all durations
    @Query("SELECT SUM(durationMillis) FROM noise_usage")
    suspend fun getTotalUsageMillis(): Long?
}

// -------------------------
// Singleton responsible for tracking app usage sessions
// -------------------------
object UsageTracker {

    private var sessionStartTime: Long? = null

    // Call this to begin tracking a usage session
    fun startSession() {
        sessionStartTime = System.currentTimeMillis()
    }

    // Finalizes and stores the session duration in the database
    suspend fun endSession(context: Context) {
        sessionStartTime?.let { start ->
            val duration = System.currentTimeMillis() - start
            val usage = NoiseUsage(
                timestamp = start,
                durationMillis = duration
            )
            withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(context).noiseUsageDao().insert(usage)
            }
            sessionStartTime = null
        }
    }
}
