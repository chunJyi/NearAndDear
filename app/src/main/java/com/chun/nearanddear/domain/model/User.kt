package com.chun.nearanddear.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Simple domain user model.
 */
@Serializable
data class User(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("email")
    val email: String,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("phone")
    val phone: String? = null,
    @SerialName("role")
    val role: UserRole = UserRole.USER,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
) {
    companion object {
        inline fun build(block: Builder.() -> Unit): User {
            return Builder().apply(block).build()
        }
    }

    class Builder {
        var id: String = ""
        var name: String = ""
        var email: String = ""
        var avatarUrl: String? = null
        var phone: String? = null
        var role: UserRole = UserRole.USER
        var createdAt: String? = null
        var updatedAt: String? = null

        fun build(): User {
            return User(
                id = id,
                name = name,
                email = email,
                avatarUrl = avatarUrl,
                phone = phone,
                role = role,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
    }
}

@Serializable
enum class UserRole {
    @SerialName("USER")
    USER,

    @SerialName("ADMIN")
    ADMIN
}

@Serializable
data class Friend(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("friend_id")
    val friendId: String,
    @SerialName("status")
    val status: FriendStatus,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
enum class FriendStatus {
    @SerialName("PENDING")
    PENDING,

    @SerialName("FRIEND")
    FRIEND,

    @SerialName("BLOCKED")
    BLOCKED
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
    @SerialName("PENDING")
    PENDING,

    @SerialName("FRIEND")
    FRIEND,

    @SerialName("BLOCKED")
    BLOCKED
}
