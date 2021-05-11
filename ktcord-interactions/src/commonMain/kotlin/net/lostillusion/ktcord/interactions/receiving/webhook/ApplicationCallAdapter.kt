package net.lostillusion.ktcord.interactions.receiving.webhook

import io.ktor.http.*
import net.lostillusion.ktcord.interactions.receiving.webhook.handlers.DiscordHttpResponse

expect class ApplicationCallAdapter {
    suspend fun respondText(status: HttpStatusCode, message: String, contentType: ContentType)
    suspend fun respond(response: DiscordHttpResponse<*>)
}