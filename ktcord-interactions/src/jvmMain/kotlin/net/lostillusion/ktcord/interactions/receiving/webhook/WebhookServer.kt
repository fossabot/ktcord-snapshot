package net.lostillusion.ktcord.interactions.receiving.webhook

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.lostillusion.ktcord.interactions.api.pipeline.InteractionCall
import net.lostillusion.ktcord.interactions.api.pipeline.KtcordPipelineApi
import net.lostillusion.ktcord.interactions.receiving.webhook.types.InteractionRequest
import net.lostillusion.ktcord.interactions.receiving.webhook.verify.VerifyRequestFeature

@KtcordPipelineApi
internal actual class DefaultWebhookServer(configuration: WebhookServer.Configuration) : WebhookServer(configuration) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }
    private val logger = KotlinLogging.logger { }

    override fun start() {
        embeddedServer(Netty, port = configuration.port) {
            // use our custom VerifyRequestFeature to verify the any request is indeed, coming from discord.
            install(VerifyRequestFeature) {
                publicKey = configuration.publicKey!!
            }

            // allow for reading content more than once, since the body was read in the verify feature, and may be
            // read in future pipeline phases.
            install(DoubleReceive)

            routing {
                // this endpoint will be the gateway for any request coming from discord.
                post(configuration.interactionEndpoint!!) {
                    val payload = call.receiveText()

                    logger.trace { "[Discord Interaction Request]: $payload" }

                    val data = json.decodeFromString(InteractionRequest.serializer(), payload)

                    pipeline.execute(InteractionCall(data, ApplicationCallAdapter(call, json)))
                }
            }
        }.start(wait = false)
    }
}

@KtcordPipelineApi
internal actual fun defaultWebhookServer(configuration: WebhookServer.Configuration) =
    DefaultWebhookServer(configuration)