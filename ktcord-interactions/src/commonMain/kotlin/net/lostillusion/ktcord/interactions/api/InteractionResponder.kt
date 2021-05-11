package net.lostillusion.ktcord.interactions.api

import net.lostillusion.ktcord.interactions.receiving.webhook.types.InteractionApplicationCommandCallbackData

sealed class InteractionResponder {
    abstract suspend fun respond(): InteractionApplicationCommandCallbackData

    sealed class SlashCommand : InteractionResponder() {
        /**
         * A deferred response for Discord interactions. A loading ACK will be made and in the meantime
         * [deferredResponse] will wait until completion and the returned [Instant] response will be made
         * as a follow up request to the initial deferred ACK.
         */
        class Deferred(private val deferredResponse: suspend () -> Instant) : InteractionResponder.SlashCommand() {
            override suspend fun respond(): InteractionApplicationCommandCallbackData = deferredResponse().respond()
        }

        class Instant(private val data: InteractionApplicationCommandCallbackData) :
            InteractionResponder.SlashCommand() {
            override suspend fun respond(): InteractionApplicationCommandCallbackData = data
        }
    }
}

fun ApplicationCommandContext.SlashCommand<*>.respond(builder: InteractionApplicationCommandCallbackData.Builder.() -> Unit): InteractionResponder.SlashCommand.Instant {
    return InteractionResponder.SlashCommand.Instant(
        InteractionApplicationCommandCallbackData.Builder().apply(builder).build()
    )
}

fun ApplicationCommandContext.SlashCommand<*>.deferred(deferredResponse: suspend () -> InteractionResponder.SlashCommand.Instant): InteractionResponder.SlashCommand.Deferred {
    return InteractionResponder.SlashCommand.Deferred(deferredResponse)
}