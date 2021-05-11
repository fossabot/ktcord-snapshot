package net.lostillusion.ktcord.interactions.api.arguments

import mu.KotlinLogging
import net.lostillusion.ktcord.interactions.models.ApplicationCommandOption
import net.lostillusion.ktcord.interactions.models.ApplicationCommandOptionType
import net.lostillusion.ktcord.interactions.models.responses.ApplicationCommandInteractionDataOption
import net.lostillusion.ktcord.interactions.models.responses.ApplicationCommandInteractionDataResolved

sealed class SlashCommandArgument<Type : Any?> {
    abstract val name: String
    abstract val description: String
    abstract val type: ApplicationCommandOptionType
    open val required: Boolean = true

    abstract val converter: ArgumentConverter<Type>
}

abstract class SlashCommandArguments {
    private val args: MutableList<SlashCommandArgument<*>> = mutableListOf()
    private val logger = KotlinLogging.logger { }

    internal fun parse(resolved: ApplicationCommandInteractionDataResolved?, arguments: List<ApplicationCommandInteractionDataOption>) {
        for (typedArgument in args) {
            val argument = arguments.find { it.name == typedArgument.name }

            if (typedArgument.required && argument == null) {
                logger.warn { "Required argument $typedArgument was not present in the resolved arguments given by Discord! Slash commands are likely desynced!" }
                continue
            }

            typedArgument.converter.parse(resolved, argument?.value)
        }
    }

    internal fun toOptions(): List<ApplicationCommandOption>? =
        if (args.isEmpty()) null
        // required arguments must go before optional requirements!!
        else args.sortedByDescending { it.required }.map { ApplicationCommandOption(it.type, it.name, it.description, it.required, null, null) }

    fun <V> arg(arg: SlashCommandArgument<V>): ArgumentConverter<V> {
        args.add(arg)
        return arg.converter
    }
}

object EmptyArguments : SlashCommandArguments()