package com.chun.nearanddear.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Simple domain user model.
 */
@Serializable
data class Location(
    val userID: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("updated_at") val updatedAt: String?,
) {
    companion object {
        inline fun build(block: Builder.() -> Unit): Location {
            return Builder().apply(block).build()
        }
    }

    class Builder {
        private var userID: String? = null
        private var latitude: Double? = null
        private var longitude: Double? = null
        private var updatedAt: String? = null

        fun userID(userID: String) = apply { this.userID = userID }
        fun latitude(latitude: Double) = apply { this.latitude = latitude }
        fun longitude(longitude: Double) = apply { this.longitude = longitude }
        fun updatedAt(updatedAt: String) = apply { this.updatedAt = updatedAt }

        fun build(): Location {
            return Location(
                userID = checkNotNull(userID) { "userID must not be null" },
                latitude = checkNotNull(latitude) { "latitude must not be null" },
                longitude = checkNotNull(longitude) { "longitude must not be null" },
                updatedAt = updatedAt
            )
        }
    }
}