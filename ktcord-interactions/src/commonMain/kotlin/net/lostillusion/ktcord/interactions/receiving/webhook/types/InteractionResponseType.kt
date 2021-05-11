package net.lostillusion.ktcord.interactions.receiving.webhook.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = InteractionResponseType.Serializer::class)
enum class InteractionResponseType(val value: Int) {
    PONG(1),

    CHANNEL_MESSAGE_WITH_SOURCE(4),

    DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE(5),

    UNKNOWN(-1);

    internal companion object Serializer : KSerializer<InteractionResponseType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("InteractionType", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): InteractionResponseType =
            of(decoder.decodeInt())

        override fun serialize(encoder: Encoder, value: InteractionResponseType) {
            encoder.encodeInt(value.value)
        }

        fun of(value: Int) = values().find { it.value == value } ?: UNKNOWN
    }
}