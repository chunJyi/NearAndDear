package com.chun.nearanddear.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * User location model.
 */
@Serializable
data class UserLocation(
    @SerialName("user_id")
    val userId: String,
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("updated_at")
    val updatedAt: String? = null
) {
    companion object {
        inline fun build(block: Builder.() -> Unit): UserLocation {
            return Builder().apply(block).build()
        }
    }

    class Builder {
        var userId: String = ""
        var latitude: Double = 0.0
        var longitude: Double = 0.0
        var updatedAt: String? = null

        fun build(): UserLocation {
            return UserLocation(
                userId = userId,
                latitude = latitude,
                longitude = longitude,
                updatedAt = updatedAt
            )
        }
    }
}