package com.chun.nearanddear.ui.screens.friends

import coil.compose.AsyncImage
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chun.nearanddear.R
import com.chun.nearanddear.domain.model.FriendModel
import com.chun.nearanddear.domain.model.FriendRequestItem
import com.chun.nearanddear.domain.model.User
import com.chun.nearanddear.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    viewModel: FriendViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }
    var selectedFriendStateTab by remember { mutableIntStateOf(0) }

    // Trigger search when query changes
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            viewModel.searchUsers(searchQuery)
        } else {
            viewModel.clearSearchResults()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchVisible) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search friends...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    } else {
                        Text("", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isSearchVisible) {
                        IconButton(onClick = {
                            isSearchVisible = false
                            searchQuery = ""
                            viewModel.clearSearchResults()
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancel Search")
                        }
                    } else {
                        IconButton(onClick = { isSearchVisible = true }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Content based on search visibility and query
            if (isSearchVisible) {
                // Show loading indicator if searching
                if (uiState.isSearching) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    // Search Results from Supabase users table
                    val searchResults = uiState.searchResults
                    if (searchResults.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No users found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            searchResults.forEach { user ->
                                UserSearchResultItem(
                                    user = user,
                                    onClick = {
                                        navController.navigate(Routes.Main.friendDetail(user.id))
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                FriendStateList(
                    selectedTab = selectedFriendStateTab,
                    onTabSelected = { selectedFriendStateTab = it },
                    friendList = uiState.friendList.orEmpty(),
                    incomingFriendRequests = uiState.incomingFriendRequests,
                    outgoingFriendRequests = uiState.outgoingFriendRequests,
                    isLoading = uiState.isLoadingFriends,
                    errorMessage = uiState.error,
                    onDismissError = viewModel::clearError,
                    onFriendClickDetail = { friend ->
                        navController.navigate(Routes.Main.friendDetail(friend.userID))
                    },
                    onFriendClickMap = { friend ->
                        navController.navigate(Routes.Main.friendLocation(friend.userID))
                    },
                    onFavoriteClick = viewModel::setFriendFavorite,
                    onAcceptIncomingRequest = viewModel::acceptIncomingFriendRequest,
                    onDeclineIncomingRequest = viewModel::declineIncomingFriendRequest,
                    onCancelOutgoingRequest = viewModel::cancelOutgoingFriendRequest
                )
            }
        }
    }
}

@Composable
private fun FriendStateList(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    friendList: List<FriendModel>,
    incomingFriendRequests: List<FriendRequestItem>,
    outgoingFriendRequests: List<FriendRequestItem>,
    isLoading: Boolean,
    errorMessage: String?,
    onDismissError: () -> Unit,
    onFriendClickDetail: (FriendModel) -> Unit,
    onFriendClickMap: (FriendModel) -> Unit,
    onFavoriteClick: (FriendModel, Boolean) -> Unit,
    onAcceptIncomingRequest: (String) -> Unit,
    onDeclineIncomingRequest: (String) -> Unit,
    onCancelOutgoingRequest: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        val favoriteFriends = friendList.filter { it.isFavorite }
        val tabs = listOf(
            "Friends" to friendList.size,
            "Favorites" to favoriteFriends.size,
            "Request" to incomingFriendRequests.size,
            "Pending" to outgoingFriendRequests.size
        )

        FriendStateTabs(
            tabs = tabs,
            selectedTab = selectedTab,
            onTabSelected = onTabSelected
        )

        errorMessage?.let { message ->
            Text(
                text = message,
                color = Color(0xFFB91C1C),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDismissError() }
                    .padding(top = 12.dp, bottom = 4.dp)
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Column
        }

        when (selectedTab) {
            0 -> FriendCardsList(
                friends = friendList,
                onFriendClickDetail = onFriendClickDetail,
                onFriendClickMap = onFriendClickMap,
                onFavoriteClick = onFavoriteClick
            )

            1 -> FriendCardsList(
                friends = favoriteFriends,
                emptyMessage = "No favorite friends yet",
                onFriendClickDetail = onFriendClickDetail,
                onFriendClickMap = onFriendClickMap,
                onFavoriteClick = onFavoriteClick
            )

            2 -> IncomingRequestCardsList(
                requests = incomingFriendRequests,
                onAccept = onAcceptIncomingRequest,
                onDecline = onDeclineIncomingRequest
            )

            3 -> OutgoingPendingCardsList(
                pending = outgoingFriendRequests,
                onCancel = onCancelOutgoingRequest
            )
        }
    }
}

@Composable
private fun FriendStateTabs(
    tabs: List<Pair<String, Int>>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = selectedTab == index
            OutlinedButton(
                onClick = { onTabSelected(index) },
                modifier = Modifier
                    .height(42.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) {
                        Color.Blue
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) {
                        Color.Blue
                    } else {
                        Color.Transparent
                    },
                    contentColor = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    val iconColor = when (tab.first) {
                        "Friends" -> Color(0xFF4CAF50)   // green
                        "Favorites" -> Color(0xFFE11D48) // rose
                        "Request" -> Color(0xFF2196F3)   // blue
                        else -> Color(0xFFFF9800)   // orange
                    }

                    val selectedIconColor = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        iconColor
                    }
                    if (tab.first == "Favorites") {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = selectedIconColor
                        )
                    } else {
                        Icon(
                            painter = painterResource(
                                id = when (tab.first) {
                                    "Friends" -> R.drawable.group
                                    "Request" -> R.drawable.add_friend
                                    else -> R.drawable.friend_request
                                }
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = selectedIconColor
                        )
                    }

                    Text(
                        text = "${tab.first} (${tab.second})",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )

                }

            }
        }
    }
}

