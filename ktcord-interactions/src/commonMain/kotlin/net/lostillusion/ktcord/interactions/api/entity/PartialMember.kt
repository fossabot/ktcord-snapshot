package net.lostillusion.ktcord.interactions.api.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PartialMember(
    val avatar: String?,
    @SerialName("is_pending") val isPending: Boolean,
    @SerialName("joined_at") val joinedAt: String,
    val nick: String?,
    val pending: Boolean,
    val permissions: Long,
    @SerialName("premium_since") val premiumSince: String?,
    val roles: List<Long>
)