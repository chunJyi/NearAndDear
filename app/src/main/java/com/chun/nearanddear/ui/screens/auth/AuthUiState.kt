package com.chun.nearanddear.ui.screens.auth

import com.chun.nearanddear.domain.model.User

/**
 * Simple UI state holder for authentication screens.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorTitle: String? = null,
    val errorMessage: String? = null
)

