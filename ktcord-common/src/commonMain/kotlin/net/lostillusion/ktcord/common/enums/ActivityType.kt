package net.lostillusion.ktcord.common.enums

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = ActivityType.ActivityTypeSerializer::class)
enum class ActivityType(val id: Byte) {
    GAME(0),
    STREAMING(1),
    LISTENING(2),
    CUSTOM(4),
    COMPETING(5),
    UNKNOWN(-1);

    internal companion object ActivityTypeSerializer : KSerializer<ActivityType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ActivityType", PrimitiveKind.BYTE)

        override fun deserialize(decoder: Decoder): ActivityType = of(decoder.decodeByte())

        override fun serialize(encoder: Encoder, value: ActivityType) = encoder.encodeByte(value.id)

        private fun of(id: Byte) = values().find { it.id == id } ?: UNKNOWN
    }
}