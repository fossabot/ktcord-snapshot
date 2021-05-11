package net.lostillusion.ktcord.gateway.ws.handlers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withTimeoutOrNull
import net.lostillusion.ktcord.common.audio.AudioInformation
import net.lostillusion.ktcord.gateway.ws.DiscordSocket
import net.lostillusion.ktcord.gateway.ws.frames.DispatchFrame

internal class AudioInformationHandler(socket: DiscordSocket) : SocketHandler(socket) {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val _audioStateFlow: MutableSharedFlow<AudioInformation> = MutableSharedFlow()
    val audioStateFlow: SharedFlow<AudioInformation> = _audioStateFlow

    override suspend fun handle() {
        incoming.filterIsInstance<DispatchFrame.VoiceStateUpdateFrame>().collect { frame ->
            val voiceServer = withTimeoutOrNull(1000) {
                incoming
                        .filterIsInstance<DispatchFrame.VoiceServerUpdateFrame>()
                        .filter { it.voiceServerUpdate.guildId == frame.voiceStateUpdate.guildId }
                        .first()
                        .voiceServerUpdate
            } ?: return@collect

            val voiceState = frame.voiceStateUpdate

            val info = AudioInformation(
                    serverId = voiceServer.guildId,
                    userId = voiceState.userId,
                    sessionId = voiceState.sessionId,
                    token = voiceServer.token,
                    endpoint = "wss://" + voiceServer.endpoint + "?v=4"
            )

            _audioStateFlow.emit(info)
        }
    }
}