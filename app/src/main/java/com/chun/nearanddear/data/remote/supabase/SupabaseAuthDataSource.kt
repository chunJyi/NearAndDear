package com.chun.nearanddear.data.remote.supabase

import android.util.Log
import com.chun.nearanddear.domain.model.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseAuthDataSource @Inject constructor(
    private val client: SupabaseClient
) {

    suspend fun signInWithGoogleIdToken(userToInsert: User): Result<User> = runCatching {
        val existingUsers = client.from("loginUser").select {
            filter { eq("userID", userToInsert.userID) }
        }.decodeList<User>()

        if (existingUsers.isEmpty()) {
            client.from("loginUser").insert(userToInsert)
            Log.d(TAG, "User inserted successfully: ${userToInsert.userID}")
            userToInsert
        } else {
            Log.d(TAG, "User already exists: ${userToInsert.userID}")
            existingUsers[0]
        }
    }.onFailure { e ->
        Log.e(TAG, "Sign-in failed: ${e.message}", e)
    }

    fun getCurrentUser(): User? {
        val user = client.auth.currentSessionOrNull()?.user ?: return null
        val userId = user.id
        val email = user.email ?: "No Email"
        val name = user.userMetadata?.get("name")?.toString()?.trim('"') ?: "No Name"
        val avatarUrl = user.userMetadata?.get("avatar_url")?.toString()?.trim('"') ?: ""
        val now = Instant.now().toString()
        val oneYear =
            LocalDateTime.now().plus(1, ChronoUnit.YEARS).toInstant(ZoneOffset.UTC).toString()

        return User(
            userID = userId,
            name = name,
            email = email,
            phone = "No Phone",
            avatarUrl = avatarUrl,
            updatedAt = now,
            createdAt = now,
            startDate = now,
            endDate = oneYear
        )
    }

    private companion object {
        private const val TAG = "SupabaseAuthDataSource"
    }
}
