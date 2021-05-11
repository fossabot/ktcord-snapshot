package net.lostillusion.ktcord.rest

import net.lostillusion.ktcord.common.util.InternalKtcordApi

@InternalKtcordApi data class DiscordRequest(
        val path: String,
        val requestMethod: DiscordRequestMethod,
        val body: Any? = null,
)

@InternalKtcordApi enum class DiscordRequestMethod {
    GET,
    POST,
    PATCH
}
