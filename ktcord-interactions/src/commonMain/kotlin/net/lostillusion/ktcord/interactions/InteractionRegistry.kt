package net.lostillusion.ktcord.interactions

import net.lostillusion.ktcord.interactions.api.declarations.KTcordInteraction
import net.lostillusion.ktcord.interactions.api.declarations.SlashCommand
import net.lostillusion.ktcord.interactions.api.declarations.SlashCommandDeclaration
import net.lostillusion.ktcord.interactions.api.declarations.SlashCommandExecutor
import net.lostillusion.ktcord.interactions.models.ApplicationCommandOption
import net.lostillusion.ktcord.interactions.models.ApplicationCommandOptionType
import net.lostillusion.ktcord.interactions.models.responses.ApplicationCommandInteractionData
import net.lostillusion.ktcord.interactions.models.responses.ApplicationCommandInteractionDataOption

interface InteractionRegistry {
    val interactions: List<KTcordInteraction>

    fun add(interaction: KTcordInteraction)

    fun resolve(data: ApplicationCommandInteractionData) : Pair<SlashCommand<*>, List<ApplicationCommandInteractionDataOption>?> {
        // probably have to rework this when new interactions are added.
        val base = interactions.find {
            when(it) {
                is SlashCommandDeclaration -> { it.command.name == data.name }
            }
        }

        when(base) {
            is SlashCommandDeclaration -> {
                if(base.configuration.slashCommand != null) return base.configuration.slashCommand!! to data.options
                else {
                    val groups = ArrayDeque(base.configuration.groups)
                    val expectedGroup = data.options?.firstOrNull { it.type == ApplicationCommandOptionType.SUB_COMMAND_GROUP } ?: unsynced()

                    while(groups.isNotEmpty()) {
                        val group = groups.removeFirst().configuration

                        if(expectedGroup.name == group.name) {
                            val expectedCommand = expectedGroup.options?.firstOrNull { it.type == ApplicationCommandOptionType.SUB_COMMAND } ?: unsynced()

                            return (group.slashCommands.firstOrNull { it.info.name == expectedCommand.name } ?: unsynced()) to expectedCommand.options
                        } else continue
                    }
                }
            }
        }

        unsynced()
    }

    private inline fun unsynced(): Nothing = throw error("slash commands are not synced!")
}

class BasicInteractionRegistry(
    vararg interactions: KTcordInteraction
) : InteractionRegistry {
    private val _interactions: MutableList<KTcordInteraction> = mutableListOf(*interactions)

    override val interactions: List<KTcordInteraction> = _interactions

    override fun add(interaction: KTcordInteraction) {
        _interactions += interaction
    }
}