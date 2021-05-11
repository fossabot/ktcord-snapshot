package net.lostillusion.ktcord.audio.ws.handlers

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import net.lostillusion.ktcord.audio.ws.DiscordAudioSocket
import net.lostillusion.ktcord.audio.ws.frames.HelloFrame
import net.lostillusion.ktcord.audio.ws.frames.IdentifyFrame
import net.lostillusion.ktcord.audio.ws.util.HeartBeater
import net.lostillusion.ktcord.common.audio.AudioInformation
import kotlin.math.roundToLong

internal class HelloHandler(
    private val info: AudioInformation,
    private val heartBeater: HeartBeater,
    socket: DiscordAudioSocket
) : AudioSocketHandler(socket) {
    override suspend fun handle() {
        incoming.filterIsInstance<HelloFrame>().collect { frame: HelloFrame ->
            require(frame.hello.version == 4)

            val identifyFrame = IdentifyFrame(
                identify = IdentifyFrame.Identify(
                    serverId = info.serverId.toString(),
                    userId = info.userId.toString(),
                    sessionId = info.sessionId,
                    token = info.token
                )
            )

            outgoing.emit(identifyFrame)

            heartBeater.start(frame.hello.heartHeatInterval.roundToLong())
        }
    }
}