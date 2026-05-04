package com.chun.nearanddear.ui.screens.splash

sealed class SplashUiState {
    object Loading : SplashUiState()
    object NavigateToLogin : SplashUiState()
    object NavigateToHome : SplashUiState()
    data class Error(val message: String, val canRetry: Boolean = true) : SplashUiState()
}
