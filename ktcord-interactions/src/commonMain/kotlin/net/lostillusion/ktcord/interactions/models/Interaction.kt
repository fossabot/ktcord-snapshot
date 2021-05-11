package net.lostillusion.ktcord.interactions.models

import kotlinx.serialization.Serializable
import net.lostillusion.ktcord.common.entity.User

@Serializable
data class Interaction(
    val id: Long,
    val applicationId: Long,
//    val type: InteractionType,
//    val data: ApplicationCommandInteractionData,
    val guildId: Long? = null,
    val channelId: Long? = null,
    val user: User? = null,
    val token: String,
    val version: Int = 1
)
