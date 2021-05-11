package net.lostillusion.ktcord.rest.ratelimiting

internal sealed class RateLimitResponse<out T> {
    object PerRouteLimitReached : RateLimitResponse<Nothing>()
    object GlobalLimitReached : RateLimitResponse<Nothing>()
    class Okay<T>(val value: T) : RateLimitResponse<T>()
}
