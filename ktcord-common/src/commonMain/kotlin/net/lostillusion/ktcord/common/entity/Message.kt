package net.lostillusion.ktcord.common.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(
        override val id: Long,
        @SerialName("channel_id")
        val channelId: Long,
        @SerialName("guild_id")
        val guildId: Long? = null,
        // TODO: make separate MessageAuthor since it wont always be a user!
        val author: User,
        val content: String
) : DiscordEntity
