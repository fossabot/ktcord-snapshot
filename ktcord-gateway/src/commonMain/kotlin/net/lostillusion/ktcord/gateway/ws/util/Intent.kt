package net.lostillusion.ktcord.gateway.ws.util

import kotlinx.serialization.KSerializer
import net.lostillusion.ktcord.common.util.DiscordBitset
import net.lostillusion.ktcord.common.util.InternalKtcordApi
import net.lostillusion.ktcord.common.util.bitsetSerializer

enum class Intent(override val value: Int) : DiscordBitset<Intent> {
    GUILDS(1 shl 0),
    GUILD_MEMBERS(1 shl 1),
    GUILDS_BANS(1 shl 2),
    GUILDS_EMOJIS(1 shl 3),
    GUILD_INTEGRATIONS(1 shl 4),
    GUILD_WEBHOOKS(1 shl 5),
    GUILD_INVITES(1 shl 6),
    GUILD_VOICE_STATES(1 shl 7),
    GUILD_PRESENCES(1 shl 8),
    GUILD_MESSAGES(1 shl 9),
    GUILD_MESSAGE_REACTIONS(1 shl 10),
    GUILD_MESSAGE_TYPING(1 shl 11),
    DIRECT_MESSAGES(1 shl 12),
    DIRECT_MESSAGE_REACTIONS(1 shl 13),
    DIRECT_MESSAGE_TYPING(1 shl 14);

    @OptIn(InternalKtcordApi::class)
    internal companion object Serializer: KSerializer<DiscordBitset<Intent>> by bitsetSerializer()
}