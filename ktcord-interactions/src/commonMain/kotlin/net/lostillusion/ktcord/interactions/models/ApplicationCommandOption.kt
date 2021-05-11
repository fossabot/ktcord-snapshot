package net.lostillusion.ktcord.interactions.models

import kotlinx.serialization.Serializable

@Serializable
data class ApplicationCommandOption(
    val type: ApplicationCommandOptionType,
    val name: String,
    val description: String,
    val required: Boolean? = null,
    val choices: List<ApplicationCommandOptionChoice>? = null,
    val options: List<ApplicationCommandOption>? = null
)