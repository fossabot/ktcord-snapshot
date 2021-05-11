package net.lostillusion.ktcord.interactions.receiving.webhook.verify

import net.i2p.crypto.eddsa.EdDSAEngine
import net.i2p.crypto.eddsa.EdDSAPublicKey
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec
import java.math.BigInteger
import java.security.MessageDigest

internal actual class Authenticator actual constructor(
    keyString: String
) {
    private val engine: EdDSAEngine

    init {
        val bytes = BigInteger(keyString, 16).toByteArray().drop(1).toByteArray()
        val spec = EdDSANamedCurveTable.getByName("Ed25519")
        engine = EdDSAEngine(MessageDigest.getInstance(spec.hashAlgorithm))
        val pubKey = EdDSAPublicKeySpec(bytes, spec)
        val vKey = EdDSAPublicKey(pubKey)
        engine.initVerify(vKey)
    }

    actual fun verify(signature: String, timestamp: String, body: String): Boolean =
        try {
            engine.verifyOneShot((timestamp + body).toByteArray(), hex(signature))
        } catch (e: Exception) {
            false
        }

    private fun hex(s: String): ByteArray {
        // From Ktor "Crypto.kt" file
        val result = ByteArray(s.length / 2)
        for (idx in result.indices) {
            val srcIdx = idx * 2
            val high = s[srcIdx].toString().toInt(16) shl 4
            val low = s[srcIdx + 1].toString().toInt(16)
            result[idx] = (high or low).toByte()
        }

        return result
    }
}

