package com.chun.nearanddear.data.remote.supabase

import android.util.Log
import com.chun.nearanddear.domain.model.Location
import com.chun.nearanddear.domain.model.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import okio.utf8Size
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

    suspend fun updateUserLocation(location: Location): Result<Unit> =
        runCatching {
            client
                .from("location")
                .update(
                    mapOf(
                        "latitude" to location.latitude,
                        "longitude" to location.longitude,
                        "updated_at" to Instant.now().toString()
                    )
                ) {
                    filter {
                        eq("user_id", location.userID)
                    }
                }
        }

    private companion object {
        private const val TAG = "SupabaseAuthDataSource"
    }
}
