package com.chun.nearanddear.ui.screens.friends

import com.chun.nearanddear.domain.model.FriendModel
import com.chun.nearanddear.domain.model.FriendRequestItem
import com.chun.nearanddear.domain.model.User

data class FriendUiState(
    val friendList: List<FriendModel>? = null,
    /** Incoming `PENDING` rows where the current user is `friend_id` (needs confirm). */
    val incomingFriendRequests: List<FriendRequestItem> = emptyList(),
    /** Outgoing `PENDING` rows where the current user is `user_id`. */
    val outgoingFriendRequests: List<FriendRequestItem> = emptyList(),
    val searchResults: List<User>? = null,
    val isSearching: Boolean = false,
    val isLoadingFriends: Boolean = false,
    val error: String? = null
)
