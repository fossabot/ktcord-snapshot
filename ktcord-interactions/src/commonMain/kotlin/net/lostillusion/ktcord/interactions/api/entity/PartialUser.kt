package net.lostillusion.ktcord.interactions.api.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PartialUser(
    val avatar: String,
    val discriminator: Short,
    val id: Long,
    @SerialName("public_flags") val publicFlags: Int,
    @SerialName("username") val userName: String
)