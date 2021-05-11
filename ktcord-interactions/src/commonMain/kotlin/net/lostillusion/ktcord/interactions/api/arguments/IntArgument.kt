package net.lostillusion.ktcord.interactions.api.arguments

import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import net.lostillusion.ktcord.interactions.models.ApplicationCommandOptionType

class IntArgument(
    override val name: String,
    override val description: String
) : SlashCommandArgument<Int>() {
    override val converter: ArgumentConverter<Int> = BasicArgumentConverter { it!!.jsonPrimitive.int }

    override val type: ApplicationCommandOptionType = ApplicationCommandOptionType.INTEGER
}

fun SlashCommandArguments.integer(name: String, description: String) = arg(IntArgument(name, description))

fun <O> SlashCommandArguments.integer(
    name: String,
    description: String,
    attributeBuilder: IntArgument.() -> SlashCommandArgument<O>
) = arg(IntArgument(name, description).let(attributeBuilder))