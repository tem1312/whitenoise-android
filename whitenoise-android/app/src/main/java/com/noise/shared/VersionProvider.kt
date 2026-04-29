package com.noise.shared

import android.content.Context

/**
 * Utility class responsible for retrieving the app's current version string.
 */
class VersionProvider(
    private val context: Context
) {

    /**
     * Returns the version name from the app's package info.
     */
    fun getVersion(): String =
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
}
