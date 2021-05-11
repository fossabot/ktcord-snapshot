package net.lostillusion.ktcord.interactions.api

import net.lostillusion.ktcord.interactions.api.arguments.SlashCommandArguments
import net.lostillusion.ktcord.interactions.models.responses.ApplicationCommandInteractionData

sealed class ApplicationCommandContext(val data: ApplicationCommandInteractionData) {
    /**
     * Provides context for any slash command invocation.
     *
     * @param arguments the parsed arguments for this invocation.
     * @param data the data from Discord for this invocation.
     */
    class SlashCommand<T : SlashCommandArguments>(val arguments: T, data: ApplicationCommandInteractionData) :
        ApplicationCommandContext(data)
}