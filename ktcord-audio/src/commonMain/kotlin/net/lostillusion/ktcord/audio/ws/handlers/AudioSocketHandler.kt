package net.lostillusion.ktcord.audio.ws.handlers

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import net.lostillusion.ktcord.audio.ws.DiscordAudioSocket
import net.lostillusion.ktcord.audio.ws.frames.Frame

internal abstract class AudioSocketHandler(protected val socket: DiscordAudioSocket) : CoroutineScope by socket {
    protected val incoming: Flow<Frame> = socket.incoming
    protected val outgoing: MutableSharedFlow<Frame> = socket.outgoing

    init {
        launch(CoroutineName(this::class.simpleName ?: "AudioSocketHandler")) {
            handle()
        }
    }

    protected abstract suspend fun handle()
}