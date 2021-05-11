package net.lostillusion.ktcord.audio.ws.frames

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = Opcode.OpcodeSerializer::class)
enum class Opcode(val code: Int) {
    IDENTIFY(0),
    SELECT_PROTOCOL(1),
    READY(2),
    HEARTBEAT(3),
    SESSION_DESCRIPTION(4),
    SPEAKING(5),
    HEARTBEAT_ACK(6),
    RESUME(7),
    HELLO(8),
    RESUMED(9),
    CLIENT_DISCONNECTED(13),
    CODEC_HINT(14),
    UNKNOWN(Int.MAX_VALUE);

    companion object OpcodeSerializer : KSerializer<Opcode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Opcode", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): Opcode {
            return of(decoder.decodeInt())
        }

        override fun serialize(encoder: Encoder, value: Opcode) {
            encoder.encodeInt(value.code)
        }

        fun of(code: Int) = values().find { it.code == code } ?: UNKNOWN
    }
}