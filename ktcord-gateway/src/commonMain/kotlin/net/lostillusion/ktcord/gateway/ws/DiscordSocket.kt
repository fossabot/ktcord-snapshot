package net.lostillusion.ktcord.gateway.ws

import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.lostillusion.ktcord.common.util.DiscordBitset
import net.lostillusion.ktcord.common.util.Platform
import net.lostillusion.ktcord.gateway.ws.util.ExponentialBackoffReconnectStrategy
import net.lostillusion.ktcord.gateway.ws.util.GatewayCloseCode
import net.lostillusion.ktcord.gateway.ws.util.Intent
import net.lostillusion.ktcord.gateway.ws.util.ReconnectStrategy
import io.ktor.http.cio.websocket.Frame as WebsocketFrame
import net.lostillusion.ktcord.gateway.ws.frames.Frame as DiscordFrame

// TODO: use compression
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
internal class DiscordSocket(
        private val token: String,
        private val intents: DiscordBitset<Intent>,
        private val reconnectStrategy: ReconnectStrategy = ExponentialBackoffReconnectStrategy(),
        scope: CoroutineScope
) : CoroutineScope by scope {
    private companion object {
        // hardcode ws endpoint as to not require the rest module
        private const val WEBSOCKET_ENDPOINT = "wss://gateway.discord.gg/?v=8&encoding=json"
    }

    private enum class State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        AUTHENTICATED
    }

    private val client = HttpClient(Platform.engine) {
        install(WebSockets)
    }

    private val frameFormatter = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        classDiscriminator = "t"
        isLenient = true
    }

    internal lateinit var session: DefaultClientWebSocketSession

    private val rawIncomingFrames: ReceiveChannel<WebsocketFrame> get() = session.incoming

    // session i/o
    private val _incoming = BroadcastChannel<DiscordFrame>(Channel.BUFFERED)
    val incoming = _incoming.asFlow()
    val outgoing = MutableSharedFlow<DiscordFrame>()

    private val logger = KotlinLogging.logger { }

    private var reconnected = false
    internal var shouldResume: Boolean = false

    private val state = MutableStateFlow(State.DISCONNECTED)

    init {
        outgoing.onEach {
            val payload = frameFormatter.encodeToString(DiscordFrame.serializer(), it)
            session.send(payload)
            logger.trace { "Sent Discord gateway frame: $payload" }
        }.launchIn(this)
    }

    fun connect(block: DiscordSocket.() -> Unit = { }) {
        launch {
            var attempt = 0

            while (isActive) {
                logger.info { "Attempting to connect to Discord websocket!" }
                state.tryEmit(State.CONNECTING)
                try {
                    val session = withTimeoutOrNull(2000) {
                        session = client.webSocketSession { url.takeFrom(WEBSOCKET_ENDPOINT) }
                    }

                    if (session == null) {
                        attempt++

                        // stop trying to reconnect if we reached max attempts
                        if (reconnectStrategy.maxAttempts != null && attempt > reconnectStrategy.maxAttempts!!) break

                        val delay = reconnectStrategy.calculateDelay(attempt)
                        logger.info { "Failed connection attempt! Waiting $delay before next connection attempt. Attempt $attempt" }

                        delay(delay * 1000L) // delay is returned in seconds, multiply by 1000 to convert to milliseconds
                        continue // just try to connect to websocket again
                    }
                } catch (e: CancellationException) {
                    break
                }

                // reset attempt counter since we successfully connected
                attempt = 0

                logger.info { "Connected to Discord gateway socket!" }
                state.tryEmit(State.CONNECTED)

                try {
                    launch {
                        // wait to be authenticated before calling user init block
                        state.filter { it == State.AUTHENTICATED }.first()
                        if (!reconnected) block(this@DiscordSocket)
                    }

                    rawIncomingFrames.consumeAsFlow().onEach {
                        val payload = it.data.decodeToString()
                        val frame = frameFormatter.decodeFromString(DiscordFrame.serializer(), payload)
                        _incoming.send(frame)
                        logger.trace { "Received Discord gateway frame: $payload" }
                    }.collect()
                } catch (e: CancellationException) {
                    break
                } catch (e: Exception) {
                    logger.error(e) { "Exception was thrown while listening to gateway frames!" }
                }

                reconnected = true

                val reason = session.closeReason.await()

                logger.info { "Closed Gateway: $reason" }

                val parsedReason = GatewayCloseCode.ofCode(reason!!.code)
                if (parsedReason.clientError) logger.info { "Discord Gateway most likely closed due to a library error! $reason" }
                if (!parsedReason.shouldReconnect) break
                if (parsedReason.shouldResume) {
                    logger.info { "gateway should resume" }
                    continue
                }

                state.tryEmit(State.DISCONNECTED)
            }
        }
    }

    suspend fun disconnect() {
        state.emit(State.DISCONNECTED)
        session.close(CloseReason(CloseReason.Codes.NORMAL, ""))
        this.cancel()
    }

//    fun presence(presenceUpdate: PresenceUpdateFrame.PresenceUpdate) {
//        if (presenceUpdate != this.presenceUpdate) {
//            gatewayOutgoingFrames.tryEmit(PresenceUpdateFrame(presenceUpdate = presenceUpdate))
//            this.presenceUpdate = presenceUpdate
//        }
//    }
//
//    fun presence(builder: PresenceUpdateFrame.StatusBuilder.() -> Unit) =
//            presence(PresenceUpdateFrame.StatusBuilder().apply(builder).build())
}