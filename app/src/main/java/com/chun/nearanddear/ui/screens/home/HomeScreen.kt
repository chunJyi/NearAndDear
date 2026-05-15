package com.chun.nearanddear.ui.screens.home

import android.R.attr.text
import android.R.color.transparent
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import coil.compose.AsyncImage
import androidx.compose.foundation.border
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.chun.nearanddear.domain.model.FriendRequestItem
import com.chun.nearanddear.domain.model.UserLocation
import com.chun.nearanddear.domain.model.User
import com.chun.nearanddear.ui.navigation.Routes
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

            CarouselExampleMultiBrowse(
                uiState.currentUser,
                uiState.location,
                uiState.isServiceRunning
            )

            FriendCard(
                friendList = uiState.friendList,
                incomingFriendRequests = uiState.incomingFriendRequests,
                outgoingFriendRequests = uiState.outgoingFriendRequests,
                isFriendDataLoading = uiState.isFriendDataLoading,
                friendRequestsError = uiState.friendRequestsError,
                onAcceptIncomingRequest = viewModel::acceptIncomingFriendRequest,
                onDeclineIncomingRequest = viewModel::declineIncomingFriendRequest,
                onCancelOutgoingRequest = viewModel::cancelOutgoingFriendRequest,
                onDismissFriendRequestError = viewModel::clearFriendRequestsError,
                navController = navController,
                tabs = tabs,
                selectedTabIndex = selectedTab.intValue,
                onTabSelected = { selectedTab.intValue = it }
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
                fontFamily = FontFamily.Cursive
            )

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = onToggleService,
                border = BorderStroke(1.dp, if (isServiceRunning) Color.Red else Color.Green),
                modifier = Modifier.width(160.dp),
                contentPadding = PaddingValues(1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = "View Location")
                    Text(
                        text = if (isServiceRunning) "Stop Sharing Location" else "Start Sharing Location",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
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
 * FriendCard shows friends, incoming requests (Request tab), and outgoing pending (Pending tab).
 *
 * @param navController used to open the full friends screen from the Friends tab
 * @param tabs tab titles shown in the tab row
 * @param selectedTabIndex selected tab index
 * @param onTabSelected called when the user selects a tab
 */
@Composable
private fun FriendCard(
    friendList: List<FriendModel>?,
    incomingFriendRequests: List<FriendRequestItem>,
    outgoingFriendRequests: List<FriendRequestItem>,
    isFriendDataLoading: Boolean,
    friendRequestsError: String?,
    onAcceptIncomingRequest: (String) -> Unit,
    onDeclineIncomingRequest: (String) -> Unit,
    onCancelOutgoingRequest: (String) -> Unit,
    onDismissFriendRequestError: () -> Unit,
    navController: NavController,
    tabs: List<String>,
    selectedTabIndex: Int,
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
            FriendsTabs(tabs, selectedTabIndex, onTabSelected)
            Spacer(Modifier.height(8.dp))

            // View All Friends Button
            if (selectedTabIndex == 0) {
                Button(
                    onClick = { navController.navigate(Routes.Main.FRIENDS) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB),
                        contentColor = Color.White
                    )
                ) {
                    Text("View All Friends")
                }
            }

            Spacer(Modifier.height(8.dp))
            when (selectedTabIndex) {
                0 -> FriendList(navController, friendList)
                1 -> FriendRequestTabContent(
                    isLoading = isFriendDataLoading,
                    errorMessage = friendRequestsError,
                    requests = incomingFriendRequests,
                    onDismissError = onDismissFriendRequestError,
                    onAccept = onAcceptIncomingRequest,
                    onDecline = onDeclineIncomingRequest,
                    emptyMessage = "No friend requests"
                )

                2 -> FriendPendingTabContent(
                    isLoading = isFriendDataLoading,
                    errorMessage = friendRequestsError,
                    pending = outgoingFriendRequests,
                    onDismissError = onDismissFriendRequestError,
                    onCancel = onCancelOutgoingRequest,
                    emptyMessage = "No pending requests"
                )
            }
        }
    }
}

@Composable
private fun FriendRequestTabContent(
    isLoading: Boolean,
    errorMessage: String?,
    requests: List<FriendRequestItem>,
    onDismissError: () -> Unit,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    emptyMessage: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp, max = 320.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = Color(0xFFB91C1C),
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDismissError() }
                        .padding(bottom = 8.dp)
                )
            }
            if (requests.isEmpty()) {
                Text(
                    text = emptyMessage,
                    modifier = Modifier.padding(16.dp),
                    color = Color.Gray
                )
            } else {
                requests.forEach { item ->
                    IncomingFriendRequestRow(
                        item = item,
                        onAccept = { onAccept(item.relationshipId) },
                        onDecline = { onDecline(item.relationshipId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendPendingTabContent(
    isLoading: Boolean,
    errorMessage: String?,
    pending: List<FriendRequestItem>,
    onDismissError: () -> Unit,
    onCancel: (String) -> Unit,
    emptyMessage: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp, max = 320.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = Color(0xFFB91C1C),
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDismissError() }
                        .padding(bottom = 8.dp)
                )
            }
            if (pending.isEmpty()) {
                Text(
                    text = emptyMessage,
                    modifier = Modifier.padding(16.dp),
                    color = Color.Gray
                )
            } else {
                pending.forEach { item ->
                    OutgoingFriendPendingRow(
                        item = item,
                        onCancel = { onCancel(item.relationshipId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun IncomingFriendRequestRow(
    item: FriendRequestItem,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            getImageAsync(item.counterpartyAvatarUrl)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.counterpartyName, fontWeight = FontWeight.Bold)
                Text(
                    item.counterpartyEmail,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onAccept,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White
                )
            ) {
                Text("Accept")
            }
            OutlinedButton(
                onClick = onDecline,
                modifier = Modifier.weight(1f)
            ) {
                Text("Decline")
            }
        }
    }
}

@Composable
private fun OutgoingFriendPendingRow(
    item: FriendRequestItem,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            getImageAsync(item.counterpartyAvatarUrl)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.counterpartyName, fontWeight = FontWeight.Bold)
                Text(
                    "Waiting for response",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel request")
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

