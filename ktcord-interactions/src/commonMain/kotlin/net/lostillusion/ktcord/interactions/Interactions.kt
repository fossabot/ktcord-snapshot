package net.lostillusion.ktcord.interactions

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.lostillusion.ktcord.common.util.InternalKtcordApi
import net.lostillusion.ktcord.interactions.api.*
import net.lostillusion.ktcord.interactions.api.arguments.SlashCommandArguments
import net.lostillusion.ktcord.interactions.api.declarations.KTcordInteraction
import net.lostillusion.ktcord.interactions.api.declarations.SlashCommand
import net.lostillusion.ktcord.interactions.api.declarations.SlashCommandDeclaration
import net.lostillusion.ktcord.interactions.api.pipeline.DiscordInteractionPipeline
import net.lostillusion.ktcord.interactions.api.pipeline.KtcordPipelineApi
import net.lostillusion.ktcord.interactions.receiving.webhook.WebhookServer
import net.lostillusion.ktcord.interactions.receiving.webhook.defaultWebhookServer
import net.lostillusion.ktcord.interactions.receiving.webhook.handlers.DiscordRequestAdapter
import net.lostillusion.ktcord.interactions.rest.InteractionsClient
import net.lostillusion.ktcord.interactions.rest.authorization.Authorization
import net.lostillusion.ktcord.interactions.rest.authorization.AuthorizationType

@OptIn(InternalKtcordApi::class)
class Interactions(private val configuration: Configuration) {
    init {
        require(configuration.authorization != null) { "Must pass in a non-empty authorization token!" }
        require(configuration.applicationId != null) { "Must set a application id!" }
        require(configuration.webhookServerConfiguration != null) { "Webhook server configuration must be set!" }
    }

    class Configuration {
        internal var authorization: Authorization? = null
        internal var applicationId: Long? = null
        internal var webhookServerConfiguration: WebhookServer.Configuration? = null
        internal var webhookServerCreator: (WebhookServer.Configuration) -> WebhookServer = ::defaultWebhookServer
        internal var autoCreate = false

        fun applicationId(value: Long) {
            applicationId = value
        }

        fun autoCreate(value: Boolean) {
            autoCreate = value
        }

        fun authorization(token: String, type: AuthorizationType = AuthorizationType.BOT) =
            authorization(Authorization(type, token))

        fun authorization(authorization: Authorization) {
            this.authorization = authorization
        }

        /**
         * Configure the underlying webhook server configuration and or implementation which will
         * act as the gateway for all interaction requests.
         *
         * @param creator the factory which will provide a [WebhookServer],
         * By default, [net.lostillusion.ktcord.interactions.receiving.webhook.DefaultWebhookServer] is used.
        * @param configuration the configuration for the webhook server.
         */
        fun webhookServer(
            creator: (WebhookServer.Configuration) -> WebhookServer = ::defaultWebhookServer,
            configuration: WebhookServer.Configuration.() -> Unit
        ) {
            webhookServerCreator = creator
            webhookServerConfiguration = WebhookServer.Configuration().apply(configuration)
        }

        internal val interactionRegistry: InteractionRegistry = BasicInteractionRegistry()

        fun declaration(name: String, description: String, guild: Long? = null, declaration: SlashCommandDeclaration.Builder.() -> Unit) {
            interactionRegistry.add(SlashCommandDeclaration(SlashCommandDeclaration.Builder(name, description, guild).apply(declaration)))
        }

        fun declaration(declaration: SlashCommandDeclaration) {
            interactionRegistry.add(declaration)
        }

        fun <Arguments: SlashCommandArguments> slashCommand(slashCommand: SlashCommand<Arguments>) {
            with(slashCommand) {
                declaration(
                    name = info.name,
                    description = info.description,
                    guild = info.guild,
                ) {
                    action({ slashCommand.arguments }, slashCommand.executor)
                }
            }
        }

//        @Suppress("UNCHECKED_CAST")
//        fun <T : SlashCommandArguments<T>> slashCommand(
//            name: String,
//            description: String,
//            arguments: T,
//            guild: Long? = null,
//            responder: InteractionResponder<T>
//        ) {
//            interactions += KTcordInteraction(
//                CreateApplicationCommand(name, description).fillOptions(arguments),
//                arguments,
//                guild,
//                responder as InteractionResponder<*>
//            )
//        }
//
    }

    private val client = InteractionsClient(configuration.authorization!!, configuration.applicationId!!)

    @KtcordPipelineApi
    private val webhookServer = configuration.webhookServerCreator(configuration.webhookServerConfiguration!!)

    @OptIn(KtcordPipelineApi::class)
    suspend fun start() {
        if (configuration.autoCreate) {
            configuration.interactionRegistry.interactions
                .filterIsInstance<SlashCommandDeclaration>()
                .filter { it.configuration.guild != null }
                .forEach { client.createGuildApplicationCommand(it.configuration.guild!!, it.command) }
        }

        webhookServer.pipeline.withHandler(DiscordRequestAdapter(client, configuration.interactionRegistry))

        webhookServer.start()
    }

    @KtcordPipelineApi
    suspend fun startAndExposePipeline(): DiscordInteractionPipeline {
        if (configuration.autoCreate) {
            configuration.interactionRegistry.interactions
                .filterIsInstance<SlashCommandDeclaration>()
                .filter { it.configuration.guild != null }
                .forEach { client.createGuildApplicationCommand(it.configuration.guild!!, it.command) }
        }

        webhookServer.pipeline.withHandler(DiscordRequestAdapter(client, configuration.interactionRegistry))

        coroutineScope {
            launch {
                webhookServer.start()
            }
        }

        return webhookServer.pipeline
    }
}

fun interactions(configurator: Interactions.Configuration.() -> Unit): Interactions {
    val config = Interactions.Configuration().apply(configurator)
    return Interactions(config)
}
