package net.lostillusion.ktcord.common.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GuildData(
        override val id: Long,
        override val name: String,
        // TODO: icon class
        val icon: String?,
        // TODO: splash/icon class??
        val splash: String?,
        @SerialName("discovery_splash") val discoverySplash: String?,
        @SerialName("owner") val isOwner: Boolean = false,
) : DiscordEntity, Nameable