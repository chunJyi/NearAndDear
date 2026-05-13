package com.chun.nearanddear.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Simple domain user model.
 */
@Serializable
data class User(
    @SerialName("id")
    val userID: String,
    @SerialName("created_at")
    val createdAt: String? = null, // Auto-managed by Supabase
    val name: String,
    val email: String,
    val phone: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val role: UserRole = UserRole.USER
) {
    companion object {
        inline fun build(block: Builder.() -> Unit): User {
            return Builder().apply(block).build()
        }
    }

    class Builder {
        var userID: String = ""
        var createdAt: String? = null
        var name: String = ""
        var email: String = ""
        var phone: String? = null
        var avatarUrl: String? = null
        var updatedAt: String? = null
        var role: UserRole = UserRole.USER

        fun build(): User {
            return User(
                userID = userID,
                createdAt = createdAt,
                name = name,
                email = email,
                phone = phone,
                avatarUrl = avatarUrl,
                updatedAt = updatedAt,
                role = role
            )
        }
    }
}

@Serializable
enum class UserRole {
    @SerialName("ADMIN") ADMIN,
    @SerialName("USER") USER
}

@Serializable
data class FriendModel(
    val userID: String,
    val name: String,
    var friendState: FriendState,
    val friendAvatarUrl: String
)

@Serializable
enum class FriendState {
    @SerialName("FRIEND") FRIEND,
    @SerialName("REQUEST") REQUEST,
    @SerialName("PENDING") PENDING
}
