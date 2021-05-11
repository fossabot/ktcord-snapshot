package net.lostillusion.ktcord.gateway.ws

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withTimeout
import net.lostillusion.ktcord.common.audio.AudioInformation
import net.lostillusion.ktcord.common.util.DiscordBitset
import net.lostillusion.ktcord.gateway.ws.frames.DispatchFrame
import net.lostillusion.ktcord.gateway.ws.frames.Frame
import net.lostillusion.ktcord.gateway.ws.frames.HeartbeatFrame
import net.lostillusion.ktcord.gateway.ws.frames.SelfVoiceStateUpdateFrame
import net.lostillusion.ktcord.gateway.ws.handlers.*
import net.lostillusion.ktcord.gateway.ws.util.ExponentialBackoffReconnectStrategy
import net.lostillusion.ktcord.gateway.ws.util.HeartBeater
import net.lostillusion.ktcord.gateway.ws.util.Intent

class DiscordGateway(
        token: String,
        intents: DiscordBitset<Intent>,
        reconnectStrategy: ExponentialBackoffReconnectStrategy = ExponentialBackoffReconnectStrategy(),
        scope: CoroutineScope
) : CoroutineScope by scope {
    private val socket = DiscordSocket(
            token = token,
            intents = intents,
            reconnectStrategy = reconnectStrategy,
            scope = this
    )

    private val incoming: Flow<Frame> = socket.incoming
    private val outgoing: MutableSharedFlow<Frame> = socket.outgoing
    val audioInformation: SharedFlow<AudioInformation>

    /**
     * A flow of all [DispatchFrame], or Discord events, coming through this session.
     */
    val dispatchFrames = incoming.filterIsInstance<DispatchFrame>()

    //TODO: presence

    init {
        val sequence: AtomicInt = atomic(0)
        val heartBeater = HeartBeater(this) { outgoing.emit(HeartbeatFrame(sequenceValue = sequence.value)) }

        HelloHandler(heartBeater, token, intents, socket)
        SequenceHandler({ sequence.lazySet(it) }, socket)
        ReconnectHandler(socket)
        ReadyHandler(token, { sequence.value }, socket)
        val audioInformationHandler = AudioInformationHandler(socket)
        audioInformation = audioInformationHandler.audioStateFlow
    }

    fun connect() = socket.connect()

    suspend fun disconnect() {
        socket.disconnect()
    }

    // functions to interact with the gateway

    /**
     * Suspends until both the VoiceStateUpdateFrame is retrieved as well with the VoiceServerUpdateFrame is retrieved,
     * and puts the required information for a audio socket provided by yourself or ktcord-audio into [AudioInformation].
     */
    suspend fun voiceState(state: SelfVoiceStateUpdateFrame.VoiceState): AudioInformation {
        outgoing.emit(SelfVoiceStateUpdateFrame(state = state))
        return withTimeout(1000) {
            audioInformation.filter {
                it.serverId == state.guildId &&
                        // TODO: don't hardcode
                        it.userId == 807364323604824065
            }.first()
        }
    }
}