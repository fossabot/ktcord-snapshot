package net.lostillusion.ktcord.interactions.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.lostillusion.ktcord.common.enums.ActivityType

@Serializable(with = ApplicationCommandOptionType.ApplicationCommandOptionTypeSerializer::class)
enum class ApplicationCommandOptionType(val value: Byte) {
    SUB_COMMAND(1),
    SUB_COMMAND_GROUP(2),
    STRING(3),
    INTEGER(4),
    BOOLEAN(5),
    USER(6),
    CHANNEL(7),
    ROLE(8);

    internal companion object ApplicationCommandOptionTypeSerializer : KSerializer<ApplicationCommandOptionType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ApplicationCommandOptionType", PrimitiveKind.BYTE)

        override fun deserialize(decoder: Decoder): ApplicationCommandOptionType = of(decoder.decodeByte())

        override fun serialize(encoder: Encoder, value: ApplicationCommandOptionType) = encoder.encodeByte(value.value)

        private fun of(value: Byte) = ApplicationCommandOptionType.values().find { it.value == value }!!
    }
}