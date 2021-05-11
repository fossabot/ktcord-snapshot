package net.lostillusion.ktcord.interactions.receiving.webhook.types

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import net.lostillusion.ktcord.interactions.models.responses.ApplicationCommandInteractionData

@Serializable(with = InteractionRequest.Serializer::class)
sealed class InteractionRequest {
    abstract val type: InteractionRequestType

    internal companion object Serializer :
        JsonContentPolymorphicSerializer<InteractionRequest>(InteractionRequest::class) {

        override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out InteractionRequest> {
            return when (InteractionRequestType.of(element.jsonObject["type"]!!.jsonPrimitive.int)) {
                InteractionRequestType.PING -> PingRequest.serializer()
                InteractionRequestType.APPLICATION_COMMAND -> ApplicationCommandRequest.serializer()
                InteractionRequestType.UNKNOWN -> throw NotImplementedError("Received an unknown interaction request type: ${element.jsonObject["type"]!!.jsonPrimitive.int}!")
            }
        }
    }
}

@Serializable
internal class PingRequest : InteractionRequest() {
    override val type: InteractionRequestType = InteractionRequestType.PING

    override fun toString(): String  = "PingRequest"
}

@Serializable
data class ApplicationCommandRequest(
    val id: Long,
    val data: ApplicationCommandInteractionData,
    @SerialName("guild_id") val guildId: Long? = null,
    @SerialName("channel_id") val channelId: Long? = null,
    val member: JsonObject? = null,
    val user: JsonObject? = null,
    val token: String,
    val version: Int
) : InteractionRequest() {
    override val type: InteractionRequestType = InteractionRequestType.APPLICATION_COMMAND
}

