package net.lostillusion.ktcord.interactions.api.arguments

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import net.lostillusion.ktcord.interactions.api.entity.PartialChannel
import net.lostillusion.ktcord.interactions.api.entity.PartialRole
import net.lostillusion.ktcord.interactions.models.ApplicationCommandOptionType

class RoleArgument(
    override val name: String,
    override val description: String
) : SlashCommandArgument<PartialRole>() {
    override val converter: ArgumentConverter<PartialRole> = ArgumentConverter { resolved, element ->
        val id = element!!.jsonPrimitive.long

        resolved!!.roles!![id]!!
    }

    override val type: ApplicationCommandOptionType = ApplicationCommandOptionType.ROLE
}

fun SlashCommandArguments.role(name: String, description: String) = arg(RoleArgument(name, description))

fun <O> SlashCommandArguments.role(
    name: String,
    description: String,
    attributeBuilder: RoleArgument.() -> SlashCommandArgument<O>
) = arg(RoleArgument(name, description).let(attributeBuilder))