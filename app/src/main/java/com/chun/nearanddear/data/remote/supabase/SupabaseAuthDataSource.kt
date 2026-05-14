package com.chun.nearanddear.data.remote.supabase

import android.util.Log
import com.chun.nearanddear.domain.model.User
import com.chun.nearanddear.domain.model.UserLocation
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseAuthDataSource @Inject constructor(
    private val client: SupabaseClient
) {

    suspend fun signInWithGoogleIdToken(userToInsert: User): Result<User> = runCatching {
        val existingUsers = client.from("users").select {
            filter { eq("id", userToInsert.id) }
        }.decodeList<User>()

        if (existingUsers.isEmpty()) {
            client.from("users").insert(userToInsert)
            insertUserLocation(userToInsert.id)
            Log.d(TAG, "User inserted successfully: ${userToInsert.id}")
            userToInsert
        } else {
            val existingUserLocation = client.from("user_location").select {
                filter { eq("user_id", userToInsert.id) }
            }.decodeList<UserLocation>()

            if (existingUserLocation.isEmpty()) {
                insertUserLocation(userToInsert.id)
            }

            Log.d(TAG, "User already exists: ${userToInsert.id}")
            existingUsers[0]
        }
    }.onFailure { e ->
        Log.e(TAG, "Sign-in failed: ${e.message}", e)
    }

    suspend fun insertUserLocation(userId: String): Result<Unit> = runCatching {
        val location = UserLocation(
            userId = userId,
            latitude = 0.0,
            longitude = 0.0,
            updatedAt = Instant.now().toString()
        )
        client.from("user_location").insert(location)
    }

    suspend fun updateUserLocation(location: UserLocation): Result<Int> =
        runCatching {
            Log.d(TAG, "Updating location for user ${location.userId}: lat=${location.latitude}, lng=${location.longitude}")

            val body = buildJsonObject {
                put("latitude", location.latitude)
                put("longitude", location.longitude)
                put("updated_at", Instant.now().toString())
            }
            val response = client
                .from("user_location")
                .update(body
                    )
                 {
                    filter { eq("user_id", location.userId) }
                }

            Log.d(TAG, "Location update response: $response")

        }.onFailure { e ->
            Log.e(TAG, "Failed to update user location: ${e.message}", e)
        }

    private companion object {
        private const val TAG = "SupabaseAuthDataSource"
    }
}
