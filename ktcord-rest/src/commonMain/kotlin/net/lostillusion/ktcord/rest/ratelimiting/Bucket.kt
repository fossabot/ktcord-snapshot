package net.lostillusion.ktcord.rest.ratelimiting

import io.ktor.client.statement.*
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.json.*
import net.lostillusion.ktcord.rest.ratelimiting.Bucket.State.CLOSED
import net.lostillusion.ktcord.rest.ratelimiting.Bucket.State.OPEN
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.*

// TODO: make refiller scopes part of specified context

/**
 * A token-bucket algorithm interface.
 * Implemented by [ConstantBucket] and [DynamicBucket].
 * [ConstantBucket] strictly follows the token-bucket algorithm, except that the refill is non-greedy!
 * [DynamicBucket] offloads the implementation of token-bucket, to the responses returned by Discord whenever
 * a request is made.
 */
internal interface Bucket {
    /**
     * A [State] representing whether this [Bucket] is currently exhausted.
     * If exhausted, the internal state will be [CLOSED].
     * Else, the internal state will be [OPEN]
     */
    enum class State {
        /**
         * The bucket is not exhausted and consuming responses.
         */
        OPEN,

        /**
         * The bucket is exhausted and is currently not consuming responses.
         */
        CLOSED;
    }

    /**
     * If required, suspend until bucket is not exhausted.
     * Then process [action].
     *
     * @param action The action to be ran once a token is given to you by the [Bucket].
     * @return the result of [action].
     */
    suspend fun <T> consume(action: suspend () -> T): T
}

/**
 * Token-bucket algorithm implementation.
 * Non-greedy refill!
 *
 * @param initialTokens the tokens this bucket will provide.
 * @Param refillInterval the interval for each non-greedy refill.
 */
@OptIn(ExperimentalTime::class)
internal class ConstantBucket(private val initialTokens: Int, private val refillInterval: Duration) : Bucket {
    private val refiller = CoroutineScope(EmptyCoroutineContext + Job() + CoroutineName("constant-bucket-refiller"))

    private val state = MutableStateFlow(OPEN)

    private val tokens = atomic(initialTokens)

    init {
        refiller.launch {
            delay(refillInterval)

            tokens.lazySet(initialTokens)

            state.emit(OPEN)
        }
    }

    override suspend fun <T> consume(action: suspend () -> T): T {
        if (tokens.value == 0) state.emit(CLOSED)
        if (state.value == CLOSED) state.filter { it == OPEN }.first()
        tokens.decrementAndGet()
        return action()
    }
}

/**
 * A token-bucket implementation powered by Discord header responses.
 * This bucket is initialized with 10 tokens and a 0 second refill interval.
 * However on the first request, these will be correctly initialized by the response returned by Discord.
 * The response's headers will contain the proper information to initialize the bucket.
 * This information is then used to power the token-bucket algorithm, similar to [ConstantBucket],
 * until the next request is made, where the information for the bucket will be updated again.
 *
 * **This implementation forces synchronization of requests!**
 * **They will be executed in the order [consumeAndParse] is called!**
 */
@OptIn(ExperimentalTime::class)
internal class DynamicBucket : Bucket, SynchronizedObject() {
    private companion object {
        private const val TEMPORARY_TOKENS = 10
        private const val X_RATELIMIT_RESET_AFTER = "x-ratelimit-reset-after"
        private const val X_RATELIMIT_LIMIT = "x-ratelimit-limit"
        private const val X_RATELIMIT_REMAINING = "x-ratelimit-remaining"
    }

    private val refiller = CoroutineScope(EmptyCoroutineContext + Job() + CoroutineName("dynamic-bucket-refiller"))
    private var job: Job? = null

    private var bucketMaxLimit = TEMPORARY_TOKENS
    private val tokens = atomic(bucketMaxLimit)

    private val defaultRefill = atomic(0.seconds)

    private val json = Json { }

    private val sync = Semaphore(1)

    /**
     * true - open for requests
     * false - not open for requests
     */
    private val open = atomic(true)

    /**
     * Suspends until the bucket is able to give out a token.
     * Executes [action] and then parses the response to properly update the configuration of this bucket.
     * **This method will force all requests to be made synchronously, following FIFO.**
     */
    suspend fun consumeAndParse(action: suspend () -> HttpResponse): RateLimitResponse<JsonObject> =
            sync.withPermit {
                while (!open.value) delay(50)

                tokens.decrementAndGet()
                val response = action()

                return parseResponse(response)
            }

    /**
     * Not supported by [DynamicBucket] as it does allow for proper handling of Discord responses.
     * Use [consumeAndParse] instead!
     */
    @Deprecated(
            message = "This method is not supported by DynamicBucket!",
            replaceWith = ReplaceWith("consumeAndParse"),
            level = DeprecationLevel.HIDDEN
    )
    override suspend fun <T> consume(action: suspend () -> T): T = throw UnsupportedOperationException(
            "Use `consumeAndParse` to keep requests synchronized!"
    )

    private suspend fun parseResponse(response: HttpResponse): RateLimitResponse<JsonObject> {
        val parsedResponse = json.parseToJsonElement(response.readText()).jsonObject

//        logger.info { response.headers }

        val resetAfter = response.headers[X_RATELIMIT_RESET_AFTER]?.toDouble()?.toDuration(DurationUnit.SECONDS)
        val remaining = response.headers[X_RATELIMIT_REMAINING]?.toInt()
        val limit = response.headers[X_RATELIMIT_LIMIT]?.toInt()

        if (remaining == 0) open.lazySet(false)

        limit?.also { bucketMaxLimit = it }
        remaining?.also { tokens.lazySet(it) }
        resetAfter?.also { refill(resetAfter, force = true) }

        return when (parsedResponse["global"]?.jsonPrimitive?.booleanOrNull) {
            true -> RateLimitResponse.GlobalLimitReached
            false -> RateLimitResponse.PerRouteLimitReached
            else -> return RateLimitResponse.Okay(parsedResponse)
        }
    }

    // FIXME: refills like a 1000 times on each refill
    private fun refillJob(delay: Duration) = refiller.launch {
        delay(delay)

        job = null

        tokens.lazySet(TEMPORARY_TOKENS)

        // open if locked
        open.lazySet(true)

        refill(defaultRefill.value, force = false)
    }

    private fun refill(delay: Duration, force: Boolean = false) {
        if (force) {
            job?.cancel()
            job = refillJob(delay)
        } else if (job == null) {
            job = refillJob(delay)
        }
    }
}