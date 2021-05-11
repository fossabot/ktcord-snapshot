package net.lostillusion.ktcord.gateway.ws.handlers

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.lostillusion.ktcord.common.util.DiscordBitset
import net.lostillusion.ktcord.gateway.ws.DiscordSocket
import net.lostillusion.ktcord.gateway.ws.frames.HelloFrame
import net.lostillusion.ktcord.gateway.ws.frames.IdentifyFrame
import net.lostillusion.ktcord.gateway.ws.util.HeartBeater
import net.lostillusion.ktcord.gateway.ws.util.Intent

internal class HelloHandler(
        private val heartBeater: HeartBeater,
        private val token: String,
        private val intents: DiscordBitset<Intent>,
        socket: DiscordSocket
) : SocketHandler(socket) {
    override suspend fun handle() {
        incoming.filterIsInstance<HelloFrame>().collect {
            heartBeater.start(it.hello.heartBeatInterval)

            val identify = IdentifyFrame(identify = IdentifyFrame.Identify(
                    token = token,
                    intents = intents,
                    properties = IdentifyFrame.Identify.IdentifyProperties(
                            os = "windows",
                            browser = "ktcord",
                            device = "ktcord"
                    )
            ))

            outgoing.emit(identify)
        }
    }
}