package net.lostillusion.ktcord.interactions.api.declarations

import net.lostillusion.ktcord.interactions.api.ApplicationCommandContext
import net.lostillusion.ktcord.interactions.api.InteractionResponder
import net.lostillusion.ktcord.interactions.api.arguments.SlashCommandArguments

abstract class SlashCommand<T : SlashCommandArguments>(
    private val argumentsRetriever: () -> T,
    internal val info: SlashCommandInfo,
    internal val executor: SlashCommandExecutor<T>
) {
    // provide a new instance of [T] whenever it is accessed!
    internal val arguments
        get() = argumentsRetriever()

//    internal val info by lazy(infoRetriever)
//    internal val executor by lazy(executorRetriever)
}

open class SlashCommandInfo(
    val name: String,
    val description: String,
    val guild: Long? = null
)

interface SlashCommandExecutor<Arguments : SlashCommandArguments> {
    fun execute(applicationCommandContext: ApplicationCommandContext.SlashCommand<Arguments>): InteractionResponder.SlashCommand
}

inline fun <Arguments : SlashCommandArguments> executor(crossinline action: ApplicationCommandContext.SlashCommand<Arguments>.() -> InteractionResponder.SlashCommand): SlashCommandExecutor<Arguments> {
    return object : SlashCommandExecutor<Arguments> {
        override fun execute(applicationCommandContext: ApplicationCommandContext.SlashCommand<Arguments>): InteractionResponder.SlashCommand {
            return action(applicationCommandContext)
        }
    }
}