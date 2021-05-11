package net.lostillusion.ktcord.interactions.api.arguments

import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive
import net.lostillusion.ktcord.interactions.models.ApplicationCommandOptionType

class BooleanArgument(
    override val name: String,
    override val description: String
) : SlashCommandArgument<Boolean>() {
    override val converter: ArgumentConverter<Boolean> = BasicArgumentConverter { it!!.jsonPrimitive.boolean }

    override val type: ApplicationCommandOptionType = ApplicationCommandOptionType.BOOLEAN
}

fun SlashCommandArguments.boolean(name: String, description: String) = arg(BooleanArgument(name, description))

fun <O> SlashCommandArguments.boolean(
    name: String,
    description: String,
    attributeBuilder: BooleanArgument.() -> SlashCommandArgument<O>
) = arg(BooleanArgument(name, description).let(attributeBuilder))