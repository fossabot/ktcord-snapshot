package net.lostillusion.ktcord.interactions.api.arguments

import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import net.lostillusion.ktcord.interactions.api.entity.User
import net.lostillusion.ktcord.interactions.models.ApplicationCommandOptionType

class UserArgument(
    override val name: String,
    override val description: String
) : SlashCommandArgument<User>() {
    override val converter: ArgumentConverter<User> = ArgumentConverter { resolved, element ->
        val id = element!!.jsonPrimitive.long

        val user = resolved!!.users!![id]!!
        val member = resolved.members?.get(id)

        User(user, member)
    }

    override val type: ApplicationCommandOptionType = ApplicationCommandOptionType.USER
}

fun SlashCommandArguments.user(name: String, description: String) = arg(UserArgument(name, description))

fun <O> SlashCommandArguments.user(
    name: String,
    description: String,
    attributeBuilder: UserArgument.() -> SlashCommandArgument<O>
) = arg(UserArgument(name, description).let(attributeBuilder))