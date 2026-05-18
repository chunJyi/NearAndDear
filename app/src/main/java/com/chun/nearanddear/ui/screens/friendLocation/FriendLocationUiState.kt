package com.chun.nearanddear.ui.screens.friendLocation

import com.chun.nearanddear.domain.model.User
import com.chun.nearanddear.domain.model.UserLocation
import com.google.android.gms.maps.model.LatLng

data class FriendLocationUiState(
    val friend: User? = null,
    val location: UserLocation? = null,
    val trackPoints: List<LatLng> = emptyList(),
    val isLoading: Boolean = false,
    val isLive: Boolean = false,
    val errorMessage: String? = null
)
