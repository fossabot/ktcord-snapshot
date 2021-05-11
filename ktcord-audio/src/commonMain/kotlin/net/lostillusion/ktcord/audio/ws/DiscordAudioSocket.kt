package net.lostillusion.ktcord.audio.ws

import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.lostillusion.ktcord.audio.AudioConnection
import net.lostillusion.ktcord.audio.udp.DiscordAudioUdpConnection
import net.lostillusion.ktcord.audio.ws.frames.HeartbeatFrame
import net.lostillusion.ktcord.audio.ws.frames.SpeakingFrame
import net.lostillusion.ktcord.audio.ws.handlers.HelloHandler
import net.lostillusion.ktcord.audio.ws.handlers.ReadyHandler
import net.lostillusion.ktcord.audio.ws.handlers.SessionDescriptionHandler
import net.lostillusion.ktcord.audio.ws.util.AudioCloseCode
import net.lostillusion.ktcord.audio.ws.util.HeartBeater
import net.lostillusion.ktcord.audio.ws.util.SpeakingFlag
import net.lostillusion.ktcord.common.audio.AudioInformation
import net.lostillusion.ktcord.common.util.DiscordBitset
import net.lostillusion.ktcord.common.util.Platform
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.properties.Delegates
import io.ktor.http.cio.websocket.Frame as WebsocketFrame
import net.lostillusion.ktcord.audio.ws.frames.Frame as DiscordFrame

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class DiscordAudioSocket(
        private val info: AudioInformation,
        internal val connection: AudioConnection,
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = connection.coroutineContext + SupervisorJob() + EmptyCoroutineContext

    private val client = HttpClient(Platform.engine) {
        install(WebSockets)
    }

    internal enum class State {
        DISCONNECTED,
        CONNECTING,
        SOCKET_CONNECTED,
        UDP_CONNECTED
    }

    private lateinit var session: DefaultClientWebSocketSession

    private val rawIncomingFrames: ReceiveChannel<WebsocketFrame>
        get() = session.incoming

    val outgoing = MutableSharedFlow<DiscordFrame>()

    private val _incoming = BroadcastChannel<DiscordFrame>(Channel.BUFFERED)
    val incoming = _incoming.asFlow()

    internal val state = MutableStateFlow(State.DISCONNECTED)

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val logger = KotlinLogging.logger { }

    private var ssrc: Int by Delegates.notNull()

    init {
        state.onEach { logger.info { "Discord Audio Socket State Change: $it" } }.launchIn(this)

        outgoing.onEach {
            val payload = json.encodeToString(DiscordFrame.serializer(), it)
            session.send(payload)
            logger.trace { "Sending Discord audio socket frame: $payload" }
        }.launchIn(this)

        val nonce: AtomicInt = atomic(0)
        val heartBeater = HeartBeater(this) { outgoing.emit(HeartbeatFrame(nonce = nonce.getAndIncrement())) }

        var udp: DiscordAudioUdpConnection by Delegates.notNull()
        HelloHandler(info, heartBeater, this)
        ReadyHandler({ ssrc = it }, { udp = it }, this)
        SessionDescriptionHandler({ udp }, this)
    }

    fun connect() {
        launch {
            while (isActive) {
                state.tryEmit(State.CONNECTING)

                logger.info { "Attempting to connect to Discord audio socket!" }

                try {
                    withTimeout(2000) {
                        session = client.webSocketSession {
                            url.takeFrom(info.endpoint)
                        }
                    }
                } catch (e: Exception) {
                    logger.info { "Could not connect to audio socket. Attempting to reconnect!" }
                    continue
                }

                state.tryEmit(State.SOCKET_CONNECTED)

                try {
                    rawIncomingFrames.consumeAsFlow().onEach {
                        val payload = it.data.decodeToString()
                        val frame = json.decodeFromString(DiscordFrame.serializer(), payload)
                        logger.trace { "Received Discord audio socket frame: $payload" }
                        _incoming.send(frame)
                    }.collect()
                } catch (e: Exception) {
                    logger.error(e) { "An exception occurred while listening to Discord Audio Socket frames!" }
                }

                state.tryEmit(State.DISCONNECTED)

                speakingState = null

                val reason = session.closeReason.await()

                val parsedReason = AudioCloseCode.ofCode(reason!!.code)

                if (parsedReason.libraryError) logger.info { "Audio Socket most likely closed due to a library error! $parsedReason" }
                if (!parsedReason.shouldReconnect) {
                    this@DiscordAudioSocket.also { it.cancel() }
                }
                if (parsedReason.shouldResume) {
                    logger.info { "audio socket should resume" }
                    continue
                }

                logger.info { "Closed Audio Socket: $reason" }
            }
        }
    }

    suspend fun disconnect() {
        this.also {
            state.tryEmit(State.DISCONNECTED)
            if (!session.outgoing.isClosedForSend)
                session.close(CloseReason(CloseReason.Codes.NORMAL, "Requested by client!"))

            it.cancel()
        }
    }

    private var speakingState: DiscordBitset<SpeakingFlag>? = null

    internal suspend fun sendSpeakingFlag(state: DiscordBitset<SpeakingFlag>) {
        if (state.value != speakingState?.value) {
            speakingState = state
            outgoing.emit(SpeakingFrame(speaking = SpeakingFrame.Speaking(
                    speaking = state,
                    delay = 0,
                    ssrc = ssrc
            )))
        }
    }
}

