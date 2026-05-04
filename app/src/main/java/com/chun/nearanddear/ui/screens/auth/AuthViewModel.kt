package com.chun.nearanddear.ui.screens.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chun.nearanddear.domain.auth.LoginErrorMapper
import com.chun.nearanddear.domain.auth.LoginOutcome
import com.chun.nearanddear.data.session.SessionDataStore
import com.chun.nearanddear.domain.usecase.auth.LoginUseCase
import com.chun.nearanddear.domain.usecase.auth.SaveUserIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val saveUserIdUseCase: SaveUserIdUseCase,
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun clearLoginError() {
        _uiState.update { it.copy(errorTitle = null, errorMessage = null) }
    }

    fun loginWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            try {
                when (val outcome = loginUseCase(context)) {
                    is LoginOutcome.Success -> {
                        saveUserIdUseCase(outcome.user)
                        this@AuthViewModel.sessionDataStore.setUser(outcome.user)
                        _uiState.value = AuthUiState(user = outcome.user)
                    }

                    LoginOutcome.Cancelled -> _uiState.value = AuthUiState()

                    is LoginOutcome.Failure -> setFailureState(outcome.title, outcome.message)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val failure = LoginErrorMapper.fromThrowable(e)
                setFailureState(failure.title, failure.message)
            }
        }
    }

    private fun setFailureState(title: String, message: String) {
        _uiState.value = AuthUiState(
            errorTitle = title,
            errorMessage = message
        )
    }
}
