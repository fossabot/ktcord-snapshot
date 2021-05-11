package net.lostillusion.ktcord.interactions.receiving.webhook.verify

@PublishedApi
internal expect class Authenticator(keyString: String) {
    fun verify(signature: String, timestamp: String, body: String): Boolean
}