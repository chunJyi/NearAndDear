package com.chun.nearanddear.ui.screens.friendDetails

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chun.nearanddear.domain.model.User
import com.chun.nearanddear.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailScreen(
    userId: String,
    navController: NavController,
    viewModel: FriendDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load user data when screen opens
    LaunchedEffect(userId) {
        viewModel.loadUserDetails(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friend Details", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
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
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error loading user details",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadUserDetails(userId) }) {
                            Text("Retry")
                        }
                    }
                }
                uiState.user != null -> {
                    FriendDetailContent(
                        user = uiState.user!!,
                        successMessage = uiState.successMessage,
                        friendRequestSent = uiState.friendRequestSent,
                        onAddFriend = { viewModel.sendFriendRequest(userId) },
                        onViewLocation = { navController.navigate(Routes.Main.HOME) },
                        onSendMessage = { /* TODO: Implement messaging */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendDetailContent(
    user: User,
    successMessage: String?,
    friendRequestSent: Boolean,
    onAddFriend: () -> Unit,
    onViewLocation: () -> Unit,
    onSendMessage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // User Avatar and Basic Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "User Avatar",
                    modifier = Modifier.padding(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onAddFriend,
                modifier = Modifier.weight(1f),
                enabled = !friendRequestSent
            ) {
                Icon(
                    if (friendRequestSent) Icons.Filled.Check else Icons.Filled.Person,
                    contentDescription = if (friendRequestSent) "Request Sent" else "Add Friend"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (friendRequestSent) "Request Sent" else "Add Friend")
            }

            OutlinedButton(
                onClick = onViewLocation,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = "View Location")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Location")
            }

            OutlinedButton(
                onClick = onSendMessage,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Email, contentDescription = "Send Message")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Message")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Success Message
        if (!successMessage.isNullOrEmpty()) {
            Text(
                text = successMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Green,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Additional Details Card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                InfoRow(label = "User ID", value = user.id)
                InfoRow(label = "Email", value = user.email)
                // Add more fields as needed
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
