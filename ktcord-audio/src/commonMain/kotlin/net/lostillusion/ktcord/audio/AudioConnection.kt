package net.lostillusion.ktcord.audio

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import net.lostillusion.ktcord.audio.ws.DiscordAudioSocket
import net.lostillusion.ktcord.common.audio.AudioInformation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class AudioConnection(
    info: AudioInformation,
    private val channelMovesSupplier: () -> Flow<AudioInformation>,
    coroutineContext: CoroutineContext
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = EmptyCoroutineContext + coroutineContext + SupervisorJob()

    internal var audioSocket: DiscordAudioSocket = DiscordAudioSocket(info, this@AudioConnection)

    var audioProvider: AudioProvider? = null

    private val channelMoves get() = channelMovesSupplier()

    init {
        channelMoves.onEach {
            move(it)
        }.launchIn(this)
    }

    fun connect() = audioSocket.connect()

    suspend fun move(info: AudioInformation) {
        audioSocket.disconnect()

        audioSocket = DiscordAudioSocket(info, this@AudioConnection)

        connect()
    }

    suspend fun disconnect() = withContext(coroutineContext) {
        audioSocket.disconnect()
        cancel()
    }

    fun onComplete(action: () -> Unit) {
        coroutineContext[Job]!!.invokeOnCompletion { action() }
    }
}