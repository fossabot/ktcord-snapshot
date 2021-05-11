package net.lostillusion.ktcord.audio.ws.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import net.lostillusion.ktcord.common.util.DiscordBitset
import net.lostillusion.ktcord.common.util.InternalKtcordApi
import net.lostillusion.ktcord.common.util.bitsetSerializer

@Serializable(with = SpeakingFlag.SpeakingFlagSerializer::class)
enum class SpeakingFlag(override val value: Int) : DiscordBitset<SpeakingFlag> {
    MICROPHONE(1 shl 0),
    SOUNDSHARE(1 shl 1),
    PRIORITY(1 shl 2);

    @OptIn(InternalKtcordApi::class)
    internal companion object SpeakingFlagSerializer : KSerializer<DiscordBitset<SpeakingFlag>> by bitsetSerializer()
}
