package com.chun.nearanddear.data.remote.supabase

import android.util.Log
import com.chun.nearanddear.domain.model.Friend
import com.chun.nearanddear.domain.model.FriendModel
import com.chun.nearanddear.domain.model.isFavoriteFor
import com.chun.nearanddear.domain.model.FriendRequestItem
import com.chun.nearanddear.domain.model.FriendState
import com.chun.nearanddear.domain.model.User
import com.chun.nearanddear.domain.model.UserLocation
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.HasRecord
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecordOrNull
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
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

    suspend fun getUsersByIds(ids: List<String>): Result<List<User>> = runCatching {
        val distinct = ids.distinct().filter { it.isNotBlank() }
        if (distinct.isEmpty()) return@runCatching emptyList()
        client.from("users").select {
            filter {
                isIn("id", distinct)
            }
        }.decodeList<User>()
    }.onFailure { e ->
        Log.e(TAG, "Failed to load users by ids: ${e.message}", e)
    }

    suspend fun getUserLocation(userId: String): Result<UserLocation?> = runCatching {
        val locations = client.from("user_location").select {
            filter { eq("user_id", userId) }
        }.decodeList<UserLocation>()

        locations.firstOrNull()
    }.onFailure { e ->
        Log.e(TAG, "Failed to load user location: ${e.message}", e)
    }

    @OptIn(SupabaseExperimental::class)
    fun observeUserLocation(userId: String): Flow<UserLocation> = callbackFlow {
        val realtimeChannel = client.channel("friend-location-$userId")
        val changes = realtimeChannel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "user_location"
            filter("user_id", FilterOperator.EQ, userId)
        }

        val collectJob = launch {
            realtimeChannel.subscribe(blockUntilSubscribed = true)
            changes.collect { action ->
                if (action is HasRecord) {
                    val location = action.decodeRecordOrNull<UserLocation>()
                    if (location != null && location.hasSharedCoordinates) {
                        trySend(location)
                    }
                }
            }
        }

        awaitClose {
            collectJob.cancel()
            launch {
                client.realtime.removeChannel(realtimeChannel)
            }
        }
    }

    suspend fun getAcceptedFriends(userId: String): Result<List<FriendModel>> = runCatching {
        val rows = client.from("friends").select {
            filter {
                or {
                    and {
                        eq("user_id", userId)
                        eq("status", "FRIEND")
                    }
                    and {
                        eq("friend_id", userId)
                        eq("status", "FRIEND")
                    }
                }
            }
        }.decodeList<Friend>()

        val otherIds = rows.map { row ->
            if (row.userId == userId) row.friendId else row.userId
        }.distinct()

        val users = getUsersByIds(otherIds).getOrThrow().associateBy { it.id }

        rows.mapNotNull { row ->
            val otherId = if (row.userId == userId) row.friendId else row.userId
            val u = users[otherId] ?: return@mapNotNull null
            FriendModel(
                relationshipId = row.id ?: return@mapNotNull null,
                userID = u.id,
                name = u.name,
                friendState = FriendState.FRIEND,
                friendAvatarUrl = u.avatarUrl.orEmpty(),
                isFavorite = row.isFavoriteFor(userId)
            )
        }
    }.onFailure { e ->
        Log.e(TAG, "Failed to load accepted friends: ${e.message}", e)
    }

    suspend fun setFriendFavorite(
        relationshipId: String,
        currentUserId: String,
        isFavorite: Boolean
    ): Result<Int> = runCatching {
        val row = client.from("friends").select {
            filter { eq("id", relationshipId) }
        }.decodeList<Friend>().firstOrNull()
            ?: throw NoSuchElementException("Friend relationship not found: $relationshipId")

        val body = buildJsonObject {
            when (currentUserId) {
                row.userId -> put("is_favorite", isFavorite)
                row.friendId -> put("friend_is_favorite", isFavorite)
                else -> throw IllegalStateException("User is not part of this friendship")
            }
        }
        client.from("friends").update(body) {
            filter { eq("id", relationshipId) }
        }
        Log.d(TAG, "Friend favorite updated: $relationshipId ($currentUserId) -> $isFavorite")
    }.onFailure { e ->
        Log.e(TAG, "Failed to update friend favorite: ${e.message}", e)
    }

    suspend fun getIncomingPendingFriendRequests(currentUserId: String): Result<List<FriendRequestItem>> =
        runCatching {
            val rows = client.from("friends").select {
                filter {
                    eq("friend_id", currentUserId)
                    eq("status", "PENDING")
                }
            }.decodeList<Friend>()

            val requesterIds = rows.map { it.userId }.distinct()
            val users = getUsersByIds(requesterIds).getOrThrow().associateBy { it.id }

            rows.mapNotNull { row ->
                val relationshipId = row.id ?: return@mapNotNull null
                val u = users[row.userId] ?: return@mapNotNull null
                FriendRequestItem(
                    relationshipId = relationshipId,
                    counterpartyId = u.id,
                    counterpartyName = u.name,
                    counterpartyEmail = u.email,
                    counterpartyAvatarUrl = u.avatarUrl
                )
            }
        }.onFailure { e ->
            Log.e(TAG, "Failed to load incoming friend requests: ${e.message}", e)
        }

    suspend fun getOutgoingPendingFriendRequests(currentUserId: String): Result<List<FriendRequestItem>> =
        runCatching {
            val rows = client.from("friends").select {
                filter {
                    eq("user_id", currentUserId)
                    eq("status", "PENDING")
                }
            }.decodeList<Friend>()

            val friendIds = rows.map { it.friendId }.distinct()
            val users = getUsersByIds(friendIds).getOrThrow().associateBy { it.id }

            rows.mapNotNull { row ->
                val relationshipId = row.id ?: return@mapNotNull null
                val u = users[row.friendId] ?: return@mapNotNull null
                FriendRequestItem(
                    relationshipId = relationshipId,
                    counterpartyId = u.id,
                    counterpartyName = u.name,
                    counterpartyEmail = u.email,
                    counterpartyAvatarUrl = u.avatarUrl
                )
            }
        }.onFailure { e ->
            Log.e(TAG, "Failed to load outgoing pending friend requests: ${e.message}", e)
        }

    suspend fun acceptFriendRequest(relationshipId: String): Result<Int> = runCatching {
        val body = buildJsonObject {
            put("status", "FRIEND")
        }
        client.from("friends").update(body) {
            filter { eq("id", relationshipId) }
        }
        Log.d(TAG, "Friend request accepted: $relationshipId")
    }.onFailure { e ->
        Log.e(TAG, "Failed to accept friend request: ${e.message}", e)
    }

    suspend fun deleteFriendRelationship(relationshipId: String): Result<Int> = runCatching {
        client.from("friends").delete {
            filter { eq("id", relationshipId) }
        }
        Log.d(TAG, "Friend relationship deleted: $relationshipId")
    }.onFailure { e ->
        Log.e(TAG, "Failed to delete friend relationship: ${e.message}", e)
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
