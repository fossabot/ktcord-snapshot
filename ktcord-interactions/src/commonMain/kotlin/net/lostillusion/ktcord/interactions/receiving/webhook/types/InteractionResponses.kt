package net.lostillusion.ktcord.interactions.receiving.webhook.types

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import net.lostillusion.ktcord.common.entity.AllowedMentions
import net.lostillusion.ktcord.common.enums.MessageFlag
import net.lostillusion.ktcord.common.util.DiscordBitset
import net.lostillusion.ktcord.common.util.InternalKtcordApi

@Serializable(with = InteractionResponse.Serializer::class)
sealed class InteractionResponse {
    abstract val type: InteractionResponseType

    internal companion object Serializer :
        JsonContentPolymorphicSerializer<InteractionResponse>(InteractionResponse::class) {

        override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out InteractionResponse> {
            return when (InteractionResponseType.of(element.jsonObject["type"]!!.jsonPrimitive.int)) {
                InteractionResponseType.PONG -> PongResponse.serializer()
                InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE -> ChannelMessageWithSourceResponse.serializer()
                InteractionResponseType.DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE -> DeferredChannelMessageWithSource.serializer()
                InteractionResponseType.UNKNOWN -> TODO()
            }
        }
    }
}

@Serializable
internal class PongResponse : InteractionResponse() {
    override val type: InteractionResponseType = InteractionResponseType.PONG
}

@Serializable
class ChannelMessageWithSourceResponse(
    val data: InteractionApplicationCommandCallbackData
) : InteractionResponse() {
    override val type: InteractionResponseType = InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE
}

@Serializable
data class InteractionApplicationCommandCallbackData @OptIn(InternalKtcordApi::class) constructor(
    val tts: Boolean = false,
    val content: String? = "",
    val embeds: List<JsonObject> = listOf(),
    @SerialName("allowed_mentions") val allowedMentions: AllowedMentions = AllowedMentions(),
    @Serializable(with = MessageFlag.Serializer::class)
    val flags: DiscordBitset<MessageFlag> = DiscordBitset.none()
) {
    class Builder {
        var tts: Boolean = false
        var content: String = ""
        val embeds: MutableList<JsonObject> = mutableListOf()
        var allowedMentions: AllowedMentions = AllowedMentions()
        var flags: DiscordBitset<MessageFlag> = DiscordBitset.none()

        internal fun build() = InteractionApplicationCommandCallbackData(
            tts, content, embeds, allowedMentions, flags
        )
    }
}

@Serializable
internal class DeferredChannelMessageWithSource : InteractionResponse() {
    override val type: InteractionResponseType = InteractionResponseType.DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE
}