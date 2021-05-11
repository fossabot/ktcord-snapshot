package net.lostillusion.ktcord.interactions.api.arguments

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import net.lostillusion.ktcord.interactions.models.responses.ApplicationCommandInteractionDataResolved
import kotlin.reflect.KProperty

abstract class ArgumentConverter<T : Any?> {
    internal var parsed: T? = null

    abstract fun parse(resolved: ApplicationCommandInteractionDataResolved?, value: JsonElement?)

    @Suppress("UNCHECKED_CAST")
    operator fun getValue(arguments: SlashCommandArguments, property: KProperty<*>): T = parsed as T
}

internal inline fun <T: Any?> ArgumentConverter(crossinline converter: (ApplicationCommandInteractionDataResolved?, JsonElement?) -> T) = object : ArgumentConverter<T>() {
    override fun parse(resolved: ApplicationCommandInteractionDataResolved?, value: JsonElement?) {
        this.parsed = converter(resolved, value)
    }
}

internal class BasicArgumentConverter<T : Any?>(private val converter: (JsonElement?) -> T) : ArgumentConverter<T>() {
    override fun parse(resolved: ApplicationCommandInteractionDataResolved?, value: JsonElement?) {
        this.parsed = converter(value)
    }
}