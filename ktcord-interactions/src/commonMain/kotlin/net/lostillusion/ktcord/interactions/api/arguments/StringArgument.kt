package net.lostillusion.ktcord.interactions.api.arguments

import kotlinx.serialization.json.jsonPrimitive
import net.lostillusion.ktcord.interactions.models.ApplicationCommandOptionType

class StringArgument(
    override val name: String,
    override val description: String
) : SlashCommandArgument<String>() {
    override val converter: ArgumentConverter<String> = BasicArgumentConverter { it!!.jsonPrimitive.content }

    override val type: ApplicationCommandOptionType = ApplicationCommandOptionType.STRING
}

fun SlashCommandArguments.string(name: String, description: String) = arg(StringArgument(name, description))

fun <O> SlashCommandArguments.string(
    name: String,
    description: String,
    attributeBuilder: StringArgument.() -> SlashCommandArgument<O>
) = arg(StringArgument(name, description).let(attributeBuilder))