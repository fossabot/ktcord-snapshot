package net.lostillusion.ktcord.audio.encryption

import com.codahale.xsalsa20poly1305.SecretBox
import okio.ByteString

internal actual object XSalsa20Poly1305Encoder {
    internal actual fun encode(message: ByteArray, key: ByteArray, nonce: ByteArray): ByteArray =
            SecretBox(ByteString.of(*key)).seal(ByteString.of(*nonce), ByteString.of(*message)).toByteArray()
}
