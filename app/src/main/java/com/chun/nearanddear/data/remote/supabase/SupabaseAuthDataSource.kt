package com.chun.nearanddear.data.remote.supabase

import android.util.Log
import com.chun.nearanddear.domain.model.Location
import com.chun.nearanddear.domain.model.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
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

    suspend fun updateUserLocation(location: Location): Result<Unit> =
        runCatching {
            Log.d(TAG, "Updating location for user ${location.userID}: lat=${location.latitude}, lng=${location.longitude}")
            
            // First check if the record exists
            val existingRecords = client.from("location").select {
                filter { eq("userID", location.userID) }
            }.decodeList<Location>()
            
            Log.d(TAG, "Existing records before update: ${existingRecords.size}")
            if (existingRecords.isNotEmpty()) {
                Log.d(TAG, "Existing location: ${existingRecords[0]}")
            } else {
                Log.w(TAG, "No existing record found for userID: ${location.userID}")
                return@runCatching // Exit early if no record exists
            }

            // Try updating only latitude first, similar to the working SQL query
            Log.d(TAG, "Attempting to update latitude only...")
            val latitudeResponse = client
                .from("location")
                .update(
                    mapOf(
                        "latitude" to location.latitude,
                        "longitude" to location.longitude,

                        )
                ) {
                    filter {
                        eq("userID", location.userID)
                    }
                }

            Log.d(TAG, "Latitude update response: $latitudeResponse")

            // Then update longitude
            Log.d(TAG, "Attempting to update longitude...")
            val longitudeResponse = client
                .from("location")
                .update(
                    mapOf(
                        "longitude" to location.longitude,
                    )
                ) {
                    filter {
                        eq("userID", location.userID)
                    }
                }

            Log.d(TAG, "Longitude update response: $longitudeResponse")

            // Finally update timestamp
            Log.d(TAG, "Attempting to update timestamp...")
            val timestampResponse = client
                .from("location")
                .update(
                    mapOf(
                        "updated_at" to Instant.now().toString(),
                    )
                ) {
                    filter {
                        eq("userID", location.userID)
                    }
                }

            Log.d(TAG, "Timestamp update response: $timestampResponse")

            // Verify the update by fetching the updated record
            val updatedRecords = client.from("location").select {
                filter { eq("userID", location.userID) }
            }.decodeList<Location>()

            Log.d(TAG, "Updated records count: ${updatedRecords.size}")
            if (updatedRecords.isNotEmpty()) {
                Log.d(TAG, "Final updated location: ${updatedRecords[0]}")
            }
        }.onFailure { e ->
            Log.e(TAG, "Failed to update user location: ${e.message}", e)
        }

    private companion object {
        private const val TAG = "SupabasezAuthDataSource"
    }
}
