package com.chun.nearanddear.data.remote.supabase

import android.util.Log
import com.chun.nearanddear.domain.model.Location
import com.chun.nearanddear.domain.model.User
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
        val existingUsers = client.from("loginUser").select {
            filter { eq("userID", userToInsert.userID) }
        }.decodeList<User>()

        if (existingUsers.isEmpty()) {
            client.from("loginUser").insert(userToInsert);
            insertUserLocation(userToInsert.userID);
            Log.d(TAG, "User inserted successfully: ${userToInsert.userID}")
            userToInsert
        } else {
            val existingUserLocation = client.from("location").select {
                filter { eq("userID", userToInsert.userID) }
            }.decodeList<Location>()

            if (existingUserLocation.isEmpty()) {
                insertUserLocation(userToInsert.userID);
            }

            Log.d(TAG, "User already exists: ${userToInsert.userID}")
            existingUsers[0]
        }
    }.onFailure { e ->
        Log.e(TAG, "Sign-in failed: ${e.message}", e)
    }

    suspend fun insertUserLocation(userId: String): Result<Unit> = runCatching {
        val location = Location(
            userID = userId,
            latitude = 0.0,
            longitude = 0.0,
            updatedAt = Instant.now().toString()
        )
        client.from("location").insert(location)
    }

    suspend fun updateUserLocation(location: Location): Result<Int> =
        runCatching {
            Log.d(TAG, "Updating location for user ${location.userID}: lat=${location.latitude}, lng=${location.longitude}")

            val body = buildJsonObject {
                put("latitude", location.latitude)
                put("longitude", location.longitude)
                put("updated_at", Instant.now().toString())
            }
            val response = client
                .from("location")
                .update(body
                    )
                 {
                    filter { eq("userID", location.userID) }
                }

            Log.d(TAG, "Location update response: $response")

        }.onFailure { e ->
            Log.e(TAG, "Failed to update user location: ${e.message}", e)
        }

    private companion object {
        private const val TAG = "SupabaseAuthDataSource"
    }
}
