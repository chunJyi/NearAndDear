package com.chun.nearanddear.data.repository

import android.content.Context
import com.chun.nearanddear.data.remote.supabase.SupabaseAuthDataSource
import com.chun.nearanddear.domain.auth.LoginErrorMapper
import com.chun.nearanddear.domain.auth.LoginOutcome
import com.chun.nearanddear.domain.repository.AuthRepository
import com.chun.nearanddear.domain.service.GoogleAuthService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val googleAuthService: GoogleAuthService,
    private val supabaseDataSource: SupabaseAuthDataSource
) : AuthRepository {

    override suspend fun loginWithGoogle(context: Context): LoginOutcome {
        return when (val step = googleAuthService.getGoogleIdToken(context)) {
            is LoginOutcome.Cancelled,
            is LoginOutcome.Failure -> step
            is LoginOutcome.Success -> {
                supabaseDataSource.signInWithGoogleIdToken(step.user).fold(
                    onSuccess = { LoginOutcome.Success(it) },
                    onFailure = { e -> LoginErrorMapper.fromThrowable(e) }
                )
            }
        }
    }

}
