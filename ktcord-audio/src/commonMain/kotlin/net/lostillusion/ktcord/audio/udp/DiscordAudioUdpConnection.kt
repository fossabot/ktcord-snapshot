package net.lostillusion.ktcord.audio.udp

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.*
import io.ktor.util.network.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import mu.KotlinLogging
import net.lostillusion.ktcord.audio.AudioConnection
import net.lostillusion.ktcord.audio.ws.util.SpeakingFlag
import net.lostillusion.ktcord.common.util.DiscordBitset
import kotlin.math.max
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

internal class DiscordAudioUdpConnection(
    private val udpConfig: UdpConfig,
    private val audioConnection: AudioConnection,
) {
    private companion object {
        private val SILENCE: ByteArray = byteArrayOf(0xF8.toByte(), 0xFF.toByte(), 0xFE.toByte())
    }

    private val network = NetworkAddress(udpConfig.ip, udpConfig.port)

    private var secretKey: ByteArray = byteArrayOf()

    fun setSecretKey(secretKey: ByteArray) {
        this.secretKey = secretKey
    }

    @OptIn(InternalAPI::class)
    private val socket: ConnectedDatagramSocket =
        aSocket(SelectorManager(Dispatchers.Default)).udp().connect(network)

    private val incoming = socket.incoming
    private val incomingFlow: Flow<Datagram>
        get() = incoming.consumeAsFlow()
    private val outgoing = socket.outgoing

    private val logger = KotlinLogging.logger { }

    @OptIn(ExperimentalUnsignedTypes::class, kotlin.ExperimentalStdlibApi::class)
    suspend fun discoverIp(): NetworkAddress {
        // ip discovery packet according to https://discord.com/developers/docs/topics/voice-connections#ip-discovery
        val packet = BytePacketBuilder().also {
            it.writeInt(udpConfig.ssrc)
            it.writeFully(ByteArray(66))
        }.build()
        outgoing.send(Datagram(packet, network))
        val response = incomingFlow.first().packet
        response.discard(4)
        val ip = response.readBytes(64).decodeToString().trimEnd { it.code == 0 }
        val port = response.readUShort().toInt()
        logger.trace { "Discovered IP: $ip:$port" }
        return NetworkAddress(ip, port)
    }

    private var sequence: Short = 0

    fun pollAudioFrames(scope: CoroutineScope) {
        require(secretKey.isNotEmpty())

        scope.launch(CoroutineName("ktcord-audio-poller")) {
            logger.debug { "Starting audio frame polling!" }

            val socket = audioConnection.audioSocket
            var framesOfSilence = 5
            var nextFrameTimestamp = getNanoTime()
            var speaking = false

            while (isActive) {
                var packet: AudioFramePacket? = null

                // poll for a audio provider
                // TODO: make this nicer? probably with a stateflow
                while (audioConnection.audioProvider == null) delay(100)
                val provider = audioConnection.audioProvider!!

                val frame = provider.provide()

                if (frame != null || framesOfSilence > 0) {
                    if (!speaking || frame != null) {
                        speaking = true
                        socket.sendSpeakingFlag(SpeakingFlag.MICROPHONE)
                    }
                    packet = AudioFramePacket(frame ?: SILENCE, sequence, sequence.toInt() * 960, udpConfig.ssrc)
                    if (frame == null) {
                        framesOfSilence--
                        if (framesOfSilence == 0) {
                            speaking = false
                            socket.sendSpeakingFlag(DiscordBitset.none<SpeakingFlag>())
                        }
                    } else {
                        framesOfSilence = 5
                    }
                }

                nextFrameTimestamp += 20_000_000

                sequence++

                packet?.encrypt(secretKey)

                delay(max(0, nextFrameTimestamp - getNanoTime()) / 1_000_000)

                if (packet != null) {
                    outgoing.send(Datagram(packet.asByteReadPacket(), network))
                }
            }
        }
    }
}

private fun getNanoTime() = Clock.System.now().toEpochMilliseconds() * 1_000_000