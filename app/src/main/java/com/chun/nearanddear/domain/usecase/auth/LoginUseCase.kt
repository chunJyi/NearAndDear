package com.chun.nearanddear.domain.usecase.auth

import android.content.Context
import com.chun.nearanddear.domain.auth.LoginOutcome
import com.chun.nearanddear.domain.model.User
import com.chun.nearanddear.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(context: Context): LoginOutcome {
        return repository.loginWithGoogle(context)
    }
}
