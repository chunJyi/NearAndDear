package com.chun.nearanddear.domain.repository

import android.content.Context
import com.chun.nearanddear.domain.auth.LoginOutcome

interface AuthRepository {
    suspend fun loginWithGoogle(context: Context): LoginOutcome
}
