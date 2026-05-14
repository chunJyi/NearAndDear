package com.chun.nearanddear.ui.screens.home

import com.chun.nearanddear.domain.model.FriendModel
import com.chun.nearanddear.domain.model.UserLocation
import com.chun.nearanddear.domain.model.User

data class HomeUiState(
    val currentUser: User? = null,
    val location: UserLocation? = null,

    val isServiceRunning: Boolean = false,
    val friendList: List<FriendModel>? = emptyList(),

    val tabs: List<String> = emptyList(),
    val selectedTab: Int = 0
)
