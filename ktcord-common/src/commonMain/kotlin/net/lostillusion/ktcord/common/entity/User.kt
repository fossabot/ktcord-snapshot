package net.lostillusion.ktcord.common.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
        override val id: Long,
        @SerialName("username") override val name: String,
        val discriminator: String,
        val avatar: String? = null,
        @SerialName("bot") val isBot: Boolean = false,
        @SerialName("system") val isSystem: Boolean = false,
        @SerialName("mfa_enabled") val mfaEnabled: Boolean? = null,
        val locale: String? = null,
) : DiscordEntity, Nameable