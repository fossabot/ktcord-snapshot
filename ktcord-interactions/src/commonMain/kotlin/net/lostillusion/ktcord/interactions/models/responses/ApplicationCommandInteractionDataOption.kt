package net.lostillusion.ktcord.interactions.models.responses

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import net.lostillusion.ktcord.interactions.models.ApplicationCommandOptionType

@Serializable data class ApplicationCommandInteractionDataOption(
    val name: String,
    val type: ApplicationCommandOptionType,
    val value: JsonElement? = null,
    val options: List<ApplicationCommandInteractionDataOption>? = null
)