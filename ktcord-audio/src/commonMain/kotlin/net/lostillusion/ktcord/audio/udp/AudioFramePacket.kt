package net.lostillusion.ktcord.audio.udp

import io.ktor.utils.io.core.*
import net.lostillusion.ktcord.audio.encryption.XSalsa20Poly1305Encoder

class AudioFramePacket(
        private var audioFrame: ByteArray,
        private val sequence: Short,
        private val timestamp: Int,
        private val ssrc: Int
) {
    private companion object {
        private const val RTP_TYPE: Byte = 0x80.toByte()
        private const val RTP_VERSION = 0x78.toByte()
        private const val NONCE_LENGTH = 24
        private const val RTP_HEADER_LENGTH = 12
    }

    private val header = BytePacketBuilder().also {
        it.writeByte(RTP_TYPE)
        it.writeByte(RTP_VERSION)
        it.writeShort(sequence)
        it.writeInt(timestamp)
        it.writeInt(ssrc)
    }.build().readBytes()

    fun encrypt(key: ByteArray) {
        val nonce = ByteArray(NONCE_LENGTH)
        header.copyInto(nonce, 0, 0, RTP_HEADER_LENGTH)
        audioFrame = XSalsa20Poly1305Encoder.encode(audioFrame, key, nonce)
    }

    fun asByteReadPacket() = BytePacketBuilder().also {
        it.writeFully(header)
        it.writeFully(audioFrame)
    }.build()
}