package com.noise.shared

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

// ---------- USER DATA STRUCTURE & DAO ----------

/**
 * Entity representing a user in the local Room database.
 * Stores a unique ID, username, and a hashed password.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val passwordHash: String
)

/**
 * Data Access Object for user-related database operations.
 */
@Dao
interface UserDao {

    /**
     * Inserts a new user. If the user already exists (by ID), it replaces them.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    /**
     * Fetch a user by their username from the database.
     */
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?
}

// ---------- USAGE DATA STRUCTURE & DAO ----------

/**
 * Entity representing usage statistics, storing total playback time.
 */
@Entity(tableName = "usage_stats")
data class UsageStat(
    @PrimaryKey val id: Int = 0, // Only one record assumed; extendable for multi-user support
    val totalPlayTimeMillis: Long
)

/**
 * Data Access Object for app usage statistics.
 */
@Dao
interface UsageDao {

    /**
     * Retrieves the usage stats from the database (by default, ID 0).
     */
    @Query("SELECT * FROM usage_stats WHERE id = 0")
    suspend fun getUsage(): UsageStat?

    /**
     * Stores or updates the current usage statistics.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsage(stat: UsageStat)
}

// ---------- MAIN DATABASE CONFIGURATION ----------

/**
 * Core Room database setup that includes tables for users, usage stats, and noise session data.
 */
@Database(
    entities = [User::class, UsageStat::class, NoiseUsage::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun usageDao(): UsageDao
    abstract fun noiseUsageDao(): NoiseUsageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Initializes and retrieves the singleton instance of the database.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "white_noise_database"
                )
                    .fallbackToDestructiveMigration() // Clears data if schema changes
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}

// ---------- PASSWORD SECURITY ----------

/**
 * Uses SHA-256 hashing algorithm to securely hash plain-text passwords.
 */
fun hashPasswordSHA256(password: String): String {
    val bytes = password.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
}

// ---------- USER REGISTRATION HANDLER ----------

/**
 * Registers a new user by checking if they exist and then storing the hashed password.
 */
suspend fun registerUser(context: Context, username: String, password: String): Boolean {
    return withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(context)
        val userDao = db.userDao()
        val existingUser = userDao.getUserByUsername(username)
        if (existingUser == null) {
            val hashedPassword = hashPasswordSHA256(password)
            userDao.insertUser(User(username = username, passwordHash = hashedPassword))
            true
        } else {
            false
        }
    }
}

// ---------- USER LOGIN HANDLER ----------

/**
 * Validates a login attempt by comparing hashed input to the stored hash.
 */
suspend fun loginUser(context: Context, username: String, password: String): Boolean {
    return withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(context)
        val userDao = db.userDao()
        val user = userDao.getUserByUsername(username)
        user?.passwordHash == hashPasswordSHA256(password)
    }
}
