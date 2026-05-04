package com.chun.nearanddear.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chun.nearanddear.ui.screens.auth.AuthViewModel
import com.chun.nearanddear.ui.screens.auth.LoginScreen
import com.chun.nearanddear.ui.screens.splash.SplashScreen
import com.chun.nearanddear.ui.screens.home.HomeScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Auth.SPLASH
    ) {
        composable(Routes.Auth.SPLASH) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.Auth.LOGIN) {
                        popUpTo(Routes.Auth.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.Main.HOME) {
                        popUpTo(Routes.Auth.SPLASH) { inclusive = true }
                    }
                }
            )
        }

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
