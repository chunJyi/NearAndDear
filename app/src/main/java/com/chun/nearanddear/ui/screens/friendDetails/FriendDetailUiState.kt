package com.chun.nearanddear.ui.screens.friendDetails

import com.chun.nearanddear.domain.model.User

data class FriendDetailUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val friendRequestSent: Boolean = false
)
