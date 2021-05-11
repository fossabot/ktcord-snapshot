package net.lostillusion.ktcord.interactions.rest

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import net.lostillusion.ktcord.common.entity.Message
import net.lostillusion.ktcord.common.util.InternalKtcordApi
import net.lostillusion.ktcord.common.util.Platform
import net.lostillusion.ktcord.interactions.models.ApplicationCommand
import net.lostillusion.ktcord.interactions.models.CreateApplicationCommand
import net.lostillusion.ktcord.interactions.models.provideApplicationId
import net.lostillusion.ktcord.interactions.rest.authorization.Authorization
import net.lostillusion.ktcord.rest.DiscordRequest
import net.lostillusion.ktcord.rest.DiscordRequestMethod
import net.lostillusion.ktcord.rest.DiscordRestClient
import net.lostillusion.ktcord.rest.ratelimiting.RateLimiter

@OptIn(InternalKtcordApi::class)
class InteractionsClient(
    private val authorization: Authorization,
    private val applicationId: Long
) {
    // the http client behind a rate limiter used by ktcord to make requests to discord
    private val client = RateLimiter(client = HttpClient(Platform.engine) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json { ignoreUnknownKeys = true })
        }

        expectSuccess = false

        defaultRequest {
            header("Authorization", "Bot ${authorization.token}")
        }
    })

    suspend fun requestGlobalApplicationCommands(): List<ApplicationCommand> {
        return client.consume(DiscordRequest(
            path = "applications/$applicationId/commands",
            requestMethod = DiscordRequestMethod.GET
        ), ListSerializer(ApplicationCommand.serializer()))
    }

    suspend fun createGlobalApplicationCommand(command: CreateApplicationCommand): ApplicationCommand {
        return client.consume(DiscordRequest(
            path = "applications/$applicationId/commands",
            requestMethod = DiscordRequestMethod.POST,
            body = command.provideApplicationId(applicationId)
        ), ApplicationCommand.serializer())
    }

    suspend fun requestGlobalApplicationCommand(id: Long): ApplicationCommand {
        return client.consume(DiscordRequest(
            path = "applications/$applicationId/commands/$id",
            requestMethod = DiscordRequestMethod.GET
        ), ApplicationCommand.serializer())
    }

    suspend fun createGuildApplicationCommand(guildId: Long, command: CreateApplicationCommand): ApplicationCommand {
        return client.consume(DiscordRequest(
            path = "applications/$applicationId/guilds/$guildId/commands",
            requestMethod = DiscordRequestMethod.POST,
            body = command.provideApplicationId(applicationId)
        ), ApplicationCommand.serializer())
    }

    suspend fun requestGuildApplicationCommands(guildId: Long): List<ApplicationCommand> {
        return client.consume(DiscordRequest(
            path = "applications/$applicationId/guilds/$guildId/commands",
            requestMethod = DiscordRequestMethod.GET
        ), ListSerializer(ApplicationCommand.serializer()))
    }

    @Serializable
    class EditMessage {
        var content: String? = null
    }

    suspend fun editInitialInteractionResponse(interactionToken: String, editor: EditMessage.() -> Unit): Message {
        val payload = EditMessage().apply(editor)

        return client.consume(DiscordRequest(
            path = "webhooks/$applicationId/$interactionToken/messages/@original",
            requestMethod = DiscordRequestMethod.PATCH,
            body = payload
        ), Message.serializer())
    }

    // TODO: https://discord.com/developers/docs/interactions/slash-commands#edit-global-application-command
}