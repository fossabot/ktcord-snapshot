package net.lostillusion.ktcord.interactions.api.entity

import kotlinx.serialization.Serializable

@Serializable
data class PartialChannel(
    val id: Long,
    val name: String,
    val permissions: Long,
    val type: Int
)