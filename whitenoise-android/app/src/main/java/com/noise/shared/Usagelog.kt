package com.noise.data

import androidx.room.*

/**
 * Represents a single usage session with start and end timestamps.
 * Each record shows how long the app was actively used.
 */
@Entity(tableName = "usage_log")
data class UsageLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long,
    val endTime: Long
)

@Dao
interface UsageLogDao {

    /**
     * Inserts a new session record into the usage log table.
     * Each entry represents a time span during which the app was used.
     */
    @Insert
    suspend fun insertLog(log: UsageLog)

    /**
     * Retrieves all session records from the log.
     * Useful for displaying a full history of app usage.
     */
    @Query("SELECT * FROM usage_log")
    suspend fun getAllLogs(): List<UsageLog>

    /**
     * Calculates the total duration of app usage by summing
     * the difference between end and start times for all sessions.
     */
    @Query("SELECT SUM(endTime - startTime) FROM usage_log")
    suspend fun getTotalUsageTime(): Long
}
