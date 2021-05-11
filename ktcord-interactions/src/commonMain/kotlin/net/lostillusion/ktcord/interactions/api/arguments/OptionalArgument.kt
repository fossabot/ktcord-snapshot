package net.lostillusion.ktcord.interactions.api.arguments

import net.lostillusion.ktcord.interactions.models.ApplicationCommandOptionType

class OptionalArgument<T>(
    override val name: String,
    override val description: String,
    private val underlyingArgument: SlashCommandArgument<T>
) : SlashCommandArgument<T?>() {
    override val converter: ArgumentConverter<T?> =
        ArgumentConverter { resolved, element ->
            // if the argument was given, then this wrapper is just a proxy for the underlying argument
            if (element != null) {
                underlyingArgument.converter.parse(resolved, element)
                underlyingArgument.converter.parsed
            } else null
        }

    override val type: ApplicationCommandOptionType = underlyingArgument.type
    override val required: Boolean = false
}

/**
 * A optional attribute which makes the argument not required on Discord's side. This will as a result also make
 * the value nullable.
 *
 * @return a nullable argument.
 */
fun <T : Any> SlashCommandArgument<T>.optional() =
    OptionalArgument(name, description, this)