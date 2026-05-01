package com.chun.nearanddear.domain.repository

import android.content.Context
import com.chun.nearanddear.domain.auth.LoginOutcome
import com.chun.nearanddear.domain.model.User

interface AuthRepository {
    suspend fun loginWithGoogle(context: Context): LoginOutcome
    fun getCurrentUser(): User?
}
