package com.chun.nearanddear.ui.screens.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chun.nearanddear.data.remote.supabase.SupabaseUserDataSource
import com.chun.nearanddear.data.session.SessionDataStore
import com.chun.nearanddear.domain.model.FriendModel
import com.chun.nearanddear.domain.model.FriendRequestItem
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
    private val incomingRequestsState = MutableStateFlow<List<FriendRequestItem>>(emptyList())
    private val outgoingPendingState = MutableStateFlow<List<FriendRequestItem>>(emptyList())

    val uiState: StateFlow<FriendUiState> = combine(
        sessionDataStore.friendModel,
        searchResultsState,
        isSearchingState,
        isLoadingFriendsState,
        errorState,
        incomingRequestsState,
        outgoingPendingState
    ) { values ->
        val friendList = values[0] as List<FriendModel>?
        val searchResults = values[1] as List<User>?
        val isSearching = values[2] as Boolean
        val isLoadingFriends = values[3] as Boolean
        val error = values[4] as String?
        val incoming = values[5] as List<FriendRequestItem>
        val outgoing = values[6] as List<FriendRequestItem>

        FriendUiState(
            friendList = friendList,
            incomingFriendRequests = incoming,
            outgoingFriendRequests = outgoing,
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

    init {
        viewModelScope.launch {
            sessionDataStore.currentUser.collect { user ->
                if (user == null) {
                    incomingRequestsState.value = emptyList()
                    outgoingPendingState.value = emptyList()
                    errorState.value = null
                    sessionDataStore.clearFriend()
                } else {
                    loadFriendData(user.id)
                }
            }
        }
    }

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
        val userId = sessionDataStore.currentUser.value?.id ?: return
        loadFriendData(userId)
    }

    private fun loadFriendData(userId: String) {
        viewModelScope.launch {
            isLoadingFriendsState.value = true
            errorState.value = null
            var combinedError: String? = null

            supabaseUserDataSource.getAcceptedFriends(userId).fold(
                onSuccess = { sessionDataStore.setFriend(it) },
                onFailure = { combinedError = it.message ?: "Could not load friends" }
            )

            val incoming = supabaseUserDataSource.getIncomingPendingFriendRequests(userId).fold(
                onSuccess = { it },
                onFailure = {
                    if (combinedError == null) combinedError = it.message ?: "Could not load requests"
                    emptyList()
                }
            )
            incomingRequestsState.value = incoming

            val outgoing = supabaseUserDataSource.getOutgoingPendingFriendRequests(userId).fold(
                onSuccess = { it },
                onFailure = {
                    if (combinedError == null) combinedError = it.message ?: "Could not load pending"
                    emptyList()
                }
            )
            outgoingPendingState.value = outgoing

            errorState.value = combinedError
            isLoadingFriendsState.value = false
        }
    }

    fun acceptIncomingFriendRequest(relationshipId: String) {
        viewModelScope.launch {
            supabaseUserDataSource.acceptFriendRequest(relationshipId).fold(
                onSuccess = { loadFriends() },
                onFailure = { errorState.value = it.message ?: "Could not accept request" }
            )
        }
    }

    fun declineIncomingFriendRequest(relationshipId: String) {
        viewModelScope.launch {
            supabaseUserDataSource.deleteFriendRelationship(relationshipId).fold(
                onSuccess = { loadFriends() },
                onFailure = { errorState.value = it.message ?: "Could not decline request" }
            )
        }
    }

    fun cancelOutgoingFriendRequest(relationshipId: String) {
        viewModelScope.launch {
            supabaseUserDataSource.deleteFriendRelationship(relationshipId).fold(
                onSuccess = { loadFriends() },
                onFailure = { errorState.value = it.message ?: "Could not cancel request" }
            )
        }
    }

    fun clearError() {
        errorState.value = null
    }
}
