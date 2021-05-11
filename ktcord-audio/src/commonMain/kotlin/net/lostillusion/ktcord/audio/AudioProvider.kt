package net.lostillusion.ktcord.audio

fun interface AudioProvider {
    fun provide(): ByteArray?
}