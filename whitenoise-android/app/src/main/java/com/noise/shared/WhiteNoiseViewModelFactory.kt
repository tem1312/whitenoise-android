package com.noise.shared

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.noise.playerscreen.viewmodel.PlayerScreenViewModel

// Factory class used to create instances of ViewModels with access to saved state and custom parameters
class WhiteNoiseViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val userPreferences: UserPreferences,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    // This method is responsible for instantiating the desired ViewModel class, injecting dependencies if needed
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        @Suppress("UNCHECKED_CAST")
        return PlayerScreenViewModel(userPreferences) as T
    }
}
