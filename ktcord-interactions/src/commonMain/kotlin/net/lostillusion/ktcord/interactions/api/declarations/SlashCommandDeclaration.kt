package net.lostillusion.ktcord.interactions.api.declarations

import mu.KotlinLogging
import net.lostillusion.ktcord.interactions.api.ApplicationCommandContext
import net.lostillusion.ktcord.interactions.api.InteractionResponder
import net.lostillusion.ktcord.interactions.api.arguments.EmptyArguments
import net.lostillusion.ktcord.interactions.api.arguments.SlashCommandArguments
import net.lostillusion.ktcord.interactions.models.ApplicationCommandOption
import net.lostillusion.ktcord.interactions.models.ApplicationCommandOptionType
import net.lostillusion.ktcord.interactions.models.CreateApplicationCommand

class SlashCommandDeclaration internal constructor(val configuration: Builder) : KTcordInteraction {
    class Builder(val name: String, val description: String, val guild: Long?) {
        private val logger = KotlinLogging.logger { }

        internal var slashCommand: SlashCommand<*>? = null
        internal val groups: MutableList<SlashCommandGroupDeclaration> = mutableListOf()

        fun <Arguments : SlashCommandArguments> action(
            arguments: () -> Arguments,
            responder: ApplicationCommandContext.SlashCommand<Arguments>.() -> InteractionResponder.SlashCommand
        ) = action(arguments, executor(responder))

        fun <Arguments : SlashCommandArguments> action(
            arguments: () -> Arguments,
            executor: SlashCommandExecutor<Arguments>
        ) {
            slashCommand = object :
                SlashCommand<Arguments>(
                    arguments,
                    SlashCommandInfo(name, description, guild),
                    executor
                ) {}
        }

        fun group(
            name: String,
            description: String,
            groupDeclaration: SlashCommandGroupDeclaration.Builder.() -> Unit
        ) {
            if (slashCommand != null) {
                logger.warn {
                    "A group declaration was attempted to added to this slash command declaration." +
                            "This declaration already consists of a command, (${slashCommand!!.info.name}), and as such" +
                            "cannot contain a group, ignoring!"
                }

                return
            }

            groups.add(
                SlashCommandGroupDeclaration(
                    SlashCommandGroupDeclaration.Builder(name, description).apply(groupDeclaration)
                )
            )
        }
    }

    private fun toApplicationCommand(): CreateApplicationCommand {
        return if(configuration.slashCommand != null) {
            with(configuration.slashCommand!!) {
                CreateApplicationCommand(info.name, info.description, arguments.toOptions())
            }
        } else {
            with(configuration) {
                CreateApplicationCommand(name, description, groups.map { TODO() })
            }
        }
    }

    override val command: CreateApplicationCommand = toApplicationCommand()
}

class SlashCommandGroupDeclaration(val configuration: Builder) {
    fun toOption() {
        ApplicationCommandOption(
            ApplicationCommandOptionType.SUB_COMMAND_GROUP,
            configuration.name,
            configuration.description,
            null
        )
    }

    class Builder(val name: String, val description: String) {
        internal val slashCommands = mutableListOf<SlashCommand<*>>()

        fun slashCommand(slashCommand: SlashCommand<*>) {
            slashCommands.add(slashCommand)
        }

        fun <Arguments : SlashCommandArguments> slashCommand(
            name: String,
            description: String,
            arguments: () -> Arguments,
            executor: SlashCommandExecutor<Arguments>,
        ) {
            val command = object :
                SlashCommand<Arguments>(
                    arguments,
                    SlashCommandInfo(name, description),
                    executor
                ) {}

            slashCommand(command)
        }

        fun <Arguments : SlashCommandArguments> slashCommand(
            name: String,
            description: String,
            arguments: () -> Arguments,
            responder: ApplicationCommandContext.SlashCommand<in Arguments>.() -> InteractionResponder.SlashCommand
        ) {
            slashCommand(
                name,
                description,
                arguments,
                executor(responder),
            )
        }

        // default - empty arguments

        fun slashCommand(
            name: String,
            description: String,
            executor: SlashCommandExecutor<EmptyArguments>,
        ) {
            val command = object :
                SlashCommand<EmptyArguments>(
                    { EmptyArguments },
                    SlashCommandInfo(name, description),
                    executor
                ) {}

            slashCommand(command)
        }

        fun slashCommand(
            name: String,
            description: String,
            responder: ApplicationCommandContext.SlashCommand<in EmptyArguments>.() -> InteractionResponder.SlashCommand
        ) {
            slashCommand(
                name,
                description,
                { EmptyArguments },
                executor(responder),
            )
        }
    }
}

fun declaration(name: String, description: String, guild: Long? = null, declaration: SlashCommandDeclaration.Builder.() -> Unit) =
    SlashCommandDeclaration(SlashCommandDeclaration.Builder(name, description, guild).apply(declaration))