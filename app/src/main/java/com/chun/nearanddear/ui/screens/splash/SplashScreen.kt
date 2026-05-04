package com.chun.nearanddear.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chun.nearanddear.R
import com.chun.nearanddear.ui.animation.ScaleOutAnimatedText
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is SplashUiState.NavigateToLogin -> {
                delay(500) // Small delay for smooth transition
                onNavigateToLogin()
            }
            is SplashUiState.NavigateToHome -> {
                delay(500) // Small delay for smooth transition
                onNavigateToHome()
            }
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {

        Image(
            painter = painterResource(id = R.drawable.background_image),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // App Logo
            Image(
                painter = painterResource(id = R.drawable.app_icon_splash),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(height = 220.dp, width = 300.dp)
                    .padding(bottom = 0.dp)
            )

            // Tagline
            ScaleOutAnimatedText(
                text = "Stay close to the ones you love",
                style = TextStyle(
                    fontSize = 25.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(bottom = 25.dp)
            )

            // Show loading indicator or error/retry UI
        val currentState = uiState
        when (currentState) {
            is SplashUiState.Loading -> {
                CircularProgressIndicator(
                    color = Color(0xFF1976D2),
                    modifier = Modifier.size(48.dp)
                )
            }
            is SplashUiState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Something went wrong",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.retry() }
                    ) {
                        Text("Retry")
                    }
                }
            }
            else -> {
                CircularProgressIndicator(
                    color = Color(0xFF1976D2),
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        }
    }
}
