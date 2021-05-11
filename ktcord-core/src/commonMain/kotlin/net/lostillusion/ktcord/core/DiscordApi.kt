package net.lostillusion.ktcord.core

import kotlinx.coroutines.*
import net.lostillusion.ktcord.interactions.Interactions
import net.lostillusion.ktcord.interactions.api.pipeline.DiscordInteractionPipeline
import net.lostillusion.ktcord.interactions.api.pipeline.KtcordPipelineApi
import net.lostillusion.ktcord.interactions.rest.authorization.Authorization
import kotlin.coroutines.CoroutineContext

class DiscordApi(private val configuration: Configuration, private val dispatcher: CoroutineDispatcher = Dispatchers.Default) : CoroutineScope {
    class Configuration {
        private var authorization: Authorization? = null
        internal var interactionsConfiguration: Interactions.Configuration? = null
//        internal var masterGatewayConfiguration: Nothing = TODO()

        /**
         * Sets the default authorization for all different Discord services.
         * The authorization can be overridden by simply redefining it in the appropriate
         * configuration method. However if left blank, and the default authorization is set
         * it will use [authorization] instead.
         */
        fun authorization(authorization: Authorization) {
            this.authorization = authorization
        }

        fun interactions(configuration: Interactions.Configuration.() -> Unit) {
            val config = Interactions.Configuration()
            authorization?.let { config.authorization(it) }
            config.let(configuration)
            interactionsConfiguration = config
        }
    }

    @KtcordPipelineApi
    lateinit var pipelineApi: DiscordInteractionPipeline
        private set

    @OptIn(KtcordPipelineApi::class)
    suspend fun start(): DiscordApi {
        if(configuration.interactionsConfiguration != null) {
            val interactions = Interactions(configuration.interactionsConfiguration!!)
            pipelineApi = interactions.startAndExposePipeline()
        }

        return this
    }

    override val coroutineContext: CoroutineContext = dispatcher + SupervisorJob() + CoroutineName("discord-api-central")
}

fun discordApi(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    configuration: DiscordApi.Configuration.() -> Unit
): DiscordApi {
    return DiscordApi(DiscordApi.Configuration().apply(configuration), dispatcher)
}