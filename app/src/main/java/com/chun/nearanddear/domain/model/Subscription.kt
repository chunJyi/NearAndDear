package com.chun.nearanddear.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Subscription model.
 */
@Serializable
data class Subscription(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("plan")
    val plan: SubscriptionPlan,
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true
)

@Serializable
enum class SubscriptionPlan {
    @SerialName("FREE")
    FREE,

    @SerialName("PREMIUM")
    PREMIUM
}
