package com.chun.nearanddear.ui.screens.friendDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chun.nearanddear.data.remote.supabase.SupabaseUserDataSource
import com.chun.nearanddear.data.session.SessionDataStore
import com.chun.nearanddear.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendDetailViewModel @Inject constructor(
    private val supabaseUserDataSource: SupabaseUserDataSource,
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendDetailUiState())
    val uiState: StateFlow<FriendDetailUiState> = _uiState

    fun loadUserDetails(userId: String) {
        if (userId.isBlank()) {
            _uiState.value = FriendDetailUiState(error = "Invalid user ID")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val result = supabaseUserDataSource.getUserById(userId)
                result.onSuccess { user ->
                    // Check if there's already a friend request or friendship
                    val currentUser = sessionDataStore.currentUser.value
                    if (currentUser != null && currentUser.id != userId) {
                        val statusResult = supabaseUserDataSource.checkFriendRequestStatus(currentUser.id, userId)
                        statusResult.onSuccess { status ->
                            val friendRequestSent = status == "PENDING" || status == "FRIEND"
                            _uiState.value = FriendDetailUiState(
                                user = user,
                                friendRequestSent = friendRequestSent
                            )
                        }.onFailure {
                            // If we can't check status, just show the user details
                            _uiState.value = FriendDetailUiState(user = user)
                        }
                    } else {
                        _uiState.value = FriendDetailUiState(user = user)
                    }
                }.onFailure { exception ->
                    _uiState.value = FriendDetailUiState(
                        error = "Failed to load user details: ${exception.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = FriendDetailUiState(
                    error = "Failed to load user details: ${e.message}"
                )
            }
        }
    }

    fun sendFriendRequest(friendId: String) {
        val currentUser = sessionDataStore.currentUser.value
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(error = "You must be logged in to send friend requests")
            return
        }

        if (currentUser.id == friendId) {
            _uiState.value = _uiState.value.copy(error = "You cannot send a friend request to yourself")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val result = supabaseUserDataSource.sendFriendRequest(currentUser.id, friendId)
                result.onSuccess {
                    // Update UI state to show success
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null,
                        successMessage = "Friend request sent successfully!",
                        friendRequestSent = true
                    )
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to send friend request: ${exception.message}",
                        successMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to send friend request: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
