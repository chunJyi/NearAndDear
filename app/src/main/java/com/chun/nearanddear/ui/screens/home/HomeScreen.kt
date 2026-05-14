package com.chun.nearanddear.ui.screens.home

import android.R.attr.title
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chun.nearanddear.R
import com.chun.nearanddear.domain.model.FriendModel
import com.chun.nearanddear.domain.model.UserLocation
import com.chun.nearanddear.domain.model.User
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
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
    val selectedTab = remember { mutableIntStateOf(0) }
    val tabs = listOf("Friends", "Request", "Pending")


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

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)

        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppBar(
                isServiceRunning = uiState.isServiceRunning,
                onToggleService = { viewModel.toggleService(context) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {

            CarouselExampleMultiBrowse(uiState.currentUser, uiState.location)

            FriendCard(
                uiState.friendList,
                navController,
                context,
                tabs,
                selectedTab.intValue
            ) {
                selectedTab.intValue = it
            }
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Near & Dear",
            fontSize = 30.sp,
            fontFamily = FontFamily.Cursive
        )
        Button(
            onClick = onToggleService,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isServiceRunning) Color.Red else Color.Green,
                contentColor = Color.White
            ),
            modifier = Modifier.height(40.dp)
        ) {
            Text(
                text = if (isServiceRunning) "STOP" else "START",
                fontSize = 14.sp
            )
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarouselExampleMultiBrowse(
    currentUser: User?,
    location: UserLocation?
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
                UserLocationInfoCard(user = currentUser, location = location)
            } else {
                UserLocationMapCard(location, name = currentUser?.name)
            }
        }
    }
}

@Composable
fun UserLocationInfoCard(user: User?, location: UserLocation?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                                    if (true) Color(0xFF2ECC71) else Color.Gray
                                )
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (true) "Live location" else "Offline",
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
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // User Data Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
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

@Composable
private fun FriendsTabs(
    tabs: List<String>, selectedTab: Int, onTabSelected: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTab, indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                Modifier
                    .tabIndicatorOffset(tabPositions[selectedTab])
                    .height(3.dp),
                color = Color(0xFF2563EB) // Blue indicator
            )
        }, divider = {},   // Remove default divider
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF000000)), // Outer tab row background
        containerColor = Color.Transparent, contentColor = Color.Black
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                modifier = Modifier
                    .background(
                        if (selectedTab == index) Color(0xFF2563EB) // Selected tab color
                        else Color(0xFFE0E0E0) // Unselected tab color
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .height(36.dp),
                text = {
                    Text(
                        title,
                        color = if (selectedTab == index) Color.White else Color.Black,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                })
        }
    }
}


/**
 * FriendCard is a composable function that displays a list of friends, requests and pending friends.
 * It also displays a tab row with three tabs: Friends, Requests and Pending.
 *
 * @param navController the NavController used to navigate to the details screen
 * @param context the Context used to display a toast message
 * @param tabs a list of strings containing the titles of the tabs
 * @param selectedIndex the index of the selected tab
 * @param onTabSelected a lambda function that is called when a tab is selected
 */
@Composable
private fun FriendCard(
    friendList: List<FriendModel>?,
    navController: NavController,
    context: Context,
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFBFD)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            FriendsTabs(tabs, selectedIndex, onTabSelected)
            Spacer(Modifier.height(8.dp))
//            FriendListHeader(navController, tabs[selectedIndex], context)
            Spacer(Modifier.height(8.dp))
            when (selectedIndex) {
                0 -> FriendList(navController, friendList)
                1 -> Text("Friend requests coming soon", modifier = Modifier.padding(16.dp))
                2 -> Text("Pending friends coming soon", modifier = Modifier.padding(16.dp))
            }
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
                        navController.navigate("map")
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
                        painter = painterResource(id = R.drawable.facebook_logo),
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
 * Checks if a user with the given ID is included in the friend list.
 *
 * @param friendList the list of friends to check against
 * @param userIdToCheck the user ID to find in the friend list
 * @return true if the friend list contains the user with the specified ID, false otherwise
 */
private fun isIncludeFriend(
    friendList: List<FriendModel>, userIdToCheck: String
): Boolean {
    return friendList.any { userIdToCheck == it.userID }
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
        // TODO: Implement actual image loading with Coil or Glide
        // For now, show placeholder
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

