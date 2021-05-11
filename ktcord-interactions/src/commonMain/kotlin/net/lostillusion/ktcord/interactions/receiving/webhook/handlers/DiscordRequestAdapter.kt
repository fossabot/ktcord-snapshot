package net.lostillusion.ktcord.interactions.receiving.webhook.handlers

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.lostillusion.ktcord.interactions.InteractionRegistry
import net.lostillusion.ktcord.interactions.api.ApplicationCommandContext
import net.lostillusion.ktcord.interactions.api.InteractionResponder
import net.lostillusion.ktcord.interactions.api.arguments.SlashCommandArguments
import net.lostillusion.ktcord.interactions.api.declarations.SlashCommandExecutor
import net.lostillusion.ktcord.interactions.receiving.webhook.types.ApplicationCommandRequest
import net.lostillusion.ktcord.interactions.receiving.webhook.types.ChannelMessageWithSourceResponse
import net.lostillusion.ktcord.interactions.receiving.webhook.types.DeferredChannelMessageWithSource
import net.lostillusion.ktcord.interactions.receiving.webhook.types.PongResponse
import net.lostillusion.ktcord.interactions.rest.InteractionsClient

internal interface DiscordRequestAdapter {
    suspend fun onPing(): DiscordHttpResponse<PongResponse> = DiscordHttpResponse(body = PongResponse())
    suspend fun onApplicationCommand(request: ApplicationCommandRequest): DiscordHttpResponse<*>
}

private class DefaultDiscordRequestAdapter(
    private val client: InteractionsClient,
    private val interactionRegistry: InteractionRegistry
) : DiscordRequestAdapter {
    private val logger = KotlinLogging.logger { }

    @Suppress("UNCHECKED_CAST")
    override suspend fun onApplicationCommand(request: ApplicationCommandRequest): DiscordHttpResponse<*> {
        val data = request.data

        val (interaction, options) = interactionRegistry.resolve(data)

        val arguments = interaction.arguments

        arguments.parse(request.data.resolved, options ?: emptyList())

        val responder = (interaction.executor as SlashCommandExecutor<SlashCommandArguments>)
            .execute(ApplicationCommandContext.SlashCommand(arguments, data))

        val (initialResponse, deferredResponse) = when (responder) {
            is InteractionResponder.SlashCommand.Instant -> Pair(
                ChannelMessageWithSourceResponse(responder.respond()),
                null
            )
            is InteractionResponder.SlashCommand.Deferred -> Pair(DeferredChannelMessageWithSource()) { suspend { responder.respond() } }
         }

        if (deferredResponse != null) {
            // launching this deferred follow-up before the actual response
            // is okay as discord will handle any race condition for us
            GlobalScope.launch {
                val callback = deferredResponse()()
                client.editInitialInteractionResponse(request.token) {
                    content = callback.content
                }
            }
        }

        return DiscordHttpResponse(
            body = initialResponse
        )
    }
}

internal fun DiscordRequestAdapter(
    client: InteractionsClient,
    interactionRegistry: InteractionRegistry
): DiscordRequestAdapter = DefaultDiscordRequestAdapter(client, interactionRegistry)