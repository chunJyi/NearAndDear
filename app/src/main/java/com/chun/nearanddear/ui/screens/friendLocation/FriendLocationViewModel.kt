package com.chun.nearanddear.ui.screens.friendLocation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chun.nearanddear.data.remote.supabase.SupabaseUserDataSource
import com.chun.nearanddear.domain.model.UserLocation
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt

@HiltViewModel
class FriendLocationViewModel @Inject constructor(
    private val supabaseUserDataSource: SupabaseUserDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendLocationUiState())
    val uiState: StateFlow<FriendLocationUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    fun startObservingFriendLocation(userId: String) {
        if (userId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Missing friend ID") }
            return
        }

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update {
                FriendLocationUiState(isLoading = true)
            }

            val friend = supabaseUserDataSource.getUserById(userId).getOrElse { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Could not load friend"
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(friend = friend, isLoading = false, errorMessage = null) }

            supabaseUserDataSource.getUserLocation(userId).onSuccess { location ->
                if (location != null) applyLocationUpdate(location)
            }

            launch {
                supabaseUserDataSource.observeUserLocation(userId)
                    .catch { error ->
                        Log.w(TAG, "Realtime location stream failed: ${error.message}", error)
                        _uiState.update { it.copy(isLive = false) }
                    }
                    .collect { location ->
                        _uiState.update { it.copy(isLive = true) }
                        applyLocationUpdate(location)
                    }
            }

            launch {
                while (isActive) {
                    delay(LOCATION_POLL_INTERVAL_MS)
                    supabaseUserDataSource.getUserLocation(userId).onSuccess { location ->
                        if (location != null) applyLocationUpdate(location)
                    }
                }
            }
        }
    }

    fun stopObservingFriendLocation() {
        observeJob?.cancel()
        observeJob = null
        _uiState.update { it.copy(isLive = false) }
    }

    fun retryLoad(userId: String) {
        startObservingFriendLocation(userId)
    }

    private fun applyLocationUpdate(location: UserLocation) {
        if (!location.hasSharedCoordinates) return

        val point = LatLng(location.latitude, location.longitude)
        _uiState.update { state ->
            val trackPoints = appendTrackPoint(state.trackPoints, point)
            state.copy(
                location = location,
                trackPoints = trackPoints,
                errorMessage = null
            )
        }
    }

    private fun appendTrackPoint(current: List<LatLng>, point: LatLng): List<LatLng> {
        val last = current.lastOrNull()
        if (last != null && distanceMeters(last, point) < MIN_TRACK_DISTANCE_METERS) {
            return current
        }
        return (current + point).takeLast(MAX_TRACK_POINTS)
    }

    private fun distanceMeters(a: LatLng, b: LatLng): Double {
        val meanLatRad = Math.toRadians((a.latitude + b.latitude) / 2.0)
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLng = Math.toRadians(b.longitude - a.longitude)
        val x = dLng * kotlin.math.cos(meanLatRad)
        val y = dLat
        return sqrt(x.pow(2) + y.pow(2)) * EARTH_RADIUS_METERS
    }

    private companion object {
        private const val TAG = "FriendLocationViewModel"
        private const val MAX_TRACK_POINTS = 100
        private const val MIN_TRACK_DISTANCE_METERS = 5.0
        private const val LOCATION_POLL_INTERVAL_MS = 10_000L
        private const val EARTH_RADIUS_METERS = 6_371_000.0
    }
}
