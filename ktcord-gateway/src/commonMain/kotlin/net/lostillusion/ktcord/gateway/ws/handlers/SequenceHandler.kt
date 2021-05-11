package net.lostillusion.ktcord.gateway.ws.handlers

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.mapNotNull
import net.lostillusion.ktcord.gateway.ws.DiscordSocket

internal class SequenceHandler(
        private val sequenceUpdater: suspend (Int) -> Unit,
        socket: DiscordSocket,
) : SocketHandler(socket) {
    override suspend fun handle() {
        incoming
                .mapNotNull { it.sequence }
                .collect(sequenceUpdater)
    }
}