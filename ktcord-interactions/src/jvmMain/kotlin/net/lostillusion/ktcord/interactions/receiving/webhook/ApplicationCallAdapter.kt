package net.lostillusion.ktcord.interactions.receiving.webhook

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import kotlinx.serialization.json.Json
import net.lostillusion.ktcord.interactions.receiving.webhook.handlers.DiscordHttpResponse
import net.lostillusion.ktcord.interactions.receiving.webhook.types.InteractionResponse

actual class ApplicationCallAdapter(private val underlyingCall: ApplicationCall, private val json: Json) {
    actual suspend fun respondText(status: HttpStatusCode, message: String, contentType: ContentType): Unit =
        underlyingCall.respondText(message, contentType, status)

    actual suspend fun respond(response: DiscordHttpResponse<*>): Unit =
        respondText(
            response.statusCode,
            json.encodeToString(InteractionResponse.serializer(), response.body!!),
            ContentType.Application.Json
        )
}