package com.chun.nearanddear.ui.screens.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chun.nearanddear.domain.auth.LoginErrorMapper
import com.chun.nearanddear.domain.auth.LoginOutcome
import com.chun.nearanddear.domain.usecase.auth.LoginUseCase
import com.chun.nearanddear.domain.usecase.auth.SaveUserIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val saveUserIdUseCase: SaveUserIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun clearLoginError() {
        _uiState.value = _uiState.value.copy(errorTitle = null, errorMessage = null)
    }

    fun loginWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            try {
                when (val outcome = loginUseCase(context)) {
                    is LoginOutcome.Success -> {
                        saveUserIdUseCase(outcome.user)
                        _uiState.value = AuthUiState(user = outcome.user)
                    }

                    LoginOutcome.Cancelled ->
                        _uiState.value = AuthUiState()

                    is LoginOutcome.Failure ->
                        _uiState.value = AuthUiState(
                            errorTitle = outcome.title,
                            errorMessage = outcome.message
                        )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val failure = LoginErrorMapper.fromThrowable(e)
                _uiState.value = AuthUiState(
                    errorTitle = failure.title,
                    errorMessage = failure.message
                )
            }
        }
    }
}
