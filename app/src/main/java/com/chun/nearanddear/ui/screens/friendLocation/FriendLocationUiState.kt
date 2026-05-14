package com.chun.nearanddear.ui.screens.friendLocation

import com.chun.nearanddear.domain.model.User
import com.chun.nearanddear.domain.model.UserLocation

data class FriendLocationUiState(
    val friend: User? = null,
    val location: UserLocation? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
