package com.chun.nearanddear.ui.screens.auth

import androidx.lifecycle.ViewModel
import com.chun.nearanddear.domain.usecase.auth.IsUserLoggedInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StartupViewModel @Inject constructor(
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase
) : ViewModel() {
    
    fun isUserLoggedIn(): Boolean {
        return isUserLoggedInUseCase()
    }
}
