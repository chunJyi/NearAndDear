package com.chun.nearanddear.ui.screens.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chun.nearanddear.data.remote.supabase.SupabaseUserDataSource
import com.chun.nearanddear.data.session.SessionDataStore
import com.chun.nearanddear.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val supabaseUserDataSource: SupabaseUserDataSource
) : ViewModel() {

    private val searchResultsState = MutableStateFlow<List<User>?>(null)
    private val isSearchingState = MutableStateFlow(false)
    private val isLoadingFriendsState = MutableStateFlow(false)
    private val errorState = MutableStateFlow<String?>(null)

    val uiState: StateFlow<FriendUiState> = combine(
        sessionDataStore.friendModel,
        searchResultsState,
        isSearchingState,
        isLoadingFriendsState,
        errorState
    ) { friendList, searchResults, isSearching, isLoadingFriends, error ->
        FriendUiState(
            friendList = friendList,
            searchResults = searchResults,
            isSearching = isSearching,
            isLoadingFriends = isLoadingFriends,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FriendUiState()
    )

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            searchResultsState.value = null
            isSearchingState.value = false
            errorState.value = null
            return
        }

        viewModelScope.launch {
            isSearchingState.value = true
            errorState.value = null
            try {
                val result = supabaseUserDataSource.searchUsers(query.trim())
                result.onSuccess { users ->
                    searchResultsState.value = users
                }.onFailure { exception ->
                    errorState.value = "Failed to search users: ${exception.message}"
                    searchResultsState.value = emptyList()
                }
            } catch (e: Exception) {
                errorState.value = "Search failed: ${e.message}"
                searchResultsState.value = emptyList()
            } finally {
                isSearchingState.value = false
            }
        }
    }

    fun clearSearchResults() {
        searchResultsState.value = null
        isSearchingState.value = false
        errorState.value = null
    }

    fun loadFriends() {
        viewModelScope.launch {
            isLoadingFriendsState.value = true
            errorState.value = null
            try {
                // Friends are loaded automatically through SessionDataStore
                // This method can be extended for manual refresh if needed
            } catch (e: Exception) {
                errorState.value = "Failed to load friends: ${e.message}"
            } finally {
                isLoadingFriendsState.value = false
            }
        }
    }

    fun clearError() {
        errorState.value = null
    }
}
