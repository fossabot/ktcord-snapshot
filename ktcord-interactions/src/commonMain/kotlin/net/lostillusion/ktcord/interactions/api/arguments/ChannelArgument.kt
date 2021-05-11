package net.lostillusion.ktcord.interactions.api.arguments

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import net.lostillusion.ktcord.interactions.api.entity.PartialChannel
import net.lostillusion.ktcord.interactions.models.ApplicationCommandOptionType

class ChannelArgument(
    override val name: String,
    override val description: String
) : SlashCommandArgument<PartialChannel>() {
    override val converter: ArgumentConverter<PartialChannel> = ArgumentConverter { resolved, element ->
        val id = element!!.jsonPrimitive.long

        resolved!!.channels!![id]!!
    }

    override val type: ApplicationCommandOptionType = ApplicationCommandOptionType.CHANNEL
}

fun SlashCommandArguments.channel(name: String, description: String) = arg(ChannelArgument(name, description))

fun <O> SlashCommandArguments.channel(
    name: String,
    description: String,
    attributeBuilder: ChannelArgument.() -> SlashCommandArgument<O>
) = arg(ChannelArgument(name, description).let(attributeBuilder))