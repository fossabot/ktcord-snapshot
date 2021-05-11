package net.lostillusion.ktcord.audio.encryption

internal expect object XSalsa20Poly1305Encoder {
    internal fun encode(message: ByteArray, key: ByteArray, nonce: ByteArray): ByteArray
}
