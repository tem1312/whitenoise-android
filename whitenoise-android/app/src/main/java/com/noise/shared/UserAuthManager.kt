package com.noise.shared

import android.content.Context
import android.content.SharedPreferences

/**
 * Utility class responsible for managing user authentication using SharedPreferences.
 * Handles basic registration, login, existence check, and clearing of stored user data.
 */
class UserAuthManager(context: Context) {

    // Access to local shared preferences for storing user credentials
    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    /**
     * Registers a new user by saving the username and password into preferences.
     * Returns false if the username already exists.
     */
    fun registerUser(username: String, password: String): Boolean {
        if (prefs.contains(username)) return false // User already exists
        prefs.edit().putString(username, password).apply()
        return true
    }

    /**
     * Verifies whether the provided credentials match those stored.
     * Returns true if they match; false otherwise.
     */
    fun validateUser(username: String, password: String): Boolean {
        return prefs.getString(username, null) == password
    }

    /**
     * Checks whether a user is already registered.
     * Returns true if the username is found in preferences.
     */
    fun userExists(username: String): Boolean {
        return prefs.contains(username)
    }

    /**
     * Clears all saved user credentials from SharedPreferences.
     * This action is irreversible.
     */
    fun clearUsers() {
        prefs.edit().clear().apply()
    }
}
