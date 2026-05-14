package com.chun.nearanddear.data.remote.supabase

import android.util.Log
import com.chun.nearanddear.domain.model.Friend
import com.chun.nearanddear.domain.model.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseUserDataSource @Inject constructor(
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

    suspend fun searchUsers(query: String): Result<List<User>> = runCatching {
        val users = client.from("users").select {
            filter {
                or {
                    ilike("name", "%$query%")
                    ilike("email", "%$query%")
                }
            }
        }.decodeList<User>()

        Log.d(TAG, "Found ${users.size} users matching query: $query")
        users
    }.onFailure { e ->
        Log.e(TAG, "Failed to search users: ${e.message}", e)
    }

    suspend fun sendFriendRequest(userId: String, friendId: String): Result<Int> = runCatching {
        // First check if a friend request already exists (in either direction)
        val existingRequests = client.from("friends").select {
            filter {
                or {
                    and {
                        eq("user_id", userId)
                        eq("friend_id", friendId)
                    }
                    and {
                        eq("user_id", friendId)
                        eq("friend_id", userId)
                    }
                }
            }
        }.decodeList<Friend>()

        if (existingRequests.isNotEmpty()) {
            throw IllegalStateException("Friend request already exists between these users")
        }

        // Insert new friend request
        client.from("friends").insert(mapOf(
            "user_id" to userId,
            "friend_id" to friendId,
            "status" to "PENDING"
        ))

        Log.d(TAG, "Friend request sent from $userId to $friendId")
    }.onFailure { e ->
        Log.e(TAG, "Failed to send friend request: ${e.message}", e)
    }

    suspend fun checkFriendRequestStatus(userId: String, friendId: String): Result<String?> = runCatching {
        val requests = client.from("friends").select {
            filter {
                or {
                    and {
                        eq("user_id", userId)
                        eq("friend_id", friendId)
                    }
                    and {
                        eq("user_id", friendId)
                        eq("friend_id", userId)
                    }
                }
            }
        }.decodeList<Friend>()

        if (requests.isNotEmpty()) {
            requests[0].status.name
        } else {
            null // No existing relationship
        }
    }.onFailure { e ->
        Log.e(TAG, "Failed to check friend request status: ${e.message}", e)
    }

    private companion object {
        private const val TAG = "SupabaseUserDataSource"
    }
}
