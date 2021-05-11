package net.lostillusion.ktcord.audio.ws.handlers

import io.ktor.util.network.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import net.lostillusion.ktcord.audio.encryption.EncryptionMode
import net.lostillusion.ktcord.audio.udp.DiscordAudioUdpConnection
import net.lostillusion.ktcord.audio.udp.UdpConfig
import net.lostillusion.ktcord.audio.ws.DiscordAudioSocket
import net.lostillusion.ktcord.audio.ws.frames.ReadyFrame
import net.lostillusion.ktcord.audio.ws.frames.SelectProtocolFrame

internal class ReadyHandler(
    private val ssrcUpdater: (Int) -> Unit,
    private val udpConnectionUpdater: (DiscordAudioUdpConnection) -> Unit,
    socket: DiscordAudioSocket
) : AudioSocketHandler(socket) {
    override suspend fun handle() {
        incoming.filterIsInstance<ReadyFrame>().collect { frame: ReadyFrame ->
            ssrcUpdater(frame.ready.ssrc)

            val udp = DiscordAudioUdpConnection(UdpConfig(frame.ready), socket.connection)

            socket.state.emit(DiscordAudioSocket.State.UDP_CONNECTED)
            udpConnectionUpdater(udp)

            val ip = udp.discoverIp()

            val selectProtocol = SelectProtocolFrame(
                selectProtocol = SelectProtocolFrame.SelectProtocol(
                    protocol = "udp",
                    data = SelectProtocolFrame.SelectProtocol.Data(
                        address = ip.hostname,
                        port = ip.port,
                        mode = EncryptionMode.NORMAL
                    )
                )
            )

            outgoing.emit(selectProtocol)
        }
    }
}