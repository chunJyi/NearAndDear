package com.chun.nearanddear.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Simple domain user model.
 */
@Serializable
data class Location(
    val userID: String,
    val latitude: String,
    val longitude: String,
    val updatedAt: String? = Instant.now().toString()
)

