package net.lostillusion.ktcord.audio.ws.frames

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.*
import net.lostillusion.ktcord.audio.encryption.EncryptionMode
import net.lostillusion.ktcord.audio.ws.util.SpeakingFlag
import net.lostillusion.ktcord.common.util.DiscordBitset

@Serializable(with = Frame.FrameSerializer::class)
sealed class Frame {
    abstract val op: Opcode

    companion object FrameSerializer : JsonContentPolymorphicSerializer<Frame>(Frame::class) {
        override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out Frame> =
                when (Opcode.of(element.jsonObject["op"]!!.jsonPrimitive.int)) {
                    Opcode.IDENTIFY -> IdentifyFrame.serializer()
                    Opcode.SELECT_PROTOCOL -> SelectProtocolFrame.serializer()
                    Opcode.READY -> ReadyFrame.serializer()
                    Opcode.HEARTBEAT -> HeartbeatFrame.serializer()
                    Opcode.SESSION_DESCRIPTION -> SessionDescriptionFrame.serializer()
                    Opcode.SPEAKING -> SpeakingFrame.serializer()
                    Opcode.HEARTBEAT_ACK -> HeartbeatAckFrame.serializer()
                    Opcode.RESUME -> ResumeFrame.serializer()
                    Opcode.HELLO -> HelloFrame.serializer()
                    Opcode.RESUMED -> TODO()
                    Opcode.CLIENT_DISCONNECTED -> ClientDisconnectedFrame.serializer()
                    Opcode.CODEC_HINT -> CodecHintFrame.serializer()
                    Opcode.UNKNOWN -> throw NotImplementedError("Discord sent us a unknown Audio Opcode of ${element.jsonObject["op"]!!.jsonPrimitive.int}!")
                }
    }
}

@Serializable
data class CodecHintFrame(@SerialName("d") val codecHint: CodecHint) : Frame() {
    @Serializable
    data class CodecHint(
            @SerialName("video_codec") val videoCodec: String,
            @SerialName("audio_codec") val audioCodec: String
    )

    override val op: Opcode = Opcode.CODEC_HINT
}

@Serializable
data class ClientDisconnectedFrame(@SerialName("d") val clientDisconnected: ClientDisconnected) : Frame() {
    @Serializable
    data class ClientDisconnected(@SerialName("user_id") val userId: Long)

    override val op: Opcode = Opcode.CLIENT_DISCONNECTED
}

@Serializable
class SpeakingFrame(@SerialName("d") val speaking: Speaking) : Frame() {
    @Serializable
    data class Speaking(
            @Serializable(with = SpeakingFlag.SpeakingFlagSerializer::class)
            val speaking: DiscordBitset<SpeakingFlag>,
            val delay: Int,
            val ssrc: Int
    )

    override val op: Opcode = Opcode.SPEAKING
}

@Serializable
class SelectProtocolFrame(@SerialName("d") val selectProtocol: SelectProtocol) : Frame() {
    @Serializable
    data class SelectProtocol(
            // should always be udp
            val protocol: String,
            val data: Data
    ) {
        @Serializable
        data class Data(
                val address: String,
                val port: Int,
                val mode: EncryptionMode
        )
    }

    override val op: Opcode = Opcode.SELECT_PROTOCOL
}

@Serializable
class SessionDescriptionFrame(@SerialName("d") val sessionDescription: SessionDescription) : Frame() {
    @Serializable
    class SessionDescription @OptIn(ExperimentalUnsignedTypes::class) constructor(
            val mode: EncryptionMode,
            // using short array because kotlinx-serialization ByteArray serializer only works on signed bytes, however
            // discord sends a unsigned byte array and implementing a custom serializer for this field is more work than needed.
            @SerialName("secret_key") val secretKey: Array<UByte>
    )

    override val op: Opcode = Opcode.SESSION_DESCRIPTION
}

@Serializable
class ReadyFrame(@SerialName("d") val ready: Ready) : Frame() {
    @Serializable
    data class Ready(
            val ssrc: Int,
            val ip: String,
            val port: Int,
            val modes: Set<EncryptionMode>
    )

    override val op: Opcode = Opcode.READY
}

@Serializable
class HeartbeatAckFrame(@SerialName("d") val nonce: Int) : Frame() {
    override val op: Opcode = Opcode.HEARTBEAT_ACK
}

@Serializable
class HeartbeatFrame(@SerialName("d") val nonce: Int) : Frame() {
    override val op: Opcode = Opcode.HEARTBEAT
}

@Serializable
class ResumeFrame(@SerialName("d") val resume: Resume) : Frame() {
    @Serializable
    data class Resume(
            val token: String,
            @SerialName("session_id")
            val sessionId: String,
            val seq: Int
    )

    override val op: Opcode = Opcode.RESUME
}

@Serializable
class IdentifyFrame(@SerialName("d") val identify: Identify) : Frame() {
    @Serializable
    data class Identify(
            // for some reasons these ids are strings
            @SerialName("server_id") val serverId: String,
            @SerialName("user_id") val userId: String,
            @SerialName("session_id") val sessionId: String,
            val token: String
    )

    override val op: Opcode = Opcode.IDENTIFY
}

@Serializable
class HelloFrame(@SerialName("d") val hello: Hello) : Frame() {
    @Serializable
    data class Hello(
            @SerialName("v") val version: Int,
            @SerialName("heartbeat_interval") val heartHeatInterval: Double
    )

    override val op: Opcode = Opcode.HELLO
}