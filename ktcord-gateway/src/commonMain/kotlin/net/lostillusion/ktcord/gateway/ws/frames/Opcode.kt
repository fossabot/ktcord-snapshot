package net.lostillusion.ktcord.gateway.ws.frames

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = Opcode.OpcodeSerializer::class)
enum class Opcode(val code: Int) {
    DISPATCH(0),
    HEARTBEAT(1),
    IDENTIFY(2),
    PRESENCE_UPDATE(3),
    VOICE_STATE_UPDATE(4),
    RESUME(6),
    RECONNECT(7),
    REQUEST_GUILD_MEMBERS(8),
    INVALID_SESSION(9),
    HELLO(10),
    HEARTBEAT_ACK(11),
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

//@Serializable(with = OpcodeSerializer::class)
//sealed class Opcode<Payload> {
//    abstract val op: Int
//    abstract val d: Payload
//    val s: Int? = null
//    val t: String? = null
//
//    init {
//        require((op == 0 && s != null && t != null) || (op != 0))
//    }
//
//    @Serializable
//    class HelloOpcode(override val d: HelloPayload) : Opcode<HelloPayload>() {
//        override val op: Int = 10
//    }
//
//}
//
//private class OpcodeSerializer<T>(private val payloadSerializer: KSerializer<T>): KSerializer<Opcode<T>> {
//    override val descriptor: SerialDescriptor = PolymorphicClass
//
//    override fun serialize(encoder: Encoder, value: Opcode<T>) {
//        encoder.encodeStructure(descriptor) {
//
//        }
//    }
//    override fun deserialize(decoder: Decoder): Opcode<T> {
//
//    }
//}