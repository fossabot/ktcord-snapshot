package net.lostillusion.ktcord.gateway.ws.handlers

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import net.lostillusion.ktcord.gateway.ws.DiscordSocket
import net.lostillusion.ktcord.gateway.ws.frames.DispatchFrame
import net.lostillusion.ktcord.gateway.ws.frames.ResumeFrame

internal class ReadyHandler(
        private val token: String,
        private val sequenceValue: () -> Int,
        socket: DiscordSocket
) : SocketHandler(socket) {
    private val sessionId: AtomicRef<String> = atomic("")

    override suspend fun handle() {
        incoming.filterIsInstance<DispatchFrame.ReadyFrame>().collect { frame: DispatchFrame.ReadyFrame ->
            if (socket.shouldResume) {
                require(sessionId.value.isNotEmpty())

                val resume = ResumeFrame(
                        resume = ResumeFrame.Resume(
                                token = token,
                                sessionId = sessionId.value,
                                seq = sequenceValue()
                        )
                )

                outgoing.emit(resume)

                socket.shouldResume = false
            }

            sessionId.lazySet(frame.ready.sessionId)
        }
    }
}