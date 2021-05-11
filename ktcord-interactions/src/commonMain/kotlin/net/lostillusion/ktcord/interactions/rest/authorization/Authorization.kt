package net.lostillusion.ktcord.interactions.rest.authorization

enum class AuthorizationType {
    BEARER,
    BOT
}

data class Authorization(val authorizationType: AuthorizationType, val token: String) {
    override fun toString(): String = "${authorizationType.name} $token"
}

fun bearerAuthorization(token: String) = Authorization(AuthorizationType.BEARER, token)
fun botAuthorization(token: String) = Authorization(AuthorizationType.BOT, token)