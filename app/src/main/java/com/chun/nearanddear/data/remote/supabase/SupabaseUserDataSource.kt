package com.chun.nearanddear.data.remote.supabase

import android.util.Log
import com.chun.nearanddear.domain.model.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class   SupabaseUserDataSource @Inject constructor(
    private val client: SupabaseClient
) {
    suspend fun getUserById(userId: String): Result<User> = runCatching {
        val users = client.from("users").select {
            filter { eq("id", userId) }
        }.decodeList<User>()

        if (users.isNotEmpty()) {
            Log.d(TAG, "User found: ${users[0].id}")
            users[0]
        } else {
            Log.d(TAG, "No user found with ID: $userId")
            throw NoSuchElementException("No user found with ID: $userId")
        }
    }.onFailure { e ->
        Log.e(TAG, "Failed to get user by ID: ${e.message}", e)
    }

    private companion object {
        private const val TAG = "SupabaseUserDataSource"
    }
}
