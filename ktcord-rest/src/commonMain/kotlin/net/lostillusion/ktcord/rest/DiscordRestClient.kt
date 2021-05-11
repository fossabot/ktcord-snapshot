package net.lostillusion.ktcord.rest

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import net.lostillusion.ktcord.common.entity.Message
import net.lostillusion.ktcord.common.entity.User
import net.lostillusion.ktcord.common.util.InternalKtcordApi
import net.lostillusion.ktcord.common.util.Platform
import net.lostillusion.ktcord.rest.DiscordRequestMethod.GET
import net.lostillusion.ktcord.rest.DiscordRequestMethod.POST
import net.lostillusion.ktcord.rest.ratelimiting.RateLimiter

interface DiscordRestClient {
    suspend fun requestCurrentUser(): User
    suspend fun sendMessage(channel: Long, content: String): Message
}

@OptIn(InternalKtcordApi::class) private class DiscordRestClientImpl(token: String) : DiscordRestClient {

    // the http client behind a rate limiter used by ktcord to make requests to discord
    private val client = RateLimiter(client = HttpClient(Platform.engine) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json { ignoreUnknownKeys = true })
        }

        expectSuccess = false

        defaultRequest {
            header("Authorization", "Bot $token")
        }
    })

    override suspend fun requestCurrentUser() =
            client.consume(
                    DiscordRequest(
                            path = "users/@me",
                            requestMethod = GET,
                    ), User.serializer()
            )

    override suspend fun sendMessage(channel: Long, content: String) =
            client.consume(
                    DiscordRequest(
                            path = "channels/$channel/messages",
                            requestMethod = POST,
                            body = mapOf("content" to content)
                    ), Message.serializer()
            )
}

fun DiscordRestClient(token: String): DiscordRestClient = DiscordRestClientImpl(token)