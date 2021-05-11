package net.lostillusion.ktcord.common.enums

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import net.lostillusion.ktcord.common.util.DiscordBitset
import net.lostillusion.ktcord.common.util.InternalKtcordApi
import net.lostillusion.ktcord.common.util.bitsetSerializer

@OptIn(InternalKtcordApi::class)
@Serializable(with = MessageFlag.Serializer::class)
enum class MessageFlag(override val value: Int) : DiscordBitset<MessageFlag> {
    CROSSPOSTED(1 shl 0),
    IS_CROSSPOST(1 shl 1),
    SUPPRESS_EMBEDS(1 shl 2),
    SOURCE_MESSAGE_DELETED(1 shl 3),
    URGENT(1 shl 4),
    EPHEMERAL(1 shl 6),
    LOADING(1 shl 7);

    @InternalKtcordApi
    companion object Serializer : KSerializer<DiscordBitset<MessageFlag>> by bitsetSerializer()
}