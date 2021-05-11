package net.lostillusion.ktcord.gateway.ws.util

import io.ktor.util.date.*
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random
import kotlin.time.ExperimentalTime

/**
 * A strategy used by [net.lostillusion.ktcord.gateway.ws.DiscordSocket] to figure the delay between reconnects.
 * If [maxAttempts] is set, then KTcord will only attempt to connect that amount of times before aborting.
 * Else, on each failed connection attempt, the delay before the next attempt will be calculated by [calculateDelay].
 */
interface ReconnectStrategy {
    /**
     * The maximum amount of reconnection attempts before KTcord will stop trying to reconnecting.
     */
    val maxAttempts: Int?

    /**
     * The function called to calculate the delay between each failed reconnect attempt, in seconds.
     */
    fun calculateDelay(attempt: Int): Int
}

/**
 * A reconnect strategy based off the exponential backoff algorithm.
 * Each attempt will increase the delay exponentially, along with a random offset, until a maximum backoff threshold
 * is reached. At which point, the delay will stay at the maximum backoff.
 *
 * @param maxAttempts the maximum amount of reconnection attempts before KTcord will abort reconnecting.
 */
class ExponentialBackoffReconnectStrategy(override val maxAttempts: Int? = null) : ReconnectStrategy {
    private companion object {
        private val random = Random(getTimeMillis())

        // in seconds
        private const val MAXIMUM_BACKOFF = 32
    }

    override fun calculateDelay(attempt: Int): Int =
            min(2.0.pow(attempt).toInt() + random.nextInt(1000) / 1000, MAXIMUM_BACKOFF)
}

@ExperimentalTime
class LinearReconnectStrategy(
        private val constantDelayInSeconds: Int,
        override val maxAttempts: Int? = null
) : ReconnectStrategy {
    override fun calculateDelay(attempt: Int): Int = constantDelayInSeconds
}