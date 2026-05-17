package com.chun.nearanddear.ui.screens.home


import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import coil.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.chun.nearanddear.R
import com.chun.nearanddear.domain.model.FriendModel
import com.chun.nearanddear.domain.model.UserLocation
import com.chun.nearanddear.domain.model.User
import com.chun.nearanddear.ui.navigation.Routes
import com.chun.nearanddear.ui.theme.NearAndDearFontFamily
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import androidx.compose.material.icons.filled.Settings
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val context: Context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }


    val resolutionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            // GPS enabled
//        } else {
//            // GPS not enabled
//        }
    }

    LaunchedEffect(uiState.isServiceRunning) {
        if (!viewModel.isLocationEnabled(context)) {
            val result = snackbarHostState.showSnackbar(
                message = "GPS is turned off. Tap to enable.",
                actionLabel = "Enable",
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                // TODO: Implement GPS enable logic
                viewModel.requestEnableGPS(context, resolutionLauncher);
                // This could open location settings or use a resolution launcher
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshFriendRequests()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = colorResource(id = R.color.background_color),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)

        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppBar(
                isServiceRunning = uiState.isServiceRunning,
                onToggleService = { viewModel.toggleService(context) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.Main.SETTINGS) },
                modifier = Modifier.padding(16.dp),
                containerColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {

            CarouselExampleMultiBrowse(
                uiState.currentUser,
                uiState.location,
                uiState.isServiceRunning
            )

            FriendCard(
                friendList = uiState.friendList,
                navController = navController
            )
        }
    }
}

@Composable
fun AppBar(
    isServiceRunning: Boolean,
    onToggleService: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 15.dp, end = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        content = {
            Text(
                text = "Near & Dear",
                fontSize = 30.sp,
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = NearAndDearFontFamily
            )

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = onToggleService,
                border = BorderStroke(
                    1.dp,
                    if (isServiceRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.width(140.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),

            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isServiceRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isServiceRunning) "Stop Sharing" else "Start Sharing",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarouselExampleMultiBrowse(
    currentUser: User?,
    location: UserLocation?,
    isServiceRunning: Boolean
) {
    data class CarouselItem(
        val id: Int,
    )

    val items = remember {
        listOf(
            CarouselItem(
                id = 0
            ),
            CarouselItem(
                id = 1
            ),
        )
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val itemWidth = maxWidth * 0.8f

        HorizontalMultiBrowseCarousel(
            state = rememberCarouselState { items.count() },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 16.dp, bottom = 16.dp),
            preferredItemWidth = itemWidth,
            itemSpacing = 12.dp,
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) { i ->
            if (i == 0) {
                UserLocationInfoCard(
                    user = currentUser,
                    location = location,
                    isServiceRunning = isServiceRunning
                )
            } else {
                UserLocationMapCard(location, name = currentUser?.name)
            }
        }
    }
}

@Composable
fun UserLocationInfoCard(user: User?, location: UserLocation?, isServiceRunning: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(15.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Background Image
            Image(
                painter = painterResource(R.drawable.user_card), // Replace with your image resource
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Content Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                // User Data Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (user?.avatarUrl != null) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = "Profile avatar",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                modifier = Modifier.padding(8.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        user?.name?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isServiceRunning) Color(0xFF2ECC71) else Color.Gray
                                    )
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isServiceRunning) "Live location" else "Offline",
                                fontSize = 15.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    Modifier.fillMaxWidth(0.6f),
                ) {
                    Text(
                        text = "📍",
                        fontSize = 20.sp,
                        color = Color.Gray
                    )
                    Spacer(Modifier.width(6.dp))
                    UserAddress(
                        LocalContext.current,
                        location?.latitude,
                        location?.longitude,
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                }
                Spacer(Modifier.fillMaxWidth(0.4f))

            } // Close Column
        } // Close Box
    } // Close Card
}

@Composable
fun UserLocationMapCard(location: UserLocation?, name: String? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(15.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // User Data Header
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LocationMapCard(location, name)
            }
        }
    }
}


@Composable
fun LocationMapCard(location: UserLocation?, name: String? = null) {
    val lat = location?.latitude ?: 16.778171
    val lon = location?.longitude ?: 96.138039
    val userLatLng = LatLng(lat, lon)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLatLng, 15f)
    }

    LaunchedEffect(userLatLng) {
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8ECEB))
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = MarkerState(position = userLatLng),
                title = name ?: "You",
                snippet = "Current Location"
            )
        }
    }
}

/**
 * FriendCard shows the home screen friend list.
 *
 * @param navController the navigation controller to open a friend's location
 */
@Composable
private fun FriendCard(
    friendList: List<FriendModel>?,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFBFD)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Row( modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Friends")

                Text(
                    text = "View All Friends",
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        navController.navigate(Routes.Main.FRIENDS)
                    }
                )
            }

            FriendList(navController, friendList)
        }
    }
}


/**
 * A composable function that displays a list of friends with their avatars and names.
 *
 * Each friend can be clicked to navigate to the map screen with the selected friend's location.
 *
 * @param navController the navigation controller to navigate to the map screen
 * @param friends a list of [FriendModel] to display
 */
@SuppressLint("ContextCastToActivity")
@Composable
private fun FriendList(
    navController: NavController, friends: List<FriendModel>?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 300.dp, max = 300.dp)
            .verticalScroll(rememberScrollState())
    ) {
        friends?.forEach { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(Routes.Main.friendLocation(item.userID))
                    }
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    getImageAsync(item.friendAvatarUrl)

                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.Bold)
                        Text(
                            formatUserId(item.userID),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Icon(
                        painter = painterResource(id = R.drawable.pin_map),
                        contentDescription = "Details",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * Format a user ID to hide the middle part.
 *
 * If the user ID is 10 characters or shorter, it is returned as is.
 * Otherwise, the first 5 characters, then 5 asterisks, then the last 5 characters are returned.
 *
 * @param userId the user ID to format
 * @return the formatted user ID
 */
fun formatUserId(userId: String): String {
    return if (userId.length <= 10) {
        userId
    } else {
        "${userId.take(5)}*****${userId.takeLast(5)}"
    }
}

/**
 * Simple composable to display friend avatar or placeholder
 */
@Composable
private fun getImageAsync(avatarUrl: String?) {
    if (avatarUrl.isNullOrEmpty()) {
        // Show placeholder icon
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Avatar",
                modifier = Modifier.padding(8.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    } else {
        // Load image from URL using Coil
        AsyncImage(
            model = avatarUrl,
            contentDescription = "Avatar",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}


@Composable
fun UserAddress(
    context: Context,
    latitude: Double?,
    longitude: Double?,
    fontSize: TextUnit = TextUnit.Unspecified,
    color: Color = Color.Unspecified
) {
    var address by remember { mutableStateOf("Loading...") }

    LaunchedEffect(latitude, longitude) {
        if (latitude == null || longitude == null) {
            address = "Location not available"
            return@LaunchedEffect
        }

        try {
            val geocoder = Geocoder(context)
            val addressList = withContext(Dispatchers.IO) {
                geocoder.getFromLocation(latitude, longitude, 1)
            }
            address = if (!addressList.isNullOrEmpty()) {
                val addressObj = addressList[0]
                "${addressObj.getAddressLine(0)}, ${addressObj.adminArea}"
            } else {
                " Address not found"
            }
        } catch (e: Exception) {
            address = "Error: ${e.localizedMessage}"
        }
    }
    Text(
        text = " $address",
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 3,
        fontWeight = FontWeight.Medium,
        fontSize = fontSize,
        color = color
    )
}