@Composable
private fun FriendCardsList(
    friends: List<FriendModel>,
    emptyMessage: String = "No friends yet",
    onFriendClickDetail: (FriendModel) -> Unit,
    onFriendClickMap: (FriendModel) -> Unit,
    onFavoriteClick: (FriendModel, Boolean) -> Unit,
) {
    if (friends.isEmpty()) {
        EmptyFriendState(message = emptyMessage)
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(friends, key = { it.userID }) { friend ->
            FriendListCard(
                friend = friend,
                onClickDetail = { onFriendClickDetail(friend) },
                onClickMap = { onFriendClickMap(friend) },
                onFavoriteClick = { onFavoriteClick(friend, !friend.isFavorite) }
            )
        }
    }
}

@Composable
private fun IncomingRequestCardsList(
    requests: List<FriendRequestItem>,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit
) {
    if (requests.isEmpty()) {
        EmptyFriendState(message = "No friend requests")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(requests, key = { it.relationshipId }) { request ->
            IncomingRequestListCard(
                item = request,
                onAccept = { onAccept(request.relationshipId) },
                onDecline = { onDecline(request.relationshipId) }
            )
        }
    }
}

@Composable
private fun OutgoingPendingCardsList(
    pending: List<FriendRequestItem>,
    onCancel: (String) -> Unit
) {
    if (pending.isEmpty()) {
        EmptyFriendState(message = "No pending requests")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(pending, key = { it.relationshipId }) { item ->
            OutgoingPendingListCard(
                item = item,
                onCancel = { onCancel(item.relationshipId) }
            )
        }
    }
}

@Composable
private fun FriendListCard(
    friend: FriendModel,
    onClickDetail: () -> Unit,
    onClickMap: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClickDetail),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FriendAvatar(
                avatarUrl = friend.friendAvatarUrl,
                name = friend.name
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatUserId(friend.userID),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                FriendStateBadge(label = "FRIEND", color = Color(0xFF2ECC71))
            }
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = if (friend.isFavorite) {
                        "Remove favorite"
                    } else {
                        "Add favorite"
                    },
                    tint = if (friend.isFavorite) {
                        Color(0xFFFFB300)
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.pin_map),
                contentDescription = "Details",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onClickMap)

            )
        }
    }
}

@Composable
private fun IncomingRequestListCard(
    item: FriendRequestItem,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            FriendRequestHeader(
                avatarUrl = item.counterpartyAvatarUrl,
                name = item.counterpartyName,
                subtitle = item.counterpartyEmail,
                badgeText = "REQUEST",
                badgeColor = Color(0xFF2563EB)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Accept")
                    }
                }
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {

                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Decline")
                    }
                }
            }
        }
    }
}

@Composable
private fun OutgoingPendingListCard(
    item: FriendRequestItem,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            FriendRequestHeader(
                avatarUrl = item.counterpartyAvatarUrl,
                name = item.counterpartyName,
                subtitle = "Waiting for response",
                badgeText = "PENDING",
                badgeColor = Color(0xFFFFA500)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.Red
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cancel request")
                }
            }
        }
    }
}

@Composable
private fun FriendRequestHeader(
    avatarUrl: String?,
    name: String,
    subtitle: String,
    badgeText: String,
    badgeColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        FriendAvatar(
            avatarUrl = avatarUrl.orEmpty(),
            name = name
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        FriendStateBadge(label = badgeText, color = badgeColor)
    }
}

@Composable
private fun FriendAvatar(
    avatarUrl: String,
    name: String
) {
    Surface(
        modifier = Modifier.size(50.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        if (avatarUrl.isBlank()) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "$name avatar",
                modifier = Modifier.padding(12.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        } else {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "$name avatar",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun FriendStateBadge(
    label: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun EmptyFriendState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
    }
}

@Composable
private fun FriendItem(
    friend: FriendModel,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Surface(
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Avatar",
                modifier = Modifier.padding(12.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Friend Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = friend.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatUserId(friend.userID),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                // State badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (friend.friendState.name) {
                        "PENDING" -> Color(0xFFFFA500)
                        "FRIEND" -> Color(0xFF2ECC71)
                        "BLOCKED" -> Color(0xFFE74C3C)
                        else -> Color.Gray
                    }
                ) {
                    Text(
                        text = friend.friendState.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Action Icon
        Icon(
            painter = painterResource(id = R.drawable.facebook_logo),
            contentDescription = "Details",
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun SearchResultItem(
    friend: FriendModel,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
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

        Spacer(modifier = Modifier.width(12.dp))

        // Friend Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = friend.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Friend",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun UserSearchResultItem(
    user: User,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
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

        Spacer(modifier = Modifier.width(12.dp))

        // User Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

// Utility function to format user ID
fun formatUserId(userId: String): String {
    return if (userId.length <= 10) {
        userId
    } else {
        "${userId.take(5)}*****${userId.takeLast(5)}"
    }
}
