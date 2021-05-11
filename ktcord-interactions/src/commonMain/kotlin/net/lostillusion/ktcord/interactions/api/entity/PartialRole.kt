package net.lostillusion.ktcord.interactions.api.entity

import kotlinx.serialization.Serializable

@Serializable
data class PartialRole(
    val color: Int,
    val hoist: Boolean,
    val id: Long,
    val managed: Boolean,
    val mentionable: Boolean,
    val name: String,
    val permissions: Long,
    val position: Int
)