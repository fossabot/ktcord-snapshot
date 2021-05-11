package net.lostillusion.ktcord.audio.ws.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class HeartBeater constructor(
    private val scope: CoroutineScope,
    private val beat: suspend () -> Unit
) {
    private val heartMutex = Mutex()

    private var beaterJob: Job? = null

    suspend fun start(intervalInMilli: Long) {
        stop()
        heartMutex.withLock {
            beaterJob = scope.launch {
                while (true) {
                    beat()
                    delay(intervalInMilli)
                }
            }
        }
    }

    suspend fun stop() = heartMutex.withLock {
        beaterJob?.cancel()
        beaterJob = null
    }
}