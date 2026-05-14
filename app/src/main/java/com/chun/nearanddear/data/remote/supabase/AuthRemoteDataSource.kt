package com.chun.nearanddear.data.remote.supabase

import com.chun.nearanddear.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Minimal stub for a Supabase-backed remote auth data source.
 * In a real app this would wrap the Supabase client and handle network calls.
 */
class AuthRemoteDataSource {

    /**
     * Simulate signing in. Replace with real Supabase call in production.
     */
    suspend fun signIn(inputUser: User): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                // Build a User with sensible defaults for the stubbed implementation.
                val builtUser = User.build {
                    // Use a generated UUID as a stand-in for auth.users(id) in this stub.
                    name = inputUser.email.substringBefore('@')
                    this.email = inputUser.email
                    phone = inputUser.phone
                    avatarUrl = inputUser.avatarUrl
                    id = "user-${UUID.randomUUID()}"
                    updatedAt = ""
                }
                Result.success(builtUser)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Simulate sign out.
     */
    suspend fun signOut(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Perform remote sign-out if necessary.
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Return the currently cached user, if any. This stub returns null.
     */
    fun getCurrentUser(): User? = null
}
