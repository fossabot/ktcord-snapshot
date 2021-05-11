package net.lostillusion.ktcord.core.utils

import net.lostillusion.ktcord.common.entity.DiscordEntity
import net.lostillusion.ktcord.common.entity.Message
import kotlin.internal.OnlyInputTypes
import kotlin.reflect.KProperty1

class EntityCache<T : DiscordEntity>(
        vararg indexTypes: KProperty1<T, *>
) {
    private val elements = mutableSetOf<T>()
    private val indices = indexTypes.map { EntityIndex(it) }

    private class UnsupportedIndexException(index: KProperty1<*, *>)
        : RuntimeException("Unable to retrieve EntityIndex for index of `${index.name}`!")

    private fun <V> getIndex(index: KProperty1<T, V>) = indices.find { it.index == index }
            ?: throw UnsupportedIndexException(index)

    fun add(value: T) {
        elements.add(value)
        indices.forEach { it.add(value) }
    }

    fun all(): Set<T> = elements

    fun purge() = indices.forEach(EntityIndex<*, *>::purge)

    fun <@OnlyInputTypes V> getByIndex(index: KProperty1<T, V>, value: V) = getIndex(index)[value]
}

class EntityIndex<T : DiscordEntity, V>(
        val index: KProperty1<T, V>
) {
    private val elementsByIndex = mutableMapOf<V, MutableList<T>>()

    fun add(value: T) {
        elementsByIndex.getOrPut(index.get(value)) { mutableListOf() } += value
    }

    fun all() = elementsByIndex

    operator fun get(value: V) = elementsByIndex[value]

    fun purge() = elementsByIndex.clear()
}

@Suppress("UNCHECKED_CAST")
private operator fun <T : DiscordEntity, V> List<EntityIndex<T, *>>.get(index: KProperty1<T, V>) =
        find { it.index == index } as EntityIndex<T, V>

fun temp() {
    val messageCache = EntityCache(
            Message::id,
            Message::channelId,
            Message::guildId
    )

    messageCache.getByIndex(Message::guildId, 123)
}
