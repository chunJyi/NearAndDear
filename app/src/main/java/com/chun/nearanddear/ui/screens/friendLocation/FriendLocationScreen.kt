package com.chun.nearanddear.ui.screens.friendLocation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chun.nearanddear.R
import com.chun.nearanddear.domain.model.User
import com.chun.nearanddear.domain.model.UserLocation
import com.chun.nearanddear.ui.screens.home.UserAddress
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

private val DefaultMapCenter = LatLng(16.778171, 96.138039)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendLocationScreen(
    userId: String,
    navController: NavController,
    viewModel: FriendLocationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        viewModel.startObservingFriendLocation(userId)
    }

    DisposableEffect(userId) {
        onDispose { viewModel.stopObservingFriendLocation() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friend Location", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.friend == null -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null && uiState.friend == null -> {
                    FriendLocationError(
                        message = uiState.errorMessage ?: "Could not load friend location",
                        onRetry = { viewModel.retryLoad(userId) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> FriendLocationContent(
                    friend = uiState.friend,
                    location = uiState.location,
                    trackPoints = uiState.trackPoints,
                    isLive = uiState.isLive
                )
            }
        }
    }
}

@Composable
private fun FriendLocationContent(
    friend: User?,
    location: UserLocation?,
    trackPoints: List<LatLng>,
    isLive: Boolean
) {
    val visibleLocation = location?.takeIf { it.hasSharedCoordinates }
    val hasLocation = visibleLocation != null
    val context = LocalContext.current
    val markerPosition = LatLng(
        visibleLocation?.latitude ?: DefaultMapCenter.latitude,
        visibleLocation?.longitude ?: DefaultMapCenter.longitude
    )
    var followTarget by remember { mutableStateOf(true) }
    var showInfoCard by remember { mutableStateOf(true) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(markerPosition, if (hasLocation) 15f else 11f)
    }

    LaunchedEffect(markerPosition, hasLocation, followTarget) {
        if (followTarget && hasLocation) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(markerPosition, if (hasLocation) 15f else 11f)
            )
        }
    }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = true,
                isBuildingEnabled = true,
                mapType = MapType.NORMAL,
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.dark_map_type)
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                compassEnabled = true,
                scrollGesturesEnabled = true,
                tiltGesturesEnabled = true,
                myLocationButtonEnabled = true
            ),
        ) {
            if (trackPoints.size >= 2) {
                Polyline(
                    points = trackPoints,
                    color = Color(0xFF3B82F6),
                    width = 8f
                )
            }
            if (hasLocation) {
                Marker(
                    state = MarkerState(position = markerPosition),
                    title = friend?.name ?: "Friend",
                    snippet = "Current location"
                )
            }
        }

        AnimatedVisibility(
            visible = showInfoCard,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = if (hasLocation) Color(0xFF2563EB) else Color.Gray
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = friend?.name ?: "Friend",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = when {
                                    hasLocation && isLive -> "Live tracking"
                                    hasLocation -> "Tracking (polling)"
                                    else -> "Location not available"
                                },
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                        if (hasLocation) {
                            LiveTrackingBadge(isLive = isLive)
                            Spacer(Modifier.width(4.dp))
                        }
                        IconButton(onClick = { showInfoCard = false }) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Hide info"
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (hasLocation) {
                        FollowTargetToggle(
                            followTarget = followTarget,
                            onFollowTargetChange = { followTarget = it }
                        )

                        Spacer(Modifier.height(12.dp))

                        UserAddress(
                            context = context,
                            latitude = visibleLocation?.latitude,
                            longitude = visibleLocation?.longitude,
                            color = Color(0xFF111827)
                        )
                        visibleLocation?.updatedAt?.let {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Updated $it",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        if (trackPoints.size >= 2) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Path: ${trackPoints.size} points",
                                color = Color(0xFF2563EB),
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Text(
                            text = "Ask your friend to start location sharing. Updates appear automatically.",
                            color = Color(0xFF4B5563)
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = !showInfoCard,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            Surface(
                modifier = Modifier.clickable { showInfoCard = true },
                shape = MaterialTheme.shapes.large,
                color = Color.White,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = friend?.name ?: "Show info",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Show info",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FollowTargetToggle(
    followTarget: Boolean,
    onFollowTargetChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Follow on map",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (followTarget) {
                    "Camera follows friend"
                } else {
                    "Map stays where you pan"
                },
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
        Switch(
            checked = followTarget,
            onCheckedChange = onFollowTargetChange
        )
    }
}

@Composable
private fun LiveTrackingBadge(isLive: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (isLive) Color(0xFF22C55E) else Color(0xFFF59E0B),
                    shape = CircleShape
                )
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = if (isLive) "Live" else "Poll",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isLive) Color(0xFF16A34A) else Color(0xFFD97706)
        )
    }
}

@Composable
private fun FriendLocationError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Could not load location",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(text = message, color = Color.Gray)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
