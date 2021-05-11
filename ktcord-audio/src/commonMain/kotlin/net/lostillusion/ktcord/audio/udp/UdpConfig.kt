package net.lostillusion.ktcord.audio.udp

import net.lostillusion.ktcord.audio.encryption.EncryptionMode
import net.lostillusion.ktcord.audio.ws.frames.ReadyFrame

internal data class UdpConfig(
        val ip: String,
        val port: Int,
        val ssrc: Int
) {
    constructor(ready: ReadyFrame.Ready) : this(ready.ip, ready.port, ready.ssrc)
}