package net.lostillusion.ktcord.gateway.ws.handlers

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import net.lostillusion.ktcord.gateway.ws.DiscordSocket
import net.lostillusion.ktcord.gateway.ws.frames.Frame

internal abstract class SocketHandler(protected val socket: DiscordSocket) : CoroutineScope by socket {
    protected val incoming: Flow<Frame> = socket.incoming
    protected val outgoing: MutableSharedFlow<Frame> = socket.outgoing

    init {
        launch(CoroutineName(this::class.simpleName ?: "SocketHandler")) {
            handle()
        }
    }

    protected abstract suspend fun handle()
}