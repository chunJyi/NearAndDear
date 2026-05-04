package com.chun.nearanddear.ui.screens.splash

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chun.nearanddear.data.session.SessionDataStore
import com.chun.nearanddear.domain.usecase.auth.GetUserIdUseCase
import com.chun.nearanddear.domain.usecase.auth.GetUserFromSupabaseUseCase
import com.chun.nearanddear.utils.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getUserFromSupabaseUseCase: GetUserFromSupabaseUseCase,
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState

    private val _showErrorDialog = MutableStateFlow(false)
    val showErrorDialog: StateFlow<Boolean> = _showErrorDialog

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted: StateFlow<Boolean> = _permissionsGranted

    init {
        startSplashSequence()
    }

    fun retry() {
        _uiState.value = SplashUiState.Loading
        startSplashSequence()
    }

    private fun startSplashSequence() {
        viewModelScope.launch {
            // Show splash screen for at least 2 seconds
            delay(2000)

            // Check if userId exists in preferences
            val userId = getUserIdUseCase()
            
            if (userId == null) {
                // No userId in preferences, navigate to login
                _uiState.value = SplashUiState.NavigateToLogin
                return@launch
            }

            // Try to get user from Supabase
            val result = getUserFromSupabaseUseCase(userId)
            
            result.fold(
                onSuccess = { user ->
                    this@SplashViewModel.sessionDataStore.setUser(user)
                    _uiState.value = SplashUiState.NavigateToHome
                },
                onFailure = { exception ->
                    // Failed to get user from Supabase, show error with retry option
                    _uiState.value = SplashUiState.Error(
                        message = exception.message ?: "Failed to load user data",
                        canRetry = true
                    )
                }
            )
        }
    }

    fun onPermissionResult(permissions: Map<String, Boolean>) {
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            _permissionsGranted.value = true
            onPermissionsGranted()
        } else {
            _showErrorDialog.value = true
        }
    }
    
    fun dismissErrorDialog() {
        _showErrorDialog.value = false
    }

    private fun onPermissionsGranted() {
        // Navigate to next screen after permissions are granted
        viewModelScope.launch {
            _uiState.value = SplashUiState.NavigateToHome
        }
    }
}
