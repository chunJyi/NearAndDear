package com.chun.nearanddear.domain.usecase.auth

import com.chun.nearanddear.data.remote.supabase.SupabaseUserDataSource
import com.chun.nearanddear.domain.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetUserFromSupabaseUseCase @Inject constructor(
    private val supabaseUserDataSource: SupabaseUserDataSource
) {
    suspend operator fun invoke(userId: String): Result<User> {
        return supabaseUserDataSource.getUserById(userId)
    }
}
