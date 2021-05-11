package net.lostillusion.ktcord.interactions.receiving.webhook.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = InteractionRequestType.Serializer::class)
enum class InteractionRequestType(val value: Int) {
    PING(1),

    APPLICATION_COMMAND(2),

    UNKNOWN(-1);

    internal companion object Serializer : KSerializer<InteractionRequestType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("InteractionType", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): InteractionRequestType =
            of(decoder.decodeInt())

        override fun serialize(encoder: Encoder, value: InteractionRequestType) {
            encoder.encodeInt(value.value)
        }

        fun of(value: Int) = values().find { it.value == value } ?: UNKNOWN
    }
}