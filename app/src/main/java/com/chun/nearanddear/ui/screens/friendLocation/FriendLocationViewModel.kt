package com.chun.nearanddear.ui.screens.friendLocation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chun.nearanddear.data.remote.supabase.SupabaseUserDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendLocationViewModel @Inject constructor(
    private val supabaseUserDataSource: SupabaseUserDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendLocationUiState())
    val uiState: StateFlow<FriendLocationUiState> = _uiState.asStateFlow()

    fun loadFriendLocation(userId: String) {
        if (userId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Missing friend ID") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val friendResult = supabaseUserDataSource.getUserById(userId)
            val locationResult = supabaseUserDataSource.getUserLocation(userId)

            val friend = friendResult.getOrNull()
            val location = locationResult.getOrNull()
            val error = friendResult.exceptionOrNull()?.message
                ?: locationResult.exceptionOrNull()?.message

            _uiState.update {
                it.copy(
                    friend = friend,
                    location = location,
                    isLoading = false,
                    errorMessage = error
                )
            }
        }
    }
}
