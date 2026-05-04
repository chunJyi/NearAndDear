package com.chun.nearanddear.ui.screens.home

import com.chun.nearanddear.domain.model.Location
import com.chun.nearanddear.domain.model.User

data class HomeUiState(
    val currentUser: User? = null,
    val location: Location? = null,
    val isServiceRunning: Boolean = false
)
