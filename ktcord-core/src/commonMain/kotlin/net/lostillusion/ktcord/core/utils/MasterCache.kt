package net.lostillusion.ktcord.core.utils

import net.lostillusion.ktcord.common.entity.*
import kotlin.internal.OnlyInputTypes
import kotlin.reflect.KProperty1
import kotlin.reflect.typeOf

/**
 * A "master" KTcord cache containing all caches for entities.
 */
@OptIn(ExperimentalStdlibApi::class)
internal class MasterCache {
    @PublishedApi
    internal val caches = mapOf(
            Message::class to net.lostillusion.ktcord.core.utils.EntityCache(
                Message::id,
                Message::guildId,
                Message::channelId
            ),
            User::class to net.lostillusion.ktcord.core.utils.EntityCache(User::id),
        GuildData::class to net.lostillusion.ktcord.core.utils.EntityCache(GuildData::id)
    )

    @Suppress("UNCHECKED_CAST")
    @PublishedApi
    internal inline fun <reified T : DiscordEntity> getCache() =
            (caches[typeOf<T>().classifier]!! as EntityCache<T>)

    inline fun <reified T : DiscordEntity, @OnlyInputTypes V> getByIndex(index: KProperty1<T, V>, value: V) =
            getCache<T>().getByIndex(index, value)

    inline fun <reified T : DiscordEntity> add(value: T) =
            getCache<T>().add(value)

    fun purge() = caches.values.forEach { it.purge() }
}