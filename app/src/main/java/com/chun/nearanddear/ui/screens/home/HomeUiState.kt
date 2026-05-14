package com.chun.nearanddear.ui.screens.home

import com.chun.nearanddear.domain.model.FriendModel
import com.chun.nearanddear.domain.model.FriendRequestItem
import com.chun.nearanddear.domain.model.UserLocation
import com.chun.nearanddear.domain.model.User

data class HomeUiState(
    val currentUser: User? = null,
    val location: UserLocation? = null,

    val isServiceRunning: Boolean = false,
    val friendList: List<FriendModel>? = emptyList(),

    val searchResults: List<User>? = null,
    val isSearching: Boolean = false,

    /** Incoming `PENDING` rows where the current user is `friend_id` (needs confirm). */
    val incomingFriendRequests: List<FriendRequestItem> = emptyList(),
    /** Outgoing `PENDING` rows where the current user is `user_id`. */
    val outgoingFriendRequests: List<FriendRequestItem> = emptyList(),
    val isFriendDataLoading: Boolean = false,
    val friendRequestsError: String? = null,

    val tabs: List<String> = emptyList(),
    val selectedTab: Int = 0
)
