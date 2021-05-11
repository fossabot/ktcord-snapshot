package net.lostillusion.ktcord.interactions.receiving.webhook.verify

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.*

internal class VerifyRequestFeature(configuration: Configuration) {
    val authenticator = Authenticator(configuration.publicKey)

    init {
        require(configuration.publicKey.isNotEmpty()) { "publicKey must not be empty!" }
    }

    class Configuration {
        var publicKey: String = ""
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, VerifyRequestFeature> {
        override val key: AttributeKey<VerifyRequestFeature> = AttributeKey("VerifyRequestFeature")

        private const val X_SIGNATURE_ED25519 = "X-Signature-Ed25519"
        private const val X_SIGNATURE_TIMESTAMP = "X-Signature-Timestamp"

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit
        ): VerifyRequestFeature {
            val configuration = Configuration().apply(configure)

            val feature = VerifyRequestFeature(configuration)

            pipeline.intercept(ApplicationCallPipeline.Call) {
                val signature = call.request.headers[X_SIGNATURE_ED25519]!!
                val timestamp = call.request.headers[X_SIGNATURE_TIMESTAMP]!!
                val body = call.receiveText()

                if (!feature.authenticator.verify(signature, timestamp, body)) {
                    call.respond(HttpStatusCode.Unauthorized, "invalid request signature")
                    this.finish()
                }
            }

            return feature
        }
    }
}