package net.lostillusion.ktcord.gateway.ws.frames

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import net.lostillusion.ktcord.common.entity.*
import net.lostillusion.ktcord.common.enums.Status
import net.lostillusion.ktcord.common.util.DiscordBitset
import net.lostillusion.ktcord.gateway.ws.util.Intent

@Serializable(with = Frame.FrameSerializer::class)
sealed class Frame {
    abstract val op: Opcode
    open val t: String? = null

    @SerialName("s")
    open val sequence: Int? = null

    companion object FrameSerializer : JsonContentPolymorphicSerializer<Frame>(Frame::class) {
        override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out Frame> =
                when (Opcode.of(element.jsonObject["op"]!!.jsonPrimitive.int)) {
                    Opcode.DISPATCH -> DispatchFrame.serializer()
                    Opcode.HEARTBEAT -> HeartbeatFrame.serializer()
                    Opcode.IDENTIFY -> IdentifyFrame.serializer()
                    Opcode.PRESENCE_UPDATE -> PresenceUpdateFrame.serializer()
                    Opcode.VOICE_STATE_UPDATE -> SelfVoiceStateUpdateFrame.serializer()
                    Opcode.RESUME -> ResumeFrame.serializer()
                    Opcode.RECONNECT -> ReconnectFrame.serializer()
                    Opcode.REQUEST_GUILD_MEMBERS -> RequestGuildMembersFrame.serializer()
                    Opcode.INVALID_SESSION -> InvalidSessionFrame.serializer()
                    Opcode.HELLO -> HelloFrame.serializer()
                    Opcode.HEARTBEAT_ACK -> HeartbeatAckFrame.serializer()
                    Opcode.UNKNOWN -> throw NotImplementedError("Discord sent us a unknown Opcode of ${element.jsonObject["op"]!!.jsonPrimitive.int}!")
                }
    }
}

@Serializable
class PresenceUpdateFrame(@SerialName("d") val presenceUpdate: PresenceUpdate) : Frame() {
    @Serializable
    class PresenceUpdate(
            val activities: Set<Activity>? = null,
            val status: Status = Status.ONLINE,
            val afk: Boolean,
            val since: Long?
    )

    class StatusBuilder {
        private var since: Long? = null
        private var status: Status = Status.ONLINE
        private val activities: MutableSet<Activity> = mutableSetOf()
        var afk: Boolean = false

        fun since(durationInMilliseconds: Long) {
            since = durationInMilliseconds
        }

        fun activity(activityBuilder: ActivityBuilder.() -> Unit) {
            activities.add(net.lostillusion.ktcord.common.entity.ActivityBuilder().apply(activityBuilder).build())
        }

        fun status(status: Status) {
            this.status = status
        }

        fun build() = PresenceUpdate(if (activities.isEmpty()) null else activities, status, afk, since)
    }

    override val op: Opcode = Opcode.PRESENCE_UPDATE
}

@Serializable
class SelfVoiceStateUpdateFrame(@SerialName("d") val state: VoiceState) : Frame() {
    @Serializable
    data class VoiceState(
            @SerialName("guild_id") val guildId: Long,
            @SerialName("channel_id") val channelId: Long,
            @SerialName("self_mute") val selfMute: Boolean,
            @SerialName("self_deaf") val selfDeaf: Boolean
    )

    override val op: Opcode = Opcode.VOICE_STATE_UPDATE
}

@Serializable
class RequestGuildMembersFrame(@SerialName("d") val requestedMembers: GuildRequestMembers) : Frame() {
    @Serializable
    class GuildRequestMembers(
            @SerialName("guild_id") val guildId: Long,
            val query: String,
            val limit: Int,
            val presences: Boolean? = null,
            val userIds: Array<Long>? = null,
            val nonce: String? = null
    )

    override val op: Opcode = Opcode.REQUEST_GUILD_MEMBERS
}

@Serializable
class InvalidSessionFrame(@SerialName("d") val resumable: Boolean) : Frame() {
    override val op: Opcode = Opcode.INVALID_SESSION
}

@Serializable
object HeartbeatAckFrame : Frame() {
    override val op: Opcode = Opcode.HEARTBEAT_ACK
}

@Serializable
object ReconnectFrame : Frame() {
    override val op: Opcode = Opcode.RECONNECT
}

@Serializable
class HeartbeatFrame(@SerialName("d") val sequenceValue: Int?) : Frame() {
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
            val token: String,
            @Serializable(with = Intent.Serializer::class)
            val intents: DiscordBitset<Intent>,
            val properties: IdentifyProperties
    ) {
        @Serializable
        data class IdentifyProperties(
                @SerialName("\$os")
                val os: String,
                @SerialName("\$browser")
                val browser: String,
                @SerialName("\$device")
                val device: String
        )
    }

    override val op: Opcode = Opcode.IDENTIFY
}

@Serializable
class HelloFrame(@SerialName("d") val hello: Hello) : Frame() {
    @Serializable
    data class Hello(
            @SerialName("heartbeat_interval") val heartBeatInterval: Long
    )

    override val op: Opcode = Opcode.HELLO
}

@Serializable
sealed class DispatchFrame : Frame() {
    override val op: Opcode = Opcode.DISPATCH

    @SerialName("s")
    abstract override val sequence: Int

    @SerialName("READY")
    @Serializable
    data class ReadyFrame(
            @SerialName("d") val ready: Ready,
            @SerialName("s") override val sequence: Int
    ) : DispatchFrame() {
        @Serializable
        data class Ready(
                val v: String,
                val user: User,
                @SerialName("session_id")
                val sessionId: String
        )
    }

    @SerialName("GUILD_CREATE")
    @Serializable
    data class GuildCreateFrame(
            @SerialName("d") val guild: GuildData,
            @SerialName("s") override val sequence: Int
    ) : DispatchFrame()

    @SerialName("MESSAGE_CREATE")
    @Serializable
    data class MessageCreateFrame(
            @SerialName("d") val message: Message,
            @SerialName("s") override val sequence: Int
    ) : DispatchFrame()

    @SerialName("VOICE_SERVER_UPDATE")
    @Serializable
    data class VoiceServerUpdateFrame(
            @SerialName("d") val voiceServerUpdate: VoiceServerUpdate,
            @SerialName("s") override val sequence: Int
    ) : DispatchFrame() {
        @Serializable
        data class VoiceServerUpdate(
                val token: String,
                @SerialName("guild_id") val guildId: Long,
                val endpoint: String
        )
    }

    @SerialName("VOICE_STATE_UPDATE")
    @Serializable
    data class VoiceStateUpdateFrame(
            @SerialName("d") val voiceStateUpdate: VoiceStateUpdate,
            @SerialName("s") override val sequence: Int
    ) : DispatchFrame() {
        @Serializable
        data class VoiceStateUpdate(
                @SerialName("guild_id") val guildId: Long,
                @SerialName("channel_id") val channelId: Long?,
                @SerialName("user_id") val userId: Long,
                // TODO: member
                @SerialName("session_id") val sessionId: String,
                // TODO: voice states of user
        )
    }
}