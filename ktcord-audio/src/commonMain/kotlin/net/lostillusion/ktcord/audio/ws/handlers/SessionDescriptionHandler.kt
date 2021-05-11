package net.lostillusion.ktcord.audio.ws.handlers

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import net.lostillusion.ktcord.audio.udp.DiscordAudioUdpConnection
import net.lostillusion.ktcord.audio.ws.DiscordAudioSocket
import net.lostillusion.ktcord.audio.ws.frames.SessionDescriptionFrame
import net.lostillusion.ktcord.audio.ws.util.SpeakingFlag
import net.lostillusion.ktcord.common.util.DiscordBitset

internal class SessionDescriptionHandler(
    private val udpGetter: () -> DiscordAudioUdpConnection,
    socket: DiscordAudioSocket
) : AudioSocketHandler(socket) {
    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun handle() {
        incoming.filterIsInstance<SessionDescriptionFrame>().collect {
            val udp = udpGetter()
            udp.setSecretKey(it.sessionDescription.secretKey.toUByteArray().toByteArray())
            socket.sendSpeakingFlag(DiscordBitset.none())
            udp.pollAudioFrames(this)
        }
    }
}