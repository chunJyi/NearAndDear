package com.chun.nearanddear.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chun.nearanddear.ui.screens.auth.AuthViewModel
import com.chun.nearanddear.ui.screens.auth.LoginScreen
import com.chun.nearanddear.ui.screens.auth.StartupViewModel
import com.chun.nearanddear.ui.screens.home.HomeScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val startupViewModel: StartupViewModel = hiltViewModel()
    val startDestination = if (startupViewModel.isUserLoggedIn()) {
        Routes.Main.HOME
    } else {
        Routes.Auth.LOGIN
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.Auth.LOGIN) {
            val viewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.Main.HOME) {
                        popUpTo(Routes.Auth.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Main.HOME) {
            HomeScreen()
        }
    }
}
