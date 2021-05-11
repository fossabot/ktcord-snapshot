package net.lostillusion.ktcord.rest.ratelimiting

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.lostillusion.ktcord.common.util.InternalKtcordApi
import net.lostillusion.ktcord.rest.DiscordRequest
import net.lostillusion.ktcord.rest.DiscordRequestMethod
import kotlin.time.ExperimentalTime
import kotlin.time.minutes

/**
 * A class used by KTcord to properly rate limit requests to Discord.
 * Any requests send through [consume] must pass through a global [ConstantBucket].
 * This bucket is given 10,000 tokens and is refilled every 10 minutes, respecting the
 * Discord global rate limit.
 * Afterwards it will pass through a [DynamicBucket], different for each major path.
 * These buckets are configured through the responses returned by Discord.
 * Any requests made through these buckets are also synchronized!
 *
 * @param client The [HttpClient] used to make requests.
 */
@OptIn(ExperimentalTime::class)
@InternalKtcordApi class RateLimiter(private val client: HttpClient) {
//    private val globalBucket = ConstantBucket(initialTokens = 10_000, refillInterval = 10.minutes)

    // all path-route buckets are dynamic
    private val buckets = mutableMapOf<String, DynamicBucket>()

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val logger = KotlinLogging.logger { }

    suspend fun <T> consume(request: DiscordRequest, serializer: KSerializer<T>): T {
        logger.trace { "Consuming request $request!" }
        val bucket = buckets.getOrPut(request.path) { DynamicBucket() }

        val requestBuilder = HttpRequestBuilder(host = "discord.com", scheme = "https", path = "/api/v8/${request.path}")
        request.body?.also { requestBuilder.body = it; requestBuilder.contentType(ContentType.Application.Json) }

        // TODO: this doesn't seem right...
//        return globalBucket.consume {
            val result = bucket.consumeAndParse {
                when (request.requestMethod) {
                    DiscordRequestMethod.GET -> client.get(requestBuilder)
                    DiscordRequestMethod.POST -> client.post(requestBuilder)
                    DiscordRequestMethod.PATCH -> client.patch(requestBuilder)
                }
            }

            return when (result) {
                RateLimitResponse.PerRouteLimitReached -> error("Dynamic bucket failed to prevent per-route rate-limit response!")
                RateLimitResponse.GlobalLimitReached -> error("Global constant bucket failed to prevent global rate-limit response!")
                is RateLimitResponse.Okay -> {
                    try {
                        json.decodeFromJsonElement(serializer, result.value)
                    } catch (e: Exception) {
                        logger.error(e) { "Error occured while deserializing result: ${result.value}" }
                        throw e
                    }
                }
            }
//        }
    }
}