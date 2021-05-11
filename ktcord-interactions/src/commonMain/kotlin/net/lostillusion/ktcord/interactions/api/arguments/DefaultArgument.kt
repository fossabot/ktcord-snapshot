package net.lostillusion.ktcord.interactions.api.arguments

import net.lostillusion.ktcord.interactions.models.ApplicationCommandOptionType

class DefaultArgument<T>(
    override val name: String,
    override val description: String,
    private val defaultProvider: () -> T,
    private val underlyingArgument: OptionalArgument<T>
) : SlashCommandArgument<T>() {
    override val converter: ArgumentConverter<T> = ArgumentConverter { resolved, element ->
        underlyingArgument.converter.parse(resolved, element)
        underlyingArgument.converter.parsed ?: defaultProvider()
    }

    override val type: ApplicationCommandOptionType = underlyingArgument.type
    override val required: Boolean = false
}


/**
 * A convenience attribute that provides a default value for nullable arguments.
 *
 * @param provider the provider for a default value. This value of this provider will be returned instead of null.
 * @return a non-required argument with a default provider instead of null.
 */
fun <T : Any> OptionalArgument<T>.default(
    provider: () -> T
) = DefaultArgument(name, description, provider, this)