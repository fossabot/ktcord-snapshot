package net.lostillusion.ktcord.interactions.api.pipeline

import io.ktor.http.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.lostillusion.ktcord.interactions.receiving.webhook.handlers.DiscordHttpResponse
import net.lostillusion.ktcord.interactions.receiving.webhook.handlers.DiscordRequestAdapter
import net.lostillusion.ktcord.interactions.receiving.webhook.types.ApplicationCommandRequest
import net.lostillusion.ktcord.interactions.receiving.webhook.types.InteractionResponse
import net.lostillusion.ktcord.interactions.receiving.webhook.types.PingRequest

@KtcordPipelineApi
class DiscordInteractionPipeline : Pipeline<Unit, InteractionCall>(Before, Call, After) {
    companion object {
        /**
         * The Phase right before any execution on the call. May be useful for custom filters, etc.
         */
        val Before = PipelinePhase("Before")

        /**
         * The actual phase where the call is executed on and a response is resolved.
         */
        val Call = PipelinePhase("Call")

        /**
         * The phase after the response has been sent. May be useful for logging, or follow-ups.
         */
        val After = PipelinePhase("After")
    }

    private val logger = KotlinLogging.logger { }

    private suspend fun PipelineContext<Unit, InteractionCall>.parse(
        json: Json,
        responder: suspend () -> DiscordHttpResponse<*>
    ) {
        // we are only given 3 seconds to respond to an interaction. if not our token is expired.
        // lets limit ourselves to 2 seconds for some head room
        val response = withTimeoutOrNull(2000) { responder() } ?: kotlin.run {
            logger.warn { "Interaction Responder took over 2 seconds to complete! This is abnormally long and was aborted!" }
            return
        }

        val payload = json.encodeToString(InteractionResponse.serializer(), response.body!!)

        context.call.respondText(
            status = response.statusCode,
            message = payload,
            contentType = ContentType.Application.Json
        )

        logger.trace { "Sending Discord Interaction Response: $payload" }
    }

    @KtcordPipelineApi
    internal fun withHandler(handler: DiscordRequestAdapter) {
        val json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }

        intercept(Call) {
            parse(json) {
                with(handler) {
                    when (context.request) {
                        is PingRequest -> onPing()
                        is ApplicationCommandRequest -> onApplicationCommand(context.request as ApplicationCommandRequest)
                    }
                }
            }
        }
    }
}