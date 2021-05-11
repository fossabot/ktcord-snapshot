package net.lostillusion.ktcord.interactions.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.lostillusion.ktcord.interactions.api.arguments.SlashCommandArguments

@Serializable
data class ApplicationCommand(
    val id: Long,
    @SerialName("application_id") val applicationId: Long,
    val name: String,
    val description: String,
    val options: List<ApplicationCommandOption>? = null
)

data class CreateApplicationCommand(
    val name: String,
    val description: String,
    val options: List<ApplicationCommandOption>? = null
)

@Serializable
internal data class ProvidedApplicationIdCreateApplicationCommand(
    val applicationId: Long,
    val name: String,
    val description: String,
    val options: List<ApplicationCommandOption>? = null
)

internal fun CreateApplicationCommand.provideApplicationId(id: Long) = ProvidedApplicationIdCreateApplicationCommand(
    applicationId = id,
    name = name,
    description = description,
    options = options
)
