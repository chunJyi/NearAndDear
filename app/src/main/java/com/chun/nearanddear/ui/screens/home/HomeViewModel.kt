package com.chun.nearanddear.ui.screens.home

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chun.nearanddear.data.remote.supabase.SupabaseUserDataSource
import com.chun.nearanddear.data.session.SessionDataStore
import com.chun.nearanddear.domain.model.Friend
import com.chun.nearanddear.domain.model.FriendModel
import com.chun.nearanddear.domain.model.FriendRequestItem
import com.chun.nearanddear.domain.model.User
import com.chun.nearanddear.domain.model.UserLocation
import com.chun.nearanddear.domain.service.LocationService
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val supabaseUserDataSource: SupabaseUserDataSource
) : ViewModel() {

    private val serviceRunningState = MutableStateFlow(false)
    private val friendDataLoadingState = MutableStateFlow(false)
    private val friendDataErrorState = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> =
        combine(
            sessionDataStore.currentUser,
            sessionDataStore.locationModel,
            serviceRunningState,
            sessionDataStore.friendModel,
            friendDataLoadingState,
            friendDataErrorState
        ) { values ->

            val user = values[0] as User?
            val location = values[1] as UserLocation?
            val isServiceRunning = values[2] as Boolean
            val friendList = values[3] as List<FriendModel>?
            val friendLoading = values[4] as Boolean
            val friendError = values[5] as String?

            HomeUiState(
                currentUser = user,
                location = location,
                isServiceRunning = isServiceRunning,
                friendList = friendList,
                isFriendDataLoading = friendLoading,
                friendRequestsError = friendError
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    init {
        viewModelScope.launch {
            sessionDataStore.currentUser.collect { user ->
                if (user == null) {

                    friendDataErrorState.value = null
                    sessionDataStore.clearFriend()
                } else {
                    loadFriendData(user.id)
                }
            }
        }
    }

    fun refreshFriendRequests() {
        val userId = sessionDataStore.currentUser.value?.id ?: return
        loadFriendData(userId)
    }

    private fun loadFriendData(userId: String) {
        viewModelScope.launch {
            friendDataLoadingState.value = true
            friendDataErrorState.value = null
            var combinedError: String? = null

            supabaseUserDataSource.getAcceptedFriends(userId).fold(
                onSuccess = { sessionDataStore.setFriend(it) },
                onFailure = { combinedError = it.message ?: "Could not load friends" }
            )



            friendDataErrorState.value = combinedError
            friendDataLoadingState.value = false
        }
    }

    fun toggleService(context: Context) {
        val currentState = serviceRunningState.value
        if (currentState) {
            // Stop service
            val serviceIntent = Intent(context, LocationService::class.java)
            context.stopService(serviceIntent)
        } else {
            // Start service
            val serviceIntent = Intent(context, LocationService::class.java)
            context.startForegroundService(serviceIntent)
        }
        serviceRunningState.update { !it }
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun requestEnableGPS(
        context: Context,
        resolutionLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
    ) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val client = LocationServices.getSettingsClient(context)

        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            // GPS already enabled
        }.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(exception.resolution).build()
                    resolutionLauncher.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    sendEx.printStackTrace()
                }
            }
        }
    }


}
