package com.chun.nearanddear.domain.model

/**
 * A single friend relationship row shown in the home Request / Pending tabs.
 *
 * @param relationshipId Primary key of the `friends` row (used for accept / decline / cancel).
 * @param counterpartyId The other user's id (requester for incoming, recipient for outgoing).
 */
data class FriendRequestItem(
    val relationshipId: String,
    val counterpartyId: String,
    val counterpartyName: String,
    val counterpartyEmail: String,
    val counterpartyAvatarUrl: String?
)
