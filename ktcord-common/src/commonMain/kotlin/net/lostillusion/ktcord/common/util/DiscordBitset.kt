package net.lostillusion.ktcord.common.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

@OptIn(InternalKtcordApi::class)
interface DiscordBitset<T : DiscordBitset<T>> {
    val value: Int

    @Suppress("UNCHECKED_CAST")
    operator fun plus(bit: T): DiscordBitset<T> =
        BasicDiscordBitset<T>(value or bit.value) as T

    infix fun contains(bit: T) =
        (value and bit.value) == bit.value

    companion object {
        fun <T : DiscordBitset<T>> none(): DiscordBitset<T> = BasicDiscordBitset(0)
    }
}

@InternalKtcordApi
inline fun <reified T : DiscordBitset<T>> bitsetSerializer(): KSerializer<T> = object : KSerializer<T> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BitsetSerializer", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): T = BasicDiscordBitset<T>(decoder.decodeInt()) as T

    override fun serialize(encoder: Encoder, value: T) = encoder.encodeInt(value.value)
}

@InternalKtcordApi
@Serializable(with = BasicDiscordBitset.Serializer::class)
@JvmInline
value class BasicDiscordBitset<T : DiscordBitset<T>>(override val value: Int) : DiscordBitset<T> {
    @InternalKtcordApi
    companion object Serializer : KSerializer<BasicDiscordBitset<*>> by bitsetSerializer()
}
