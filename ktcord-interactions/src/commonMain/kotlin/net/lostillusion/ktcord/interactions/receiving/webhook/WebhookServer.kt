package net.lostillusion.ktcord.interactions.receiving.webhook

import net.lostillusion.ktcord.interactions.InteractionRegistry
import net.lostillusion.ktcord.interactions.api.pipeline.DiscordInteractionPipeline
import net.lostillusion.ktcord.interactions.api.pipeline.KtcordPipelineApi
import net.lostillusion.ktcord.interactions.receiving.webhook.handlers.DiscordRequestAdapter
import net.lostillusion.ktcord.interactions.rest.InteractionsClient

/**
 * A interface depicting a server that handles raw HTTP interaction requests from Discord.
 * A custom implementation may be useful if one does not exist for your target.
 */
abstract class WebhookServer(
    protected val configuration: Configuration
) {
    init {
        require(configuration.validate()) { "invalid webhook server configuration!" }
    }

    @KtcordPipelineApi
    val pipeline = DiscordInteractionPipeline()

    abstract fun start()

    @OptIn(KtcordPipelineApi::class)
    fun applyHandler(client: InteractionsClient, registry: InteractionRegistry) {
        pipeline.withHandler(DiscordRequestAdapter(client, registry))
    }

    class Configuration {
        var interactionEndpoint: String? = null
        var publicKey: String? = null
        var port: Int = 80

        internal fun validate() = interactionEndpoint != null && publicKey != null
    }
}

/**
 * The default webhook server implementation for each target.
 */
internal expect class DefaultWebhookServer : WebhookServer

internal expect fun defaultWebhookServer(configuration: WebhookServer.Configuration): DefaultWebhookServer