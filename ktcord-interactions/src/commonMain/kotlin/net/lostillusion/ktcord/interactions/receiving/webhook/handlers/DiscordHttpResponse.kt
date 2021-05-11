package net.lostillusion.ktcord.interactions.receiving.webhook.handlers

import io.ktor.http.*
import net.lostillusion.ktcord.interactions.receiving.webhook.types.InteractionResponse

data class DiscordHttpResponse<out T: InteractionResponse>(
    val statusCode: HttpStatusCode = HttpStatusCode.OK,
    val body: T? = null
)