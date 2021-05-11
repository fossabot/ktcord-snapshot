package net.lostillusion.ktcord.interactions.models.responses

import kotlinx.serialization.Serializable

@Serializable data class ApplicationCommandInteractionData(
    val id: Long,
    val name: String,
    val resolved: ApplicationCommandInteractionDataResolved? = null,
    val options: List<ApplicationCommandInteractionDataOption>? = null
)