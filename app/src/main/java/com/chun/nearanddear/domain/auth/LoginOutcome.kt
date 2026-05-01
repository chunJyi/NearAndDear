package com.chun.nearanddear.domain.auth

import com.chun.nearanddear.domain.model.User

/**
 * Result of a Google + backend login attempt. Used so the UI can treat
 * user cancellation differently from real failures.
 */
sealed class LoginOutcome {
    data class Success(val user: User) : LoginOutcome()
    data object Cancelled : LoginOutcome()
    data class Failure(
        val title: String,
        val message: String,
        val recoverable: Boolean = true,
        val cause: Throwable? = null
    ) : LoginOutcome()
}
