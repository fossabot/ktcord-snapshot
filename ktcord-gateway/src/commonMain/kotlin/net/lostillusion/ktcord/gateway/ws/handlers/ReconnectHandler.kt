package net.lostillusion.ktcord.gateway.ws.handlers

import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import mu.KotlinLogging
import net.lostillusion.ktcord.gateway.ws.DiscordSocket
import net.lostillusion.ktcord.gateway.ws.frames.ReconnectFrame

internal class ReconnectHandler(socket: DiscordSocket) : SocketHandler(socket) {
    private val logger = KotlinLogging.logger { }

    override suspend fun handle() {
        incoming.filterIsInstance<ReconnectFrame>().collect {
            logger.debug { "Discord told us to reconnect, attempting reconnect!" }

            socket.shouldResume = true
            socket.session.close(CloseReason(CloseReason.Codes.NORMAL, "Received Opcode 7!"))
        }
    }
}